package ai.intelli.data.files

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import ai.intelli.data.security.AppIntentKey
import ai.intelli.i18n.L
import java.io.File

/**
 * URIs another app hands to Intelli (share, "open with", install). Intelli can open its own private files, so another
 * app could otherwise make it read them for it: `content://<pkg>.fileprovider/…/shared_prefs/…`,
 * `file:///data/data/<pkg>/files/home/.ssh/…`, and copy them into a mini app or open them where it can see them.
 *
 * Refused unless Intelli itself sent the intent ([AppIntentKey.OPEN_OWN_URI]):
 *  - content:// of a provider that runs as Intelli's user id (its own, and those of the apps that share it);
 *  - file:// anywhere but shared storage (apps targeting Android 7+ can't send file:// at all).
 */
object IncomingUriGuard {

    fun isIntelliPrivate(context: Context, uri: Uri): Boolean = when (uri.scheme?.lowercase()) {
        "content" -> {
            val authority = uri.authority
            if (authority.isNullOrBlank()) true
            else {
                val info = runCatching {
                    @Suppress("DEPRECATION")
                    context.packageManager.resolveContentProvider(authority, PackageManager.GET_META_DATA)
                }.getOrNull()
                info != null && info.applicationInfo.uid == context.applicationInfo.uid
            }
        }
        "file" -> {
            val canon = runCatching { File(uri.path ?: "").canonicalPath }.getOrNull()
            // shared storage only, and not Intelli's own folder there (Android/data/<pkg>, closed to other apps on 11+)
            val dirs: List<File?> = context.externalCacheDirs.orEmpty().toList() + context.getExternalFilesDirs(null).orEmpty().toList()
            val ownExternal = dirs.mapNotNull { d: File? -> runCatching { d?.parentFile?.canonicalPath }.getOrNull() }
            canon == null || SHARED.none { canon == it.trimEnd('/') || canon.startsWith(it) } ||
                ownExternal.any { canon == it || canon.startsWith("$it/") }
        }
        else -> false
    }

    private val SHARED = listOf("/storage/", "/sdcard/", "/mnt/media_rw/")

    /** Whether [uri] may be used for this [intent]: not a private one, or one Intelli sent itself. */
    fun allowed(context: Context, intent: Intent?, uri: Uri): Boolean =
        !isIntelliPrivate(context, uri) || AppIntentKey.isTrusted(context, intent, AppIntentKey.OPEN_OWN_URI)

    fun toastRefused(context: Context) {
        Toast.makeText(context, L.files.incomingUriGuard.antigemDoesNotOpenItsOwn, Toast.LENGTH_LONG).show()
    }

    /** Signs an explicit intent to one of Intelli's own screens that carries one of its own files. */
    fun signOwn(context: Context, intent: Intent): Intent = AppIntentKey.sign(context, intent, AppIntentKey.OPEN_OWN_URI)

    /**
     * A chooser for Intelli's own files without Intelli's own screens in it: they would refuse the file (they can't
     * tell it came from Intelli through a chooser).
     */
    fun chooser(context: Context, target: Intent, title: CharSequence?): Intent =
        Intent.createChooser(target, title).putExtra(
            Intent.EXTRA_EXCLUDE_COMPONENTS,
            arrayOf(
                ComponentName(context, "ai.intelli.MainActivity"),
                ComponentName(context, "ai.intelli.ui.miniapps.MiniAppShareActivity"),
                ComponentName(context, "ai.intelli.ui.miniapps.MiniAppInstallActivity"),
            )
        )
}
