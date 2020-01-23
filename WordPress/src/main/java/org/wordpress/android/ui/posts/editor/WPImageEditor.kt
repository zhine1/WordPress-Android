package org.wordpress.android.ui.posts.editor

import android.content.Context
import org.wordpress.android.imageeditor.ImageEditor
import org.wordpress.android.util.image.ImageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WPImageEditor @Inject constructor() {
    @Inject lateinit var imageManager: ImageManager
    @Inject lateinit var imageEditor: ImageEditor

    fun edit(
        context: Context,
        mediaUrl: String
    ) {
        imageEditor.edit(context, mediaUrl)
    }
}
