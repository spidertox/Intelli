package ai.intelli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.graphics.drawable.toBitmap
import ai.intelli.data.preferences.AuthPreferences
import ai.intelli.theme.GeminiTheme
import ai.intelli.theme.isSystemInDarkThemeRobust
import ai.intelli.ui.components.LocalTerminalDialog

class TerminalActivity : ComponentActivity() {

    private val authPreferences: AuthPreferences by lazy { AuthPreferences(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        showTermuxIconInRecents()
        ai.intelli.data.service.IntelliService.start(this, fromForeground = true)

        setContent {
            val initialThemeMode = remember { authPreferences.getThemeModeSync() }
            val themeMode by authPreferences.themeMode.collectAsState(initial = initialThemeMode)
            val isSystemDark = isSystemInDarkThemeRobust()
            val useDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemDark
            }

            GeminiTheme(darkTheme = useDarkTheme) {
                LocalTerminalDialog(
                    onDismiss = { finish() }
                )
            }
        }
    }

    /**
     * Recents shows this activity's own icon (the Intelli one) for the terminal task, even when it was opened from the
     * Termux-style launcher icon. When that style is on, give the task the Termux icon and name instead. It must be a
     * bitmap: the Pixel launcher's Recents ignores a task icon given as a resource id.
     */
    private fun showTermuxIconInRecents() {
        if (ai.intelli.data.preferences.TerminalLauncherManager.getLauncherStyle(this) !=
            ai.intelli.data.preferences.TerminalLauncherManager.STYLE_TERMUX
        ) return
        try {
            val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.mipmap.ic_termux_launcher) ?: return
            val size = (getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager).launcherLargeIconSize
            val icon = drawable.toBitmap(size, size)
            @Suppress("DEPRECATION")
            setTaskDescription(android.app.ActivityManager.TaskDescription(getString(R.string.termux_name), icon))
        } catch (_: Exception) {
        }
    }
}
