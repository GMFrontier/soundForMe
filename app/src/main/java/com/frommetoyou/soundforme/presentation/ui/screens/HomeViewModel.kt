package com.frommetoyou.soundforme.presentation.ui.screens

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frommetoyou.soundforme.domain.model.ActiveButton
import com.frommetoyou.soundforme.domain.model.MusicItem
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.domain.use_case.DetectorService
import com.frommetoyou.soundforme.domain.use_case.DetectorService.Actions
import com.frommetoyou.soundforme.domain.use_case.SettingsManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
class HomeViewModel(
    private val settingsManager: SettingsManager,
) : ViewModel() {
    private var contentResolver: ContentResolver? = null

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _musicList = MutableStateFlow(listOf<MusicItem>())
    val musicList: StateFlow<List<MusicItem>> = _musicList

    private val _musicPermissionGranted = MutableStateFlow(false)
    val musicPermissionGranted: StateFlow<Boolean> = _musicPermissionGranted

    private val _settings = MutableStateFlow(SettingConfig())
    val settings: StateFlow<SettingConfig> = _settings

    private val _openPrivacyPolicy = MutableStateFlow(false)
    val openPrivacyPolicy: StateFlow<Boolean> = _openPrivacyPolicy

    private val _permissionEvent = MutableStateFlow<Event>(Event.Idle)
    val permissionEvent: StateFlow<Event> = _permissionEvent.asStateFlow()

    private lateinit var micPermissionState: PermissionState
    private lateinit var notificationPermissionState: PermissionState

    init {
        CoroutineScope(Dispatchers.IO).launch {
            settingsManager.getSettings().collectLatest {
                _settings.value = it
            }
        }
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) visiblePermissionDialogQueue.add(
            permission
        )
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeAt(0)
    }

    fun setMusicPermission() {
        _musicPermissionGranted.value = true
    }

    fun loadMusicList() = viewModelScope.launch(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver?.let { resolver ->
            val cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            val musicList = mutableListOf<MusicItem>()
            cursor?.use {
                val idColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val uriColumn =
                    it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val uri = it.getString(uriColumn)

                    musicList.add(MusicItem(id, name, uri))
                }
                _musicList.value = musicList
            }
        }
    }

    fun setContentResolver(contentResolver: ContentResolver) {
        this.contentResolver = contentResolver
    }

    fun saveSettings(settings: SettingConfig) {
        viewModelScope.launch {
            _settings.value = settings
            settingsManager.saveSettings(settings)
        }
    }

    fun startDetector(context: Context) {

        viewModelScope.launch {
            if (settings.value.isPrivacyAccepted.not()) {
                _openPrivacyPolicy.value = true
                return@launch
            }

            if (micPermissionState.status.isGranted.not()) {
                _permissionEvent.value = Event.PermissionRequest
                return@launch
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                if (notificationPermissionState.status.isGranted.not()) {
                    _permissionEvent.value = Event.PermissionRequest
                    return@launch
                }

            saveSettings(
                settings.value.copy(
                    active = ActiveButton.On
                )
            )
            val intent = Intent(context, DetectorService::class.java).apply {
                action = Actions.START.toString()
            }
            context.startService(intent)
        }
    }

    fun stopDetector(context: Context) {
        saveSettings(
            settings.value.copy(
                active = ActiveButton.Off
            )
        )
        val intent = Intent(context, DetectorService::class.java).apply {
            action = Actions.STOP.toString()
        }
        context.stopService(intent)
    }

    fun setOpenPrivacyPolicy(b: Boolean) {
        _openPrivacyPolicy.value = b
    }

    fun setPermissionState(permissionState: PermissionState) {
        micPermissionState = permissionState
    }

    fun setNotifPermissionState(permissionState: PermissionState) {
        notificationPermissionState = permissionState
    }

    sealed class Event {
        object Idle : Event()
        object PermissionRequest : Event()
    }
}
