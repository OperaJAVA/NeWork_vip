package ru.netology.nework.activity

import android.content.Context
import android.net.Uri
import ru.netology.nework.media.MediaModel
import ru.netology.nework.util.AndroidUtils

object ContentLoader {
    fun getContentLoading(uri: Uri, typeFile: String, context: Context?): MediaModel? {
        // Проверяем, что контекст не null
        if (context == null) return null

        val type = context.contentResolver.getType(uri)
        return if (type != null && type.contains(Regex(typeFile))) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val length = inputStream.available()
                val name = AndroidUtils.getFileName(uri, context) ?: "_"
                MediaModel(uri = uri, name = name, length = length, type = type)
            }
        } else {
            null
        }
    }
}