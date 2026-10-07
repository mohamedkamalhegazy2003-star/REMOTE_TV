package com.remotetv.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot

object C {
    val Bg = Color(0xFF0E0E10)
    val Panel = Color(0xFF1C1C21)
    val Key1 = Color(0xFF2D2D33)
    val Key2 = Color(0xFF19191D)
    val Text = Color(0xFFEDEDF0)
    val Sub = Color(0xFF9A9AA3)
    val Accent = Color(0xFF4C8DFF)
    val Red = Color(0xFFFF4B44)
    val Green = Color(0xFF2EE37A)
    val Amber = Color(0xFFFFB020)
}

private val keyBrush = Brush.verticalGradient(listOf(C.Key1, C.Key2))
private val rimBrush = Brush.verticalGradient(listOf(Color(0x30FFFFFF), Color(0x05FFFFFF)))

/** Press + hold-to-repeat with haptic feedback. */
fun Modifier.keyPress(
    repeat: Boolean = false,
    onPressed: (Boolean) -> Unit = {},
    onKey: () -> Unit,
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val fire by rememberUpdatedState(onKey)
    val pressedCb by rememberUpdatedState(onPressed)
    pointerInput(repeat) {
        detectTapGestures(onPress = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            pressedCb(true)
            fire()
            if (repeat) {
                coroutineScope {
                    val j = launch { delay(450); while (true) { fire(); delay(130) } }
                    tryAwaitRelease()
                    j.cancel()
                }
            } else {
                tryAwaitRelease()
            }
            pressedCb(false)
        })
    }
}

/** Staggered fade/slide-in when the screen opens. */
@Composable
fun Modifier.appear(index: Int): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        a.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
    }
    return this.graphicsLayer { alpha = a.value; translationY = (1f - a.value) * 48f }
}

@Composable
fun KeyButton(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    repeat: Boolean = false,
    tint: Color = Color.White,
    background: Brush = keyBrush,
    onKey: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        if (pressed) 0.9f else 1f,
        spring(dampingRatio = 0.5f, stiffness = 500f), label = "scale"
    )
    val glow by animateFloatAsState(if (pressed) 1f else 0f, tween(120), label = "glow")
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(background)
            .background(tint.copy(alpha = 0.18f * glow))
            .border(1.dp, rimBrush, shape)
            .keyPress(repeat, { pressed = it }, onKey),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
fun NumKey(label: String, onKey: () -> Unit) {
    KeyButton(Modifier.size(62.dp), onKey = onKey) {
        Text(label, color = C.Text, fontSize = 27.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun RowScope.Cell(content: @Composable () -> Unit) {
    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { content() }
}

/** Tall pill with a "+" half and a "-" half (volume / channel). */
@Composable
fun Rocker(
    modifier: Modifier,
    top: ImageVector,
    bottom: ImageVector,
    label: String,
    onTop: () -> Unit,
    onBottom: () -> Unit,
) {
    val shape = RoundedCornerShape(40.dp)
    var pTop by remember { mutableStateOf(false) }
    var pBot by remember { mutableStateOf(false) }
    val aTop by animateFloatAsState(if (pTop) 0.16f else 0f, tween(100), label = "t")
    val aBot by animateFloatAsState(if (pBot) 0.16f else 0f, tween(100), label = "b")
    Box(
        modifier
            .shadow(10.dp, shape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(shape)
            .background(keyBrush)
            .border(1.dp, rimBrush, shape)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.weight(1f).fillMaxWidth()
                    .background(Color.White.copy(alpha = aTop))
                    .keyPress(true, { pTop = it }, onTop),
                contentAlignment = Alignment.Center
            ) { Icon(top, null, tint = C.Text, modifier = Modifier.size(30.dp)) }
            Box(
                Modifier.weight(1f).fillMaxWidth()
                    .background(Color.White.copy(alpha = aBot))
                    .keyPress(true, { pBot = it }, onBottom),
                contentAlignment = Alignment.Center
            ) { Icon(bottom, null, tint = C.Text, modifier = Modifier.size(30.dp)) }
        }
        Text(label, Modifier.align(Alignment.Center), color = C.Sub, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

/** Circular D-pad: tap zones for up/down/left/right + center OK, with hold-to-repeat. */
@Composable
fun DPad(modifier: Modifier = Modifier, onKey: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val fire by rememberUpdatedState(onKey)
    var active by remember { mutableIntStateOf(-1) } // 0 up 1 down 2 left 3 right 4 ok
    val codes = intArrayOf(Key.UP, Key.DOWN, Key.LEFT, Key.RIGHT, Key.OK)
    val okScale by animateFloatAsState(if (active == 4) 0.9f else 1f, spring(0.5f, 500f), label = "ok")

    Box(
        modifier
            .shadow(16.dp, CircleShape, ambientColor = Color.Black, spotColor = Color.Black)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFF2A2A2F), Color(0xFF141417))))
            .border(1.dp, rimBrush, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(onPress = { o ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val dx = o.x - w / 2
                    val dy = o.y - h / 2
                    val zone = when {
                        hypot(dx, dy) < w * 0.21f -> 4
                        abs(dx) > abs(dy) -> if (dx > 0) 3 else 2
                        else -> if (dy > 0) 1 else 0
                    }
                    active = zone
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    fire(codes[zone])
                    if (zone != 4) {
                        coroutineScope {
                            val j = launch { delay(450); while (true) { fire(codes[zone]); delay(120) } }
                            tryAwaitRelease()
                            j.cancel()
                        }
                    } else {
                        tryAwaitRelease()
                    }
                    active = -1
                })
            },
        contentAlignment = Alignment.Center
    ) {
        @Composable
        fun BoxScope.Arrow(zone: Int, icon: ImageVector, align: Alignment, pad: Modifier) {
            val tint by animateColorAsState(if (active == zone) Color.White else C.Sub, tween(100), label = "arrow")
            val bg by animateFloatAsState(if (active == zone) 0.14f else 0f, tween(100), label = "arrowbg")
            Box(
                Modifier.align(align).then(pad).size(42.dp)
                    .background(Color.White.copy(alpha = bg), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = tint, modifier = Modifier.size(30.dp)) }
        }
        Arrow(0, Icons.Filled.KeyboardArrowUp, Alignment.TopCenter, Modifier.padding(top = 8.dp))
        Arrow(1, Icons.Filled.KeyboardArrowDown, Alignment.BottomCenter, Modifier.padding(bottom = 8.dp))
        Arrow(2, Icons.Filled.KeyboardArrowLeft, Alignment.CenterStart, Modifier.padding(start = 8.dp))
        Arrow(3, Icons.Filled.KeyboardArrowRight, Alignment.CenterEnd, Modifier.padding(end = 8.dp))

        Box(
            Modifier.size(80.dp)
                .graphicsLayer { scaleX = okScale; scaleY = okScale }
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(Color(0xFF34343A), Color(0xFF1E1E22))))
                .background(Color.White.copy(alpha = if (active == 4) 0.16f else 0f))
                .border(1.dp, rimBrush, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("OK", color = C.Text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold) }
    }
}
