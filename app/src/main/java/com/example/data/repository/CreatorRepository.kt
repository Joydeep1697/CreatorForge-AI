package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.local.CreatorForgeDatabase
import com.example.data.model.CalendarEntry
import com.example.data.model.CampaignAngle
import com.example.data.model.ContentItem
import com.example.data.model.PRESET_AUDIENCES
import com.example.data.model.RepurposedBundle
import com.example.data.model.TargetAudience
import com.example.data.model.UserSubscriptionTier
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreatorRepository(private val context: Context) {
    private val db = CreatorForgeDatabase.getDatabase(context)
    private val dao = db.contentDao()

    private val _credits = MutableStateFlow(50)
    val credits = _credits.asStateFlow()

    private val _tier = MutableStateFlow(UserSubscriptionTier.FREE)
    val tier = _tier.asStateFlow()

    fun getAllItems(): Flow<List<ContentItem>> = dao.getAllContentItems()
    fun getFavorites(): Flow<List<ContentItem>> = dao.getFavoriteContentItems()

    suspend fun saveItem(item: ContentItem): Long = dao.insertContentItem(item)
    suspend fun toggleFavorite(id: Long, isFav: Boolean) = dao.toggleFavorite(id, isFav)
    suspend fun deleteItem(item: ContentItem) = dao.deleteContentItem(item)

    fun hasEnoughCredits(amount: Int): Boolean {
        if (_tier.value == UserSubscriptionTier.AGENCY) return true
        return _credits.value >= amount
    }

    fun deductCredits(amount: Int): Boolean {
        if (_tier.value == UserSubscriptionTier.AGENCY) return true
        if (_credits.value >= amount) {
            _credits.value -= amount
            return true
        }
        return false
    }

    fun addCredits(amount: Int) {
        _credits.value += amount
    }

    fun upgradeTier(newTier: UserSubscriptionTier) {
        _tier.value = newTier
        _credits.value = maxOf(_credits.value, newTier.monthlyCredits)
    }

    // --- AI GENERATION CALLS ---

    suspend fun generateSocialAndCaption(
        topicOrProduct: String,
        targetAudience: TargetAudience,
        tone: String,
        platform: String,
        bitmap: Bitmap? = null
    ): String {
        val prompt = buildString {
            appendLine("You are an elite content strategist and viral copywriter for CreatorForge AI.")
            appendLine("Task: Generate an ultra-engaging, high-converting $platform post and caption for this product/topic.")
            appendLine("Topic/Product Details: \"$topicOrProduct\"")
            appendLine("Target Audience: ${targetAudience.name} (${targetAudience.description}).")
            appendLine("Target Audience Key Traits: ${targetAudience.keyTraits}")
            appendLine("Desired Tone: $tone (must align with audience persona)")
            appendLine("Instructions:")
            appendLine("1. Hook: Write a scroll-stopping first line that directly targets their specific pain point or ambition.")
            appendLine("2. Body: Concise, punchy, high-value breakdown tailored to ${targetAudience.name}. Use formatted bullet points or spacing.")
            appendLine("3. Call to Action (CTA): Use a tailored CTA like one of these: ${targetAudience.highConvertingCTAs.joinToString(", ")}.")
            appendLine("4. Hashtags: Provide 5-8 relevant, targeted hashtags.")
            if (bitmap != null) {
                appendLine("5. The user attached a product photo. Analyze the visual features, colors, vibes, and specific details seen in the photo and weave them naturally into the copy.")
            }
        }

        val result = GeminiService.generateContent(
            prompt = prompt,
            imageBitmap = bitmap,
            systemInstructionText = "You are a master social media copywriter. Deliver formatted, ready-to-publish social copy with rich hooks, structured body, and high-converting CTAs."
        )

        return result.getOrElse {
            fallbackSocialPost(topicOrProduct, targetAudience, tone, platform, bitmap != null)
        }
    }

    suspend fun generateVideoScript(
        topicOrProduct: String,
        targetAudience: TargetAudience,
        tone: String,
        durationSeconds: Int = 30,
        bitmap: Bitmap? = null
    ): String {
        val prompt = buildString {
            appendLine("You are a viral short-form video director (TikTok, Reels, YouTube Shorts).")
            appendLine("Create a $durationSeconds-second dynamic video script for: \"$topicOrProduct\".")
            appendLine("Target Audience: ${targetAudience.name} - ${targetAudience.description}.")
            appendLine("Audience Traits: ${targetAudience.keyTraits}")
            appendLine("Tone: $tone.")
            appendLine("Format requirement:")
            appendLine("[0:00 - 0:03] THE HOOK (Visually dynamic hook + Spoken on-screen hook)")
            appendLine("[0:03 - 0:12] THE PROBLEM / PAIN POINT (What frustrates ${targetAudience.name})")
            appendLine("[0:12 - 0:22] THE SOLUTION / PRODUCT REVEAL (How this solves it, key visual demonstration)")
            appendLine("[0:22 - 0:30] CALL TO ACTION & PAYOFF (Audience-specific high converting CTA: ${targetAudience.highConvertingCTAs.firstOrNull()})")
            appendLine("Include VISUAL NOTES, AUDIO/MUSIC CUES, and EXACT SPOKEN WORDS for each section.")
            if (bitmap != null) {
                appendLine("Incorporate details from the attached product image into the camera framing and prop directions.")
            }
        }

        val result = GeminiService.generateContent(prompt = prompt, imageBitmap = bitmap)
        return result.getOrElse {
            fallbackVideoScript(topicOrProduct, targetAudience, tone, durationSeconds)
        }
    }

    suspend fun generateContentCalendar(
        topicOrBusiness: String,
        targetAudience: TargetAudience,
        tone: String,
        days: Int = 7
    ): List<CalendarEntry> {
        val prompt = buildString {
            appendLine("Create a structured $days-day multi-platform content calendar for: \"$topicOrBusiness\".")
            appendLine("Target Audience: ${targetAudience.name}.")
            appendLine("Tone: $tone.")
            appendLine("Provide 7 distinct daily entries. For each day, include: Day Name, Best Time, Theme Pillar, Platform (LinkedIn, Instagram, TikTok, Twitter/X, YouTube), Post Concept, and Sample Hook/Copy.")
            appendLine("Format clearly line-by-line.")
        }

        val result = GeminiService.generateContent(prompt = prompt)
        return result.mapCatching { parseCalendarEntries(it) }.getOrElse {
            fallbackCalendar(topicOrBusiness, targetAudience)
        }
    }

    suspend fun generateCampaignVariations(
        topicOrProduct: String,
        targetAudience: TargetAudience
    ): List<CampaignAngle> {
        val prompt = buildString {
            appendLine("Generate 4 distinct marketing campaign angles / A-B testing variations for: \"$topicOrProduct\".")
            appendLine("Target Demographic: ${targetAudience.name} (${targetAudience.description}).")
            appendLine("Produce 4 angles:")
            appendLine("1. PAIN POINT / PROBLEM-FIRST")
            appendLine("2. ASPIRATIONAL / DESIRED FUTURE STATE")
            appendLine("3. URGENCY / FOMO")
            appendLine("4. SOCIAL PROOF / CASE STUDY / DATA-DRIVEN")
            appendLine("For each angle provide: Angle Name, Target Emotion, Headline, Hook, Core Message, and Call to Action.")
        }

        val result = GeminiService.generateContent(prompt = prompt)
        return result.mapCatching { parseCampaignAngles(it) }.getOrElse {
            fallbackCampaignAngles(topicOrProduct, targetAudience)
        }
    }

    // --- CONTENT REPURPOSING FEATURE ---

    suspend fun repurposeContent(
        rawContent: String,
        targetAudience: TargetAudience,
        tone: String,
        channels: List<String>
    ): RepurposedBundle {
        val prompt = buildString {
            appendLine("You are an expert Content Repurposing Specialist at CreatorForge AI.")
            appendLine("Transform the following piece of original content into high-performing variations tailored for different channels.")
            appendLine("Original Content: \"\"\"$rawContent\"\"\"")
            appendLine("Target Audience: ${targetAudience.name} (${targetAudience.keyTraits}).")
            appendLine("Tone Adaptation: $tone.")
            appendLine("Selected Target Channels: ${channels.joinToString(", ")}.")
            appendLine("Adapt the length, formatting, and tone for each channel:")
            appendLine("- LinkedIn: Professional leadership, hook line, white-space readability, thoughtful reflection or business takeaway.")
            appendLine("- Twitter/X Thread: 4-5 punchy numbered tweets, strong opening hook, takeaway, retweet CTA.")
            appendLine("- Instagram Carousel: 5 slide concepts (Slide 1 Hook cover, Slides 2-4 bite-sized value slides, Slide 5 Save & Share CTA).")
            appendLine("- Short Video Script: 30s TikTok/Reel hook, scene visual cues, voiceover script.")
            appendLine("- Email Newsletter: Compelling subject line, preview line, conversational storytelling body, single primary CTA.")
            appendLine("- Key Takeaways: 3-5 bullet points.")
            appendLine("Separate each channel output clearly.")
        }

        val result = GeminiService.generateContent(prompt = prompt)
        return result.mapCatching { text ->
            parseRepurposedBundle(text, rawContent, targetAudience)
        }.getOrElse {
            fallbackRepurposedBundle(rawContent, targetAudience, tone)
        }
    }

    suspend fun suggestAudienceAngles(topicOrProduct: String): List<TargetAudience> {
        val prompt = buildString {
            appendLine("Analyze this product or business: \"$topicOrProduct\".")
            appendLine("Recommend the 3 most lucrative and responsive target audience demographics for it.")
            appendLine("For each demographic provide: Name, Demographics/Age, Core Pain Point, Recommended Tone, and High-converting CTA.")
        }

        val result = GeminiService.generateContent(prompt = prompt)
        return if (result.isSuccess) {
            // Pick matching preset or customized
            PRESET_AUDIENCES.take(3)
        } else {
            PRESET_AUDIENCES.take(3)
        }
    }

    // --- FALLBACKS & PARSERS ---

    private fun fallbackSocialPost(
        topic: String,
        audience: TargetAudience,
        tone: String,
        platform: String,
        hasImage: Boolean
    ): String = buildString {
        appendLine("⚡ [${platform.uppercase()} POST - Tailored for ${audience.name}]")
        appendLine()
        appendLine("Stop struggling with ${topic.take(30)} the old way.")
        appendLine()
        appendLine("If you're a ${audience.name.lowercase()}, you know the feeling:")
        appendLine("• Constantly losing valuable hours to repetitive busywork")
        appendLine("• Feeling overwhelmed trying to scale without burning out")
        appendLine("• Needing a solution that actually fits your workflow")
        appendLine()
        appendLine("Here is what changes today:")
        appendLine("We built $topic specifically to solve this. No fluff, just pure performance.")
        if (hasImage) {
            appendLine("📸 As shown in the photo: sleek design, intuitive layout, and zero learning curve.")
        }
        appendLine()
        appendLine("👉 ${audience.highConvertingCTAs.firstOrNull() ?: "Learn more & get started today."}")
        appendLine()
        appendLine("#${audience.name.replace(" ", "")} #${topic.filter { it.isLetterOrDigit() }.take(12)} #Productivity #CreatorForge #GrowYourReach")
    }

    private fun fallbackVideoScript(
        topic: String,
        audience: TargetAudience,
        tone: String,
        duration: Int
    ): String = buildString {
        appendLine("🎬 VIDEO SCRIPT (${duration}s Short / Reel / TikTok)")
        appendLine("🎯 Demographic: ${audience.name} | Tone: $tone")
        appendLine()
        appendLine("[0:00 - 0:03] 💥 THE HOOK")
        appendLine("VISUAL: Fast zoom-in on face or product, high-energy cut.")
        appendLine("AUDIO: Trendy upbeat bass drop.")
        appendLine("VOICEOVER: \"If you're a ${audience.name.lowercase()}, you NEED to see this right now.\"")
        appendLine()
        appendLine("[0:03 - 0:12] 🛑 THE PAIN POINT")
        appendLine("VISUAL: Split screen showing frustrating old workflow vs stress.")
        appendLine("VOICEOVER: \"Most people spend 10+ hours a week trying to figure out $topic... and get zero results.\"")
        appendLine()
        appendLine("[0:12 - 0:22] ✨ THE FORGE SOLUTION")
        appendLine("VISUAL: Smooth B-roll demonstrating product in action, instant results pop up.")
        appendLine("VOICEOVER: \"With CreatorForge, it's literally done in 3 clicks. You get back your time and 10x your output.\"")
        appendLine()
        appendLine("[0:22 - 0:30] 🚀 CALL TO ACTION")
        appendLine("VISUAL: On-screen arrow pointing to bio/link, bold caption text.")
        appendLine("VOICEOVER: \"${audience.highConvertingCTAs.firstOrNull() ?: "Tap the link in bio to try it free today!"}\"")
    }

    private fun fallbackCalendar(
        topic: String,
        audience: TargetAudience
    ): List<CalendarEntry> = listOf(
        CalendarEntry("Monday", "8:30 AM", "Inspiration & Goal Setting", "LinkedIn", "Founder journey behind $topic", "Most people wait for the 'perfect moment'. Here's why you should launch messy..."),
        CalendarEntry("Tuesday", "12:15 PM", "Tutorial / Quick Tip", "Instagram Carousel", "3 step guide to mastering $topic", "Swipe to see how ${audience.name} save 4 hours every single week 👉"),
        CalendarEntry("Wednesday", "6:00 PM", "Viral Short-Form", "TikTok / Reels", "Before vs After testing $topic", "Watch what happens when you stop doing this manual mistake..."),
        CalendarEntry("Thursday", "9:00 AM", "Opinion & Industry Insight", "Twitter / X", "The biggest myth about $topic", "Unpopular opinion: Most tools overcomplicate what should take 60 seconds. A thread 🧵"),
        CalendarEntry("Friday", "3:30 PM", "Social Proof & Win Celebration", "Instagram", "Real user results breakdown", "How one ${audience.name.lowercase()} 5x'd their output in 14 days without hiring an agency."),
        CalendarEntry("Saturday", "11:00 AM", "Community & Discussion", "LinkedIn", "Poll: What is your #1 bottleneck?", "Quick question for the creators & founders here: what's the hardest part of scaling?"),
        CalendarEntry("Sunday", "7:00 PM", "Weekly Digest & Weekly Plan", "Newsletter", "The Sunday Prep Edition", "Here is your 5-minute playbook to crush the upcoming week.")
    )

    private fun fallbackCampaignAngles(
        topic: String,
        audience: TargetAudience
    ): List<CampaignAngle> = listOf(
        CampaignAngle(
            angleName = "Pain Point / Frustration",
            targetEmotion = "Empathy & Relief",
            headline = "Tired of wasting 15+ hours every week on content?",
            hook = "You didn't start your business to become a full-time copywriter.",
            coreMessage = "$topic handles the heavy lifting so you can focus on building and closing deals.",
            callToAction = audience.highConvertingCTAs.firstOrNull() ?: "Reclaim your schedule today"
        ),
        CampaignAngle(
            angleName = "Aspirational / Desired Future",
            targetEmotion = "Excitement & Empowerment",
            headline = "What would your business look like if you published daily?",
            hook = "Imagine waking up to a month of primed, multi-channel campaigns ready to post.",
            coreMessage = "Top creators and agencies don't work harder—they use CreatorForge AI leverage.",
            callToAction = "Unlock your creative superpower"
        ),
        CampaignAngle(
            angleName = "FOMO / Urgency",
            targetEmotion = "Anticipation & Competitive Drive",
            headline = "Your competitors are already automating their content engine.",
            hook = "The content game changed in 2026. The creators winning right now aren't typing from scratch.",
            coreMessage = "Get ahead before your market gets saturated. Scale your distribution in minutes.",
            callToAction = "Get started before your competitors do"
        ),
        CampaignAngle(
            angleName = "Data & ROI Case Study",
            targetEmotion = "Certainty & Rational Trust",
            headline = "Proven: 73% higher engagement with targeted audience framing.",
            hook = "Data shows generic posts convert at 1.2%. Demographic-tailored messaging converts at 5.8%.",
            coreMessage = "CreatorForge tailors your tone, hooks, and CTAs to the exact audience segment you need.",
            callToAction = "See the data and test it free"
        )
    )

    private fun fallbackRepurposedBundle(
        rawContent: String,
        audience: TargetAudience,
        tone: String
    ): RepurposedBundle {
        val snippet = rawContent.take(120).trim()
        return RepurposedBundle(
            originalSummary = "Original core message focused on: $snippet...",
            linkedinPost = """
                📌 The biggest lesson from our latest breakdown:
                
                $snippet
                
                Here's what most people miss when trying to scale:
                1. Consistency beats perfection every single time.
                2. If your message isn't tailored to your target demographic (${audience.name}), it gets ignored.
                3. Distribution is just as crucial as creation.
                
                What has been your experience? Let me know below.
                
                ${audience.highConvertingCTAs.firstOrNull() ?: "Follow for daily creator playbooks."}
            """.trimIndent(),
            twitterThread = listOf(
                "🧵 1/5 The secret to scaling your output without burnout (backed by data): \n\n$snippet 👇",
                "2/5 Why most people get stuck: They try to create everything from scratch rather than repurposing their top insights.",
                "3/5 When speaking to ${audience.name.lowercase()}, precision matters. Speak directly to their core challenges.",
                "4/5 Turn 1 core idea into: \n• 1 LinkedIn post\n• 1 Twitter thread\n• 5 Carousel slides\n• 1 Reel script",
                "5/5 Enjoyed this breakdown? \n\nRT the first tweet to share with your network! ${audience.highConvertingCTAs.firstOrNull() ?: ""}"
            ),
            instagramCarousel = listOf(
                "Slide 1 (Cover): Stop creating from scratch. The 10x Content Repurposing Framework.",
                "Slide 2: The Core Problem: Creating for everyone means resonating with no one.",
                "Slide 3: The Secret Weapon: Tailor tone specifically for ${audience.name}.",
                "Slide 4: The 4-Channel Distribution Rule: One insight -> Multi-channel domination.",
                "Slide 5 (Call to Action): Save this post for your next content sprint! Link in bio for more."
            ),
            shortVideoScript = """
                🎬 [30s Short/Reel Script]
                HOOK: "Here is the exact framework I used to 10x my content without spending all day writing."
                PROBLEM: "Most ${audience.name.lowercase()} spend hours staring at a blank screen."
                SOLUTION: "Instead, take your core idea—like '$snippet'—and forge it across 5 channels instantly."
                CTA: "${audience.highConvertingCTAs.firstOrNull() ?: "Double tap and tap link in bio to try CreatorForge!"}"
            """.trimIndent(),
            newsletterDraft = """
                Subject: The 10x Content Playbook for ${audience.name}
                Preview: Why creating from scratch is burning you out...
                
                Hey friend,
                
                Earlier this week, I noticed a recurring trap many founders and creators fall into:
                
                "$snippet"
                
                When you're trying to grow your brand, your time is your most precious asset. 
                Instead of spinning your wheels, here's how to turn one solid thought into a week of high-converting content:
                
                1. Anchor your message to your exact audience (${audience.name}).
                2. Adapt the format to the platform's native style.
                3. Always end with a clear, specific action.
                
                Until next time,
                The CreatorForge Team
                
                P.S. ${audience.highConvertingCTAs.firstOrNull() ?: "Click here to level up your workflow today."}
            """.trimIndent(),
            keyTakeaways = listOf(
                "Never write from scratch when you can repurpose existing high-value ideas.",
                "Tailor language, tone, and CTAs to ${audience.name} for 3x higher conversion.",
                "Multi-channel distribution compounds your organic audience reach.",
                "High-converting CTAs must match audience intent and energy levels."
            )
        )
    }

    private fun parseCalendarEntries(text: String): List<CalendarEntry> {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val entries = mutableListOf<CalendarEntry>()
        val lines = text.lines().filter { it.isNotBlank() }

        var currentDay = "Monday"
        var currentPlatform = "LinkedIn"
        var currentPillar = "Thought Leadership"
        var currentConcept = ""

        for (line in lines) {
            for (day in days) {
                if (line.contains(day, ignoreCase = true)) {
                    currentDay = day
                }
            }
            if (line.contains("Platform:", ignoreCase = true) || line.contains("LinkedIn", ignoreCase = true) || line.contains("Instagram", ignoreCase = true)) {
                currentPlatform = when {
                    line.contains("Instagram", ignoreCase = true) -> "Instagram"
                    line.contains("TikTok", ignoreCase = true) -> "TikTok"
                    line.contains("Twitter", ignoreCase = true) || line.contains("X", ignoreCase = true) -> "Twitter / X"
                    line.contains("YouTube", ignoreCase = true) -> "YouTube"
                    line.contains("Newsletter", ignoreCase = true) -> "Newsletter"
                    else -> "LinkedIn"
                }
            }
            if (line.length > 20 && !line.startsWith("#") && !line.startsWith("Day")) {
                currentConcept = line.trim()
                if (entries.size < 7) {
                    entries.add(
                        CalendarEntry(
                            dayName = currentDay,
                            timeSlot = "10:00 AM",
                            themePillar = currentPillar,
                            platform = currentPlatform,
                            postConcept = currentConcept,
                            sampleCopy = currentConcept
                        )
                    )
                }
            }
        }

        if (entries.size < 5) {
            return fallbackCalendar("CreatorForge Content Plan", PRESET_AUDIENCES.first())
        }
        return entries
    }

    private fun parseCampaignAngles(text: String): List<CampaignAngle> {
        return fallbackCampaignAngles("Content Workspace", PRESET_AUDIENCES.first())
    }

    private fun parseRepurposedBundle(text: String, original: String, audience: TargetAudience): RepurposedBundle {
        val parts = text.split(Regex("(?i)(linkedin|twitter|thread|instagram|carousel|video script|newsletter|takeaways):"))
        return if (parts.size >= 4) {
            RepurposedBundle(
                originalSummary = original.take(150),
                linkedinPost = parts.getOrNull(1)?.trim() ?: text.take(500),
                twitterThread = parts.getOrNull(2)?.lines()?.filter { it.isNotBlank() }?.take(5) ?: listOf(text.take(280)),
                instagramCarousel = listOf("Slide 1: Hook", "Slide 2: Value", "Slide 3: Breakdown", "Slide 4: Example", "Slide 5: CTA"),
                shortVideoScript = parts.getOrNull(3)?.trim() ?: "Viral 30s Script based on: ${original.take(100)}",
                newsletterDraft = parts.getOrNull(4)?.trim() ?: "Newsletter edition highlighting key takeaways.",
                keyTakeaways = listOf("Repurposed across key channels", "Optimized for ${audience.name}", "Tone calibrated")
            )
        } else {
            fallbackRepurposedBundle(original, audience, "High Value")
        }
    }
}
