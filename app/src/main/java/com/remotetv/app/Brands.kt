package com.remotetv.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Simple vector-drawn approximations of the app logos.
// To use the official artwork, put PNGs in res/drawable and swap these for Image(painterResource(...)).

@Composable
fun YouTubeLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(width = 34.dp, height = 24.dp).background(Color(0xFFFF0000), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(6.dp))
        Text("YouTube", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
    }
}

@Composable
fun PrimeLogo() {
    val blue = Color(0xFF00A8E1)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = blue)) { append("prime") }
                withStyle(SpanStyle(color = Color.White)) { append(" video") }
            },
            fontSize = 21.sp, fontWeight = FontWeight.Bold
        )
        Canvas(Modifier.width(72.dp).height(10.dp)) {
            drawArc(
                color = blue, startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(0f, -size.height), size = Size(size.width, size.height * 2f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SpotifyLogo() {
    val green = Color(0xFF1ED760)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(34.dp)) {
            drawCircle(green)
            val w = size.width
            listOf(0.34f to 0.72f, 0.26f to 0.69f, 0.18f to 0.66f).forEach { (r, cy) ->
                drawArc(
                    color = Color(0xFF0E0E10), startAngle = 215f, sweepAngle = 110f, useCenter = false,
                    topLeft = Offset(w * 0.5f - r * w, cy * w - r * w), size = Size(2 * r * w, 2 * r * w),
                    style = Stroke(width = w * 0.075f, cap = StrokeCap.Round)
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text("Spotify", color = green, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ShahidLogo() {
    Text("شاهد", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
}

@Composable
fun WatchItLogo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("W", color = Color(0xFFF6B800), fontSize = 36.sp, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
        Spacer(Modifier.width(6.dp))
        Text("WATCH IT", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
fun AppsLabel() {
    Text("Apps", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Medium)
}
