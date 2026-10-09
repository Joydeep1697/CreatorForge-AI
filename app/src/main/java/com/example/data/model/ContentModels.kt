package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_items")
data class ContentItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val type: String, // "social_post", "caption", "video_script", "content_calendar", "campaign_variation", "repurposed"
    val platform: String, // "linkedin", "twitter", "instagram", "tiktok", "youtube", "newsletter", "multi"
    val originalInput: String,
    val targetAudience: String,
    val tone: String,
    val content: String,
    val hashtags: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val hasImage: Boolean = false,
    val imageUri: String? = null
)

data class TargetAudience(
    val id: String,
    val name: String,
    val description: String,
    val keyTraits: String,
    val recommendedTone: String,
    val highConvertingCTAs: List<String>,
    val iconName: String
)

val PRESET_AUDIENCES = listOf(
    TargetAudience(
        id = "young_pros",
        name = "Young Professionals",
        description = "Ages 22-35, career-focused, ambitious, time-crunched, tech-savvy",
        keyTraits = "Values productivity, work-life balance, career advancement, and speed",
        recommendedTone = "Direct, sharp, inspiring, professional without jargon",
        highConvertingCTAs = listOf(
            "Save 5 hours this week -> Click here",
            "Level up your workflow today",
            "Try it free on your next lunch break"
        ),
        iconName = "work"
    ),
    TargetAudience(
        id = "eco_conscious",
        name = "Eco-Conscious Consumers",
        description = "Values sustainability, transparency, ethical sourcing, zero waste",
        keyTraits = "Scrutinizes greenwashing, cares about environmental footprint & long-term impact",
        recommendedTone = "Authentic, mindful, transparent, hopeful, purpose-driven",
        highConvertingCTAs = listOf(
            "Join 10,000+ conscious changemakers",
            "Make the eco-friendly switch today",
            "See our transparent carbon pledge"
        ),
        iconName = "eco"
    ),
    TargetAudience(
        id = "small_biz",
        name = "Small Business Owners & Solopreneurs",
        description = "Entrepreneurs, local retailers, agency owners juggling every role",
        keyTraits = "Extremely ROI-driven, cost-conscious, seeks leverage and practical automation",
        recommendedTone = "Pragmatic, relatable, results-backed, peer-to-peer",
        highConvertingCTAs = listOf(
            "Cut overhead costs by 40% starting now",
            "Claim your 14-day free agency trial",
            "Built for founders who wear all the hats"
        ),
        iconName = "store"
    ),
    TargetAudience(
        id = "gen_z",
        name = "Gen Z Creators & Digital Natives",
        description = "Ages 16-26, TikTok & Reels first, visually native, meme-aware",
        keyTraits = "Hates forced corporate ads, values raw authenticity, aesthetic, humor",
        recommendedTone = "Energetic, witty, conversational, concise, unfiltered",
        highConvertingCTAs = listOf(
            "Don't sleep on this — tap in below",
            "Drop a comment if you needed to hear this",
            "Link in bio before it sells out"
        ),
        iconName = "spark"
    ),
    TargetAudience(
        id = "b2b_execs",
        name = "Enterprise B2B Decision Makers",
        description = "Directors, VPs, C-level executives managing teams and budgets",
        keyTraits = "Risk-averse, metric-oriented, demands security, scalability, and measurable ROI",
        recommendedTone = "Authoritative, analytical, strategic, data-substantiated",
        highConvertingCTAs = listOf(
            "Schedule a 15-minute executive briefing",
            "Download the benchmark research report",
            "Calculate your enterprise ROI"
        ),
        iconName = "analytics"
    ),
    TargetAudience(
        id = "busy_parents",
        name = "Busy Parents & Families",
        description = "Managing households, children, schedules, looking for sanity and convenience",
        keyTraits = "Prioritizes family safety, convenience, stress relief, time-saving hacks",
        recommendedTone = "Empathetic, warm, reassuring, problem-solving, no-fluff",
        highConvertingCTAs = listOf(
            "Give yourself 30 quiet minutes back today",
            "Parent-tested, kid-approved — order now",
            "Simplify dinner time tonight"
        ),
        iconName = "family"
    ),
    TargetAudience(
        id = "fitness_enthusiasts",
        name = "Fitness & Wellness Achievers",
        description = "Athletes, gym-goers, wellness seekers focused on health and performance",
        keyTraits = "Driven by personal transformation, daily discipline, and peak energy",
        recommendedTone = "Motivating, empowering, science-backed, high-tempo",
        highConvertingCTAs = listOf(
            "Crush your PRs — start your 7-day challenge",
            "Fuel your peak performance today",
            "Join the movement and unlock your best self"
        ),
        iconName = "fitness"
    )
)

data class RepurposedBundle(
    val originalSummary: String,
    val linkedinPost: String,
    val twitterThread: List<String>,
    val instagramCarousel: List<String>,
    val shortVideoScript: String,
    val newsletterDraft: String,
    val keyTakeaways: List<String>
)

data class CalendarEntry(
    val dayName: String,
    val timeSlot: String,
    val themePillar: String,
    val platform: String,
    val postConcept: String,
    val sampleCopy: String,
    var isScheduled: Boolean = false
)

data class CampaignAngle(
    val angleName: String, // e.g. "Pain Point / Frustration", "Aspirational / Desired Future", "FOMO / Urgency", "Data & Case Study"
    val headline: String,
    val hook: String,
    val coreMessage: String,
    val callToAction: String,
    val targetEmotion: String
)

enum class UserSubscriptionTier(val displayName: String, val monthlyCredits: Int, val price: String) {
    FREE("Free Starter", 50, "$0 / mo"),
    PRO("Creator Pro", 1000, "$19 / mo"),
    AGENCY("Agency Scale", 5000, "$49 / mo")
}
