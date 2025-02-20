package com.frommetoyou.soundforme.presentation.ui.screens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.frommetoyou.soundforme.R
import com.frommetoyou.soundforme.domain.model.MusicItem
import com.frommetoyou.soundforme.domain.model.SettingConfig
import com.frommetoyou.soundforme.presentation.ui.util.UiText
import com.frommetoyou.soundforme.presentation.ui.util.findAndroidActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicSelectionScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val settings = viewModel.settings.collectAsState()
    var mediaPlayer: MediaPlayer? = null

    var musicList =
        viewModel.musicList.collectAsStateWithLifecycle(initialValue = listOf())

    var playingIndex by remember { mutableStateOf(-1) }
    var searchQuery by remember { mutableStateOf("") }
    var focusedSearch by remember { mutableStateOf(false) }
    val filteredItems = musicList.value.filter {
        it.name?.contains(
            searchQuery,
            ignoreCase = true
        ) ?: false
    }

    var selectedMusic by remember {
        mutableStateOf(MusicItem())
    }

    var permission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            READ_MEDIA_AUDIO
        } else {
            READ_EXTERNAL_STORAGE
        }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(true) {
        context.findAndroidActivity()?.contentResolver?.let {
            viewModel.setContentResolver(it)
        }
    }
    val musicPermission = rememberPermissionState(permission = permission)

    if (musicPermission.status.isGranted) {
        viewModel.setMusicPermission()
    }

    LaunchedEffect(musicList.value) {
        if (musicList.value.isNotEmpty()) {
            musicList.value.firstOrNull { it.id == settings.value.musicItem.id }
                ?.let {
                    selectedMusic = it
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (viewModel.musicPermissionGranted.collectAsState().value) {
            LaunchedEffect(true) {
                scope.launch {
                    viewModel.loadMusicList()
                }
            }
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxSize()
            ) {
                Column {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { focusedSearch = false },
                        active = focusedSearch,
                        onActiveChange = { focusedSearch = it },

                        modifier = modifier
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                                bottom = 12.dp
                            )
                            .fillMaxWidth(),

                        placeholder = { Text("Search") },

                        leadingIcon = {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        trailingIcon = {
                            if (focusedSearch)
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    modifier = Modifier.pointerInput(true) {
                                        detectTapGestures {
                                            searchQuery = ""
                                            focusedSearch = false
                                        }
                                    }
                                )
                        },
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        tonalElevation = 0.dp,
                    ) {
                        filteredItems.forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Max),
                                color = Color.Transparent,
                                onClick = {
                                    selectedMusic = item
                                    focusedSearch = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.name,
                                        fontSize = 15.sp,
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp,
                                )
                            )
                        }
                    }

                    LazyColumn(
                    ) {
                        items(musicList.value.size) { index ->
                            val music = musicList.value[index]
                            val isPlaying = playingIndex == index
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors =
                                if (music.id == settings.value.musicItem.id)
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    ) else if (selectedMusic.id == music.id)
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                else CardDefaults.cardColors()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .pointerInput(true) {
                                            detectTapGestures {
                                                if (selectedMusic != music)
                                                    selectedMusic = music
                                                else
                                                    selectedMusic =
                                                        MusicItem()
                                            }
                                        }
                                ) {
                                    val imageRes =
                                        if (isPlaying) R.drawable.ic_baseline_pause_circle_24
                                        else R.drawable.ic_baseline_play_circle_24

                                    RadioButton(
                                        selected = (music == selectedMusic || music.id == settings.value.musicItem.id),
                                        onClick = null,
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        colors = if (music.id != settings.value.musicItem.id)
                                            RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.onSecondary
                                            )
                                        else RadioButtonDefaults.colors(
                                            selectedColor =
                                            MaterialTheme.colorScheme.onTertiary
                                        )
                                    )
                                    Text(
                                        music.name,
                                        modifier = Modifier.weight(.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val image: Painter =
                                        painterResource(id = imageRes)
                                    Image(
                                        painter = image,
                                        contentDescription = UiText.StringResource(
                                            R.string.play
                                        )
                                            .asString(context),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clickable {
                                                mediaPlayer?.let {
                                                    it.pause()
                                                    it.stop()
                                                    mediaPlayer = null
                                                }
                                                if (isPlaying) {
                                                    playingIndex = -1
                                                    return@clickable
                                                }
                                                mediaPlayer =
                                                    MediaPlayer.create(
                                                        context,
                                                        music.uri.toUri()
                                                    )
                                                mediaPlayer?.start()
                                                playingIndex = index
                                            },
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            viewModel.saveSettings(
                                settings.value.copy(
                                    musicItem = SettingConfig().musicItem
                                )
                            )

                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            UiText.StringResource(R.string.default_alarm)
                                .asString(context)
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = (selectedMusic.id != -1L && selectedMusic != settings.value.musicItem),
                        colors = ButtonDefaults.buttonColors(containerColor = if (selectedMusic.id != -1L) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        onClick = {
                            viewModel.saveSettings(
                                settings.value.copy(
                                    musicItem = selectedMusic
                                )
                            )
                            navController.popBackStack()

                        }
                    ) {
                        Text(
                            UiText.StringResource(R.string.select)
                                .asString(context)
                        )
                    }
                }
            }
        } else {

            Card(
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        UiText.StringResource(R.string.permissions_1).asString(
                            LocalContext.current
                        )
                    )
                    Button(
                        modifier = Modifier.padding(top= 16.dp),
                        onClick = {
                        musicPermission.launchPermissionRequest()
                    }) {
                        Text(
                            UiText.StringResource(R.string.permissions_2)
                                .asString(
                                    LocalContext.current
                                )
                        )
                    }
                }
            }
        }
    }
}