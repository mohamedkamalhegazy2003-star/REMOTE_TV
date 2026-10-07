package com.remotetv.app

/** Android KeyEvent codes sent to the TV. */
object Key {
    const val UP = 19
    const val DOWN = 20
    const val LEFT = 21
    const val RIGHT = 22
    const val OK = 23
    const val HOME = 3
    const val BACK = 4
    const val VOL_UP = 24
    const val VOL_DOWN = 25
    const val MUTE = 164
    const val CH_UP = 166
    const val CH_DOWN = 167
    const val MIC = 231          // KEYCODE_VOICE_ASSIST
    const val INPUT = 178        // KEYCODE_TV_INPUT
    const val SETTINGS = 176
    const val RED = 183
    const val GREEN = 184
    const val YELLOW = 185
    const val BLUE = 186
    const val NUM_ENTRY = 234    // KEYCODE_TV_NUMBER_ENTRY  (-/--)
    const val SLEEP = 223
    const val ALL_APPS = 284
    fun digit(n: Int) = 7 + n    // KEYCODE_0 = 7
}

/** An app the quick-launch tiles can open. Package is detected from what is installed on the TV. */
data class AppTarget(val label: String, val exact: List<String>, val keywords: List<String>) {
    companion object {
        val YouTube = AppTarget("YouTube",
            listOf("com.google.android.youtube.tv", "com.google.android.youtube"), listOf("youtube"))
        val Prime = AppTarget("Prime Video",
            listOf("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"),
            listOf("amazonvideo", "avod", "primevideo"))
        val Spotify = AppTarget("Spotify",
            listOf("com.spotify.tv.android", "com.spotify.music"), listOf("spotify"))
        val Shahid = AppTarget("Shahid",
            listOf("net.mbc.shahidtv", "net.mbc.shahid"), listOf("shahid"))
        val WatchIt = AppTarget("Watch It",
            listOf("com.watchit.tv", "com.watchit.app"), listOf("watchit", "watch.it", "watch_it"))
    }
}
