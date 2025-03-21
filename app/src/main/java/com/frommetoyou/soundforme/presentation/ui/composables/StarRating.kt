package com.frommetoyou.soundforme.presentation.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.frommetoyou.soundforme.R

@Composable
fun StarRating(rating: Float) {
    val fullStars = rating.toInt()
    val hasHalfStar = (rating - fullStars) >= 0.5f
    val totalStars = 5

    Row {
        // Draw full stars
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Full star",
                tint = Color.Yellow,
                modifier = Modifier.size(16.dp)
            )
        }

        // Draw half star if exists
        if (hasHalfStar) {
            Icon(
                painter = painterResource(R.drawable.ic_baseline_star_half_24),
                contentDescription = "Half star",
                tint = Color.Yellow,
                modifier = Modifier.size(16.dp)
            )
        }

        // Draw empty stars
        repeat(totalStars - fullStars - if (hasHalfStar) 1 else 0) {
            Icon(
                imageVector = Icons.TwoTone.Star,
                contentDescription = "Empty star",
                tint = Color.Yellow,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}