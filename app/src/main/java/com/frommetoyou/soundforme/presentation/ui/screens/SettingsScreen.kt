package com.frommetoyou.soundforme.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frommetoyou.soundforme.R

@Composable
fun SettingScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.home_background7),
        contentDescription = "background",
        modifier = Modifier
            .offset(x = 100.dp)
            .wrapContentSize(unbounded = true)
            .fillMaxSize()
        ,
        contentScale = ContentScale.Crop,
    )
    Column(modifier = modifier.fillMaxSize()) {
        Text(text = "Customiza tus preferencias!", fontSize = 25.sp)
        Row {
            Column(modifier = modifier.fillMaxSize()) {
                Text(text = "Whistle or Clap!", fontSize = 12.sp)
                Text(text = "Choose the sound you want to use", fontSize = 12.sp)
            }
            Button(
                onClick = {

                }
            ) {
                Text(text = "Whistle", fontSize = 25.sp)

            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun SettingItem(modifier: Modifier = Modifier) {

}