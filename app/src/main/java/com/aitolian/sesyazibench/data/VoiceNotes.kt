package com.aitolian.sesyazibench.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract

data class VoiceNote(val uri: Uri, val name: String, val modified: Long, val sizeBytes: Long) {
    /** WhatsApp sesli mesajları ~16 kbit/s opus → yaklaşık süre (dosyayı açmadan). */
    val approxSec: Int get() = (sizeBytes / 2_000).toInt().coerceAtLeast(1)
}

/**
 * WhatsApp sesli mesaj klasörüne tek seferlik (kullanıcı onaylı) erişim.
 * Android'in resmi klasör izni (SAF) kullanılır; "tüm dosyalar" izni istenmez.
 */
object VoiceNotes {
    private const val EXTERNAL = "com.android.externalstorage.documents"

    /** İzin ekranını doğrudan bu klasörde açmayı dener (bulunamazsa kullanıcı gezinir). */
    val initialFolder: Uri = DocumentsContract.buildDocumentUri(
        EXTERNAL, "primary:Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Voice Notes",
    )

    fun persist(context: Context, tree: Uri) {
        context.contentResolver.takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    fun release(context: Context, tree: Uri) {
        runCatching {
            context.contentResolver.releasePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun hasAccess(context: Context, tree: Uri): Boolean =
        context.contentResolver.persistedUriPermissions.any { it.uri == tree && it.isReadPermission }

    /**
     * Klasör ve alt klasörlerindeki (WhatsApp haftalık klasörler açar) ses
     * dosyalarını en yeniden eskiye döner.
     */
    fun list(context: Context, tree: Uri, limit: Int = 40): List<VoiceNote> {
        val out = mutableListOf<VoiceNote>()
        val rootId = DocumentsContract.getTreeDocumentId(tree)
        walk(context, tree, rootId, depth = 0, out = out)
        return out.sortedByDescending { it.modified }.take(limit)
    }

    private fun walk(context: Context, tree: Uri, docId: String, depth: Int, out: MutableList<VoiceNote>) {
        if (depth > 2) return
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
        val cols = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE,
        )
        runCatching {
            context.contentResolver.query(children, cols, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(0)
                    val name = c.getString(1) ?: continue
                    val mime = c.getString(2) ?: ""
                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        walk(context, tree, id, depth + 1, out)
                    } else if (isAudio(name, mime)) {
                        out += VoiceNote(
                            DocumentsContract.buildDocumentUriUsingTree(tree, id),
                            name, c.getLong(3), c.getLong(4),
                        )
                    }
                }
            }
        }
    }

    private fun isAudio(name: String, mime: String): Boolean {
        val n = name.lowercase()
        return mime.startsWith("audio/") || mime == "application/ogg" ||
            n.endsWith(".opus") || n.endsWith(".ogg") || n.endsWith(".m4a") || n.endsWith(".aac")
    }
}
