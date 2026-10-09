package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CalendarEntry
import com.example.data.model.CampaignAngle
import com.example.data.model.ContentItem
import com.example.data.model.PRESET_AUDIENCES
import com.example.data.model.RepurposedBundle
import com.example.data.model.TargetAudience
import com.example.data.model.UserSubscriptionTier
import com.example.data.repository.CreatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkspaceTab {
    STUDIO_FORGE,
    REPURPOSE,
    CALENDAR,
    CAMPAIGNS,
    LIBRARY
}

enum class GenerationMode {
    TEXT_INPUT,
    PRODUCT_PHOTO
}

class CreatorForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreatorRepository(application)

    val credits: StateFlow<Int> = repository.credits
    val tier: StateFlow<UserSubscriptionTier> = repository.tier

    val savedItems: StateFlow<List<ContentItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workspace UI State
    private val _currentTab = MutableStateFlow(WorkspaceTab.STUDIO_FORGE)
    val currentTab = _currentTab.asStateFlow()

    private val _generationMode = MutableStateFlow(GenerationMode.TEXT_INPUT)
    val generationMode = _generationMode.asStateFlow()

    private val _selectedAudience = MutableStateFlow(PRESET_AUDIENCES[0])
    val selectedAudience = _selectedAudience.asStateFlow()

    private val _selectedTone = MutableStateFlow("Direct & High Value")
    val selectedTone = _selectedTone.asStateFlow()

    // Inputs
    private val _ideaInput = MutableStateFlow("")
    val ideaInput = _ideaInput.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap = _selectedImageBitmap.asStateFlow()

    // Repurposing feature input
    private val _repurposeRawContent = MutableStateFlow("")
    val repurposeRawContent = _repurposeRawContent.asStateFlow()

    private val _repurposeTone = MutableStateFlow("Conversational & Authentic")
    val repurposeTone = _repurposeTone.asStateFlow()

    private val _selectedChannels = MutableStateFlow(setOf("LinkedIn", "Twitter/X Thread", "Instagram Carousel", "Short Video Script", "Email Newsletter"))
    val selectedChannels = _selectedChannels.asStateFlow()

    // Generation Results
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage = _statusMessage.asStateFlow()

    private val _generatedSocialPost = MutableStateFlow<String?>(null)
    val generatedSocialPost = _generatedSocialPost.asStateFlow()

    private val _generatedVideoScript = MutableStateFlow<String?>(null)
    val generatedVideoScript = _generatedVideoScript.asStateFlow()

    private val _generatedCalendar = MutableStateFlow<List<CalendarEntry>>(emptyList())
    val generatedCalendar = _generatedCalendar.asStateFlow()

    private val _generatedCampaignAngles = MutableStateFlow<List<CampaignAngle>>(emptyList())
    val generatedCampaignAngles = _generatedCampaignAngles.asStateFlow()

    private val _repurposedBundle = MutableStateFlow<RepurposedBundle?>(null)
    val repurposedBundle = _repurposedBundle.asStateFlow()

    // Dialogs
    private val _showPaywallDialog = MutableStateFlow(false)
    val showPaywallDialog = _showPaywallDialog.asStateFlow()

    private val _showAudiencePicker = MutableStateFlow(false)
    val showAudiencePicker = _showAudiencePicker.asStateFlow()

    init {
        // Seed default calendar and angles for immediate rich preview
        viewModelScope.launch {
            _generatedCalendar.value = repository.generateContentCalendar(
                "CreatorForge AI Studio",
                PRESET_AUDIENCES[0],
                "Engaging"
            )
            _generatedCampaignAngles.value = repository.generateCampaignVariations(
                "CreatorForge AI Studio",
                PRESET_AUDIENCES[0]
            )
        }
    }

    // Setters
    fun setTab(tab: WorkspaceTab) { _currentTab.value = tab }
    fun setGenerationMode(mode: GenerationMode) { _generationMode.value = mode }
    fun setAudience(audience: TargetAudience) { _selectedAudience.value = audience }
    fun setTone(tone: String) { _selectedTone.value = tone }
    fun setIdeaInput(text: String) { _ideaInput.value = text }
    fun setSelectedImage(bitmap: Bitmap?) { _selectedImageBitmap.value = bitmap }
    fun setRepurposeContent(text: String) { _repurposeRawContent.value = text }
    fun setRepurposeTone(tone: String) { _repurposeTone.value = tone }
    fun setShowPaywall(show: Boolean) { _showPaywallDialog.value = show }
    fun setShowAudiencePicker(show: Boolean) { _showAudiencePicker.value = show }
    fun clearStatusMessage() { _statusMessage.value = null }

    fun toggleChannel(channel: String) {
        val current = _selectedChannels.value.toMutableSet()
        if (current.contains(channel)) {
            if (current.size > 1) current.remove(channel)
        } else {
            current.add(channel)
        }
        _selectedChannels.value = current
    }

    // --- ACTIONS ---

    fun generateSocialPost(platform: String = "Instagram & LinkedIn") {
        val input = _ideaInput.value.trim()
        if (input.isBlank() && _selectedImageBitmap.value == null) {
            _statusMessage.value = "Please enter an idea, description, or pick a product photo!"
            return
        }

        if (!repository.hasEnoughCredits(2)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Forging $platform copy for ${_selectedAudience.value.name}..."
            repository.deductCredits(2)

            val effectiveTopic = if (input.isNotBlank()) input else "Product Photo Feature"
            val result = repository.generateSocialAndCaption(
                topicOrProduct = effectiveTopic,
                targetAudience = _selectedAudience.value,
                tone = _selectedTone.value,
                platform = platform,
                bitmap = _selectedImageBitmap.value
            )

            _generatedSocialPost.value = result
            _isLoading.value = false
            _statusMessage.value = "Generated successfully! (2 credits used)"

            // Auto-save to database
            repository.saveItem(
                ContentItem(
                    title = "$platform: ${effectiveTopic.take(35)}",
                    type = "social_post",
                    platform = platform.lowercase(),
                    originalInput = effectiveTopic,
                    targetAudience = _selectedAudience.value.name,
                    tone = _selectedTone.value,
                    content = result,
                    hasImage = _selectedImageBitmap.value != null
                )
            )
        }
    }

    fun generateVideoScript(durationSeconds: Int = 30) {
        val input = _ideaInput.value.trim()
        if (input.isBlank() && _selectedImageBitmap.value == null) {
            _statusMessage.value = "Please enter an idea or add a product photo!"
            return
        }

        if (!repository.hasEnoughCredits(4)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Drafting ${durationSeconds}s viral video script..."
            repository.deductCredits(4)

            val effectiveTopic = if (input.isNotBlank()) input else "Product Feature"
            val result = repository.generateVideoScript(
                topicOrProduct = effectiveTopic,
                targetAudience = _selectedAudience.value,
                tone = _selectedTone.value,
                durationSeconds = durationSeconds,
                bitmap = _selectedImageBitmap.value
            )

            _generatedVideoScript.value = result
            _isLoading.value = false
            _statusMessage.value = "Video script forged! (4 credits used)"

            repository.saveItem(
                ContentItem(
                    title = "Script (${durationSeconds}s): ${effectiveTopic.take(35)}",
                    type = "video_script",
                    platform = "shorts_reels",
                    originalInput = effectiveTopic,
                    targetAudience = _selectedAudience.value.name,
                    tone = _selectedTone.value,
                    content = result,
                    hasImage = _selectedImageBitmap.value != null
                )
            )
        }
    }

    fun generateCalendar() {
        val input = _ideaInput.value.trim()
        val effectiveTopic = if (input.isNotBlank()) input else "Weekly Brand Campaign"

        if (!repository.hasEnoughCredits(6)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Building 7-day content schedule..."
            repository.deductCredits(6)

            val calendar = repository.generateContentCalendar(
                topicOrBusiness = effectiveTopic,
                targetAudience = _selectedAudience.value,
                tone = _selectedTone.value
            )

            _generatedCalendar.value = calendar
            _isLoading.value = false
            _statusMessage.value = "Content Calendar ready! (6 credits used)"
            _currentTab.value = WorkspaceTab.CALENDAR
        }
    }

    fun generateCampaignAngles() {
        val input = _ideaInput.value.trim()
        val effectiveTopic = if (input.isNotBlank()) input else "Core Product Offer"

        if (!repository.hasEnoughCredits(3)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Forging 4 A/B testing campaign variations..."
            repository.deductCredits(3)

            val angles = repository.generateCampaignVariations(
                topicOrProduct = effectiveTopic,
                targetAudience = _selectedAudience.value
            )

            _generatedCampaignAngles.value = angles
            _isLoading.value = false
            _statusMessage.value = "Campaign angles generated! (3 credits used)"
            _currentTab.value = WorkspaceTab.CAMPAIGNS
        }
    }

    // --- REPURPOSING FEATURE (MANDATORY REQUIREMENT) ---

    fun executeRepurpose() {
        val raw = _repurposeRawContent.value.trim()
        if (raw.isBlank()) {
            _statusMessage.value = "Please paste or enter some content to repurpose!"
            return
        }

        if (!repository.hasEnoughCredits(5)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Repurposing across ${_selectedChannels.value.size} channels..."
            repository.deductCredits(5)

            val bundle = repository.repurposeContent(
                rawContent = raw,
                targetAudience = _selectedAudience.value,
                tone = _repurposeTone.value,
                channels = _selectedChannels.value.toList()
            )

            _repurposedBundle.value = bundle
            _isLoading.value = false
            _statusMessage.value = "Repurposed successfully! (5 credits used)"

            // Save bundle to library
            repository.saveItem(
                ContentItem(
                    title = "Repurposed: ${raw.take(35)}...",
                    type = "repurposed",
                    platform = "multi",
                    originalInput = raw,
                    targetAudience = _selectedAudience.value.name,
                    tone = _repurposeTone.value,
                    content = "LinkedIn:\n${bundle.linkedinPost}\n\nShort Script:\n${bundle.shortVideoScript}\n\nNewsletter:\n${bundle.newsletterDraft}"
                )
            )
        }
    }

    fun suggestAudienceAnglesForTopic() {
        val input = _ideaInput.value.trim()
        val topic = if (input.isNotBlank()) input else "Content Creator Brand"

        viewModelScope.launch {
            _statusMessage.value = "Analyzing top audience segments for '$topic'..."
            val suggested = repository.suggestAudienceAngles(topic)
            if (suggested.isNotEmpty()) {
                _selectedAudience.value = suggested.first()
                _statusMessage.value = "Switched to: ${suggested.first().name} (${suggested.first().keyTraits.take(30)}...)"
            }
        }
    }

    fun toggleFavorite(item: ContentItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun deleteItem(item: ContentItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun buyCredits(amount: Int) {
        repository.addCredits(amount)
        _statusMessage.value = "Added $amount credits to your workspace!"
        _showPaywallDialog.value = false
    }

    fun upgradeTier(tier: UserSubscriptionTier) {
        repository.upgradeTier(tier)
        _statusMessage.value = "Upgraded to ${tier.displayName} tier!"
        _showPaywallDialog.value = false
    }
}
