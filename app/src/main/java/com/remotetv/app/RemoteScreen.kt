package com.remotetv.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RemoteScreen(c: TvController) {
    val state by c.state.collectAsState()
    val snack = remember { SnackbarHostState() }
    var showConnect by remember { mutableStateOf(false) }
    var showPower by remember { mutableStateOf(false) }
    var showMore by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { c.messages.collectLatest { snack.showSnackbar(it) } }
    LaunchedEffect(Unit) { if (c.savedHost.isEmpty()) showConnect = true }

    Box(Modifier.fillMaxSize().background(C.Bg).systemBarsPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 28.dp)
        ) {
            // ---------------------------------------------------------- header
            Header(state, Modifier.appear(0), onStatus = { showConnect = true }, onPower = { showPower = true })
            Spacer(Modifier.height(18.dp))

            // ---------------------------------------------------------- number pad
            Column(Modifier.appear(1)) {
                listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                        r.forEach { n -> Cell { NumKey("$n") { c.key(Key.digit(n)) } } }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                    Cell { LabeledKey("ENTER NO.") { NumKey("-/--") { c.key(Key.NUM_ENTRY) } } }
                    Cell { LabeledKey("") { NumKey("0") { c.key(Key.digit(0)) } } }
                    Cell {
                        LabeledKey("MORE") {
                            KeyButton(Modifier.size(62.dp), onKey = { showMore = true }) {
                                Icon(Icons.Filled.MoreHoriz, null, tint = C.Text, modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            // ---------------------------------------------------------- volume / mute / mic / channel
            Row(Modifier.fillMaxWidth().height(160.dp).appear(2), verticalAlignment = Alignment.CenterVertically) {
                Cell {
                    Rocker(Modifier.width(66.dp).height(160.dp), Icons.Filled.Add, Icons.Filled.Remove, "VOL",
                        { c.key(Key.VOL_UP) }, { c.key(Key.VOL_DOWN) })
                }
                Cell {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyButton(Modifier.size(58.dp), onKey = { c.key(Key.MUTE) }) {
                            Icon(Icons.Filled.VolumeOff, null, tint = C.Text, modifier = Modifier.size(26.dp))
                        }
                        KeyButton(Modifier.size(58.dp), tint = C.Accent, onKey = { c.key(Key.MIC) }) {
                            Icon(Icons.Filled.Mic, null, tint = C.Text, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                Cell {
                    Rocker(Modifier.width(66.dp).height(160.dp), Icons.Filled.KeyboardArrowUp, Icons.Filled.KeyboardArrowDown, "CH",
                        { c.key(Key.CH_UP) }, { c.key(Key.CH_DOWN) })
                }
            }
            Spacer(Modifier.height(18.dp))

            // ---------------------------------------------------------- navigation
            Row(Modifier.fillMaxWidth().appear(3), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.weight(1f).height(200.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SideKey(Icons.Filled.Home, "Home") { c.key(Key.HOME) }
                    SideKey(Icons.AutoMirrored.Filled.ArrowBack, "Back") { c.key(Key.BACK) }
                }
                DPad(Modifier.size(196.dp)) { c.key(it) }
                Column(
                    Modifier.weight(1f).height(200.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SideKey(Icons.Filled.SettingsInputHdmi, "Input") { c.key(Key.INPUT) }
                    SideKey(Icons.Filled.Settings, "Settings") { c.key(Key.SETTINGS) }
                }
            }
            Spacer(Modifier.height(20.dp))

            // ---------------------------------------------------------- color keys
            Row(Modifier.fillMaxWidth().appear(4), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                ColorKey(Color(0xFFE53935), Modifier.weight(1f)) { c.key(Key.RED) }
                ColorKey(Color(0xFF1FB86A), Modifier.weight(1f)) { c.key(Key.GREEN) }
                ColorKey(Color(0xFFFBC02D), Modifier.weight(1f)) { c.key(Key.YELLOW) }
                ColorKey(Color(0xFF3B82F6), Modifier.weight(1f)) { c.key(Key.BLUE) }
            }
            Spacer(Modifier.height(18.dp))

            // ---------------------------------------------------------- app tiles
            Column(Modifier.appear(5), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TileRow({ YouTubeLogo() }, { c.launch(AppTarget.YouTube) }, { PrimeLogo() }, { c.launch(AppTarget.Prime) })
                TileRow({ SpotifyLogo() }, { c.launch(AppTarget.Spotify) }, { ShahidLogo() }, { c.launch(AppTarget.Shahid) })
                TileRow({ AppsLabel() }, { c.openAllApps() }, { WatchItLogo() }, { c.launch(AppTarget.WatchIt) })
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }

    if (showConnect) ConnectDialog(c, state) { showConnect = false }
    if (showPower) PowerDialog(c) { showPower = false }
    if (showMore) MoreDialog(c) { showMore = false }
}

// ------------------------------------------------------------------ pieces

@Composable
private fun Header(state: ConnState, modifier: Modifier, onStatus: () -> Unit, onPower: () -> Unit) {
    val (dot, text) = when (state) {
        is ConnState.Connected -> C.Green to "CONNECTED • ${state.name.uppercase()}"
        ConnState.Connecting -> C.Amber to "CONNECTING…"
        is ConnState.Failed -> C.Red to "DISCONNECTED • TAP TO CONNECT"
        ConnState.Disconnected -> C.Red to "NOT CONNECTED • TAP TO CONNECT"
    }
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(
        0.35f, 1f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "dot"
    )
    val glow by inf.animateFloat(
        0.25f, 0.6f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow"
    )
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null, onClick = onStatus
        )) {
            Text("Remote TV", color = C.Text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).alpha(if (state is ConnState.Connected) 1f else pulse).background(dot, CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(text, color = C.Sub, fontSize = 13.sp, letterSpacing = 0.5.sp, maxLines = 1)
            }
        }
        Box(
            Modifier.size(78.dp).drawBehind {
                drawCircle(
                    Brush.radialGradient(listOf(C.Red.copy(alpha = glow), Color.Transparent)),
                    radius = size.minDimension * 0.5f
                )
            },
            contentAlignment = Alignment.Center
        ) {
            KeyButton(
                Modifier.size(56.dp), tint = C.Red,
                background = Brush.verticalGradient(listOf(Color(0xFF3A1A1C), Color(0xFF22100F))),
                onKey = onPower
            ) { Icon(Icons.Filled.PowerSettingsNew, null, tint = C.Red, modifier = Modifier.size(28.dp)) }
        }
    }
}

@Composable
private fun LabeledKey(label: String, key: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        key()
        Spacer(Modifier.height(6.dp))
        Text(label.ifEmpty { " " }, color = C.Sub, fontSize = 11.sp, letterSpacing = 0.8.sp, maxLines = 1)
    }
}

@Composable
private fun SideKey(icon: ImageVector, label: String, onKey: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        KeyButton(Modifier.size(50.dp), onKey = onKey) {
            Icon(icon, null, tint = C.Text, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, color = C.Sub, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun ColorKey(color: Color, modifier: Modifier, onKey: () -> Unit) {
    KeyButton(
        modifier.height(34.dp), shape = RoundedCornerShape(20.dp), tint = color,
        background = Brush.verticalGradient(listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.7f))),
        onKey = onKey
    ) {}
}

@Composable
private fun TileRow(
    leftLogo: @Composable () -> Unit, onLeft: () -> Unit,
    rightLogo: @Composable () -> Unit, onRight: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KeyButton(Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(18.dp), onKey = onLeft) { leftLogo() }
        KeyButton(Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(18.dp), onKey = onRight) { rightLogo() }
    }
}

// ------------------------------------------------------------------ dialogs

@Composable
private fun ConnectDialog(c: TvController, state: ConnState, onDismiss: () -> Unit) {
    var ip by remember { mutableStateOf(c.savedHost) }
    var name by remember { mutableStateOf(c.savedName) }
    var scanning by remember { mutableStateOf(false) }
    var found by remember { mutableStateOf(listOf<String>()) }
    var scanned by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.Panel,
        title = { Text("Connect to TV") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "On the TV: Settings → Developer options → enable USB debugging / ADB over network (port 5555). " +
                        "Accept the prompt on the TV the first time and tick “Always allow”.",
                    color = C.Sub, fontSize = 12.sp
                )
                OutlinedTextField(
                    ip, { ip = it }, label = { Text("TV IP address") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    name, { name = it }, label = { Text("Name (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(
                    enabled = !scanning,
                    onClick = {
                        scanning = true
                        scope.launch { found = NetScan.scan(); scanned = true; scanning = false }
                    }
                ) { Text(if (scanning) "Scanning…" else "Scan network") }
                if (scanned && found.isEmpty()) Text("No device found with ADB enabled.", color = C.Sub, fontSize = 12.sp)
                found.forEach { host ->
                    Text(
                        host, color = C.Accent, fontSize = 16.sp,
                        modifier = Modifier.fillMaxWidth().clickable { ip = host }.padding(vertical = 8.dp)
                    )
                }
                if (state is ConnState.Failed) Text(state.reason, color = C.Red, fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(onClick = { c.connect(ip, name); onDismiss() }) { Text("Connect") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun PowerDialog(c: TvController, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.Panel,
        title = { Text("Power") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Shut down the laptop that is running Android TV?", color = C.Text)
                OutlinedButton(onClick = { c.sleep(); onDismiss() }) { Text("Sleep instead") }
            }
        },
        confirmButton = {
            TextButton(onClick = { c.shutdown(); onDismiss() }) { Text("Shut down", color = C.Red) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MoreDialog(c: TvController, onDismiss: () -> Unit) {
    val extra = listOf(
        "Play/Pause" to 85, "Rewind" to 89, "Forward" to 90,
        "Previous" to 88, "Next" to 87, "Stop" to 86,
        "Menu" to 82, "Guide" to 172, "Info" to 165,
        "Subtitles" to 175, "Search" to 84, "Sleep" to Key.SLEEP,
    )
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = C.Panel,
        title = { Text("More keys") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                extra.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { (label, code) ->
                            FilledTonalButton(
                                onClick = { c.key(code) }, modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) { Text(label, fontSize = 11.sp, maxLines = 1) }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    text, { text = it }, label = { Text("Type text on TV (English)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { c.sendText(text); text = "" }, enabled = text.isNotEmpty()) { Text("Send text") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}
