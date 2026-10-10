package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
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
import com.example.data.remote.FirebaseManager
import com.example.data.repository.CreatorRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkspaceTab {
    STUDIO_FORGE,
    REPURPOSE,
    PLAN,
    LIBRARY
}

enum class StudioFormat(val displayName: String, val creditCost: Int) {
    SOCIAL_POST("Social Post", 2),
    VIDEO_SCRIPT("Video Script", 4),
    CALENDAR("Content Plan", 6),
    CAMPAIGN("Campaign Angles", 3)
}

enum class PlanSubTab {
    CALENDAR,
    CAMPAIGNS
}

class CreatorForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreatorRepository(application)

    init {
        FirebaseManager.initAuthListener()
    }

    val currentUser: StateFlow<FirebaseUser?> = FirebaseManager.currentUser

    val credits: StateFlow<Int> = repository.credits
    val tier: StateFlow<UserSubscriptionTier> = repository.tier

    val savedItems: StateFlow<List<ContentItem>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Workspace UI State
    private val _currentTab = MutableStateFlow(WorkspaceTab.STUDIO_FORGE)
    val currentTab = _currentTab.asStateFlow()

    private val _selectedFormat = MutableStateFlow(StudioFormat.SOCIAL_POST)
    val selectedFormat = _selectedFormat.asStateFlow()

    private val _planSubTab = MutableStateFlow(PlanSubTab.CALENDAR)
    val planSubTab = _planSubTab.asStateFlow()

    private val _selectedAudience = MutableStateFlow(PRESET_AUDIENCES[0])
    val selectedAudience = _selectedAudience.asStateFlow()

    private val _selectedTone = MutableStateFlow("Direct & High Value")
    val selectedTone = _selectedTone.asStateFlow()

    // Studio Input
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

    private val _generatedResult = MutableStateFlow<String?>(null)
    val generatedResult = _generatedResult.asStateFlow()

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

    private val _showAccountDialog = MutableStateFlow(false)
    val showAccountDialog = _showAccountDialog.asStateFlow()

    init {
        // Pre-populate sample calendar and campaign angles
        viewModelScope.launch {
            _generatedCalendar.value = repository.generateContentCalendar(
                "CreatorForge Minimal Workspace",
                PRESET_AUDIENCES[0],
                "Engaging"
            )
            _generatedCampaignAngles.value = repository.generateCampaignVariations(
                "CreatorForge Minimal Workspace",
                PRESET_AUDIENCES[0]
            )
        }
    }

    // Setters
    fun setTab(tab: WorkspaceTab) { _currentTab.value = tab }
    fun setSelectedFormat(format: StudioFormat) { _selectedFormat.value = format }
    fun setPlanSubTab(subTab: PlanSubTab) { _planSubTab.value = subTab }
    fun setAudience(audience: TargetAudience) { _selectedAudience.value = audience }
    fun setTone(tone: String) { _selectedTone.value = tone }
    fun setIdeaInput(text: String) { _ideaInput.value = text }
    fun setSelectedImage(bitmap: Bitmap?) { _selectedImageBitmap.value = bitmap }
    fun setRepurposeContent(text: String) { _repurposeRawContent.value = text }
    fun setRepurposeTone(tone: String) { _repurposeTone.value = tone }
    fun setShowPaywall(show: Boolean) { _showPaywallDialog.value = show }
    fun setShowAudiencePicker(show: Boolean) { _showAudiencePicker.value = show }
    fun setShowAccountDialog(show: Boolean) { _showAccountDialog.value = show }
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

    // --- GOOGLE SIGN-IN ---
    fun signInWithGoogle(activity: Activity) {
        FirebaseManager.signInWithGoogle(
            activity = activity,
            scope = viewModelScope,
            onSuccess = {
                _statusMessage.value = "Welcome, ${it.displayName ?: "Creator"}!"
                _showAccountDialog.value = false
            },
            onError = { err ->
                if (err != "Sign-in dismissed") {
                    _statusMessage.value = "Sign-in: $err"
                }
            }
        )
    }

    fun signOut(context: Context) {
        FirebaseManager.signOut(context, viewModelScope) {
            _statusMessage.value = "Signed out"
            _showAccountDialog.value = false
        }
    }

    // --- MINIMALIST UNIFIED FORGE ACTION ---
    fun forgeContent() {
        val input = _ideaInput.value.trim()
        if (input.isBlank() && _selectedImageBitmap.value == null) {
            _statusMessage.value = "Please enter a topic or attach a photo"
            return
        }

        val format = _selectedFormat.value
        if (!repository.hasEnoughCredits(format.creditCost)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.deductCredits(format.creditCost)
            val effectiveTopic = if (input.isNotBlank()) input else "Product Showcase"

            when (format) {
                StudioFormat.SOCIAL_POST -> {
                    val result = repository.generateSocialAndCaption(
                        topicOrProduct = effectiveTopic,
                        targetAudience = _selectedAudience.value,
                        tone = _selectedTone.value,
                        platform = "Multi-Platform Post",
                        bitmap = _selectedImageBitmap.value
                    )
                    _generatedResult.value = result
                    repository.saveItem(
                        ContentItem(
                            title = "Post: ${effectiveTopic.take(35)}",
                            type = "social_post",
                            platform = "social",
                            originalInput = effectiveTopic,
                            targetAudience = _selectedAudience.value.name,
                            tone = _selectedTone.value,
                            content = result,
                            hasImage = _selectedImageBitmap.value != null
                        )
                    )
                }
                StudioFormat.VIDEO_SCRIPT -> {
                    val result = repository.generateVideoScript(
                        topicOrProduct = effectiveTopic,
                        targetAudience = _selectedAudience.value,
                        tone = _selectedTone.value,
                        durationSeconds = 30,
                        bitmap = _selectedImageBitmap.value
                    )
                    _generatedResult.value = result
                    repository.saveItem(
                        ContentItem(
                            title = "Script (30s): ${effectiveTopic.take(35)}",
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
                StudioFormat.CALENDAR -> {
                    val calendar = repository.generateContentCalendar(
                        topicOrBusiness = effectiveTopic,
                        targetAudience = _selectedAudience.value,
                        tone = _selectedTone.value
                    )
                    _generatedCalendar.value = calendar
                    _currentTab.value = WorkspaceTab.PLAN
                    _planSubTab.value = PlanSubTab.CALENDAR
                }
                StudioFormat.CAMPAIGN -> {
                    val angles = repository.generateCampaignVariations(
                        topicOrProduct = effectiveTopic,
                        targetAudience = _selectedAudience.value
                    )
                    _generatedCampaignAngles.value = angles
                    _currentTab.value = WorkspaceTab.PLAN
                    _planSubTab.value = PlanSubTab.CAMPAIGNS
                }
            }

            _isLoading.value = false
        }
    }

    // --- REPURPOSING ACTION ---
    fun executeRepurpose() {
        val raw = _repurposeRawContent.value.trim()
        if (raw.isBlank()) {
            _statusMessage.value = "Please paste text to repurpose"
            return
        }

        if (!repository.hasEnoughCredits(5)) {
            _showPaywallDialog.value = true
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            repository.deductCredits(5)

            val bundle = repository.repurposeContent(
                rawContent = raw,
                targetAudience = _selectedAudience.value,
                tone = _repurposeTone.value,
                channels = _selectedChannels.value.toList()
            )

            _repurposedBundle.value = bundle
            _isLoading.value = false

            repository.saveItem(
                ContentItem(
                    title = "Repurposed: ${raw.take(35)}...",
                    type = "repurposed",
                    platform = "multi",
                    originalInput = raw,
                    targetAudience = _selectedAudience.value.name,
                    tone = _repurposeTone.value,
                    content = "LinkedIn:\n${bundle.linkedinPost}\n\nNewsletter:\n${bundle.newsletterDraft}"
                )
            )
        }
    }

    fun suggestAudienceAnglesForTopic() {
        val input = _ideaInput.value.trim()
        val topic = if (input.isNotBlank()) input else "Content Creator Brand"

        viewModelScope.launch {
            val suggested = repository.suggestAudienceAngles(topic)
            if (suggested.isNotEmpty()) {
                _selectedAudience.value = suggested.first()
                _statusMessage.value = "Calibrated for: ${suggested.first().name}"
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
        _statusMessage.value = "+$amount credits added"
        _showPaywallDialog.value = false
    }

    fun upgradeTier(tier: UserSubscriptionTier) {
        repository.upgradeTier(tier)
        _statusMessage.value = "Upgraded to ${tier.displayName}"
        _showPaywallDialog.value = false
    }
}
