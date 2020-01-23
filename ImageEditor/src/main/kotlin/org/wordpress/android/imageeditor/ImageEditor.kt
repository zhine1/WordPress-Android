
package org.wordpress.android.imageeditor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCrop.Options
import com.yalantis.ucrop.model.AspectRatio
import com.yalantis.ucrop.view.CropImageView
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageEditor @Inject constructor() {
    fun edit(
        context: Context,
        mediaUrl: String
    ) {
        // TODO
        // Temporarily goes to edit image activity
        val intent = Intent(context, EditImageActivity::class.java)

        val bundle = prepareUCropBundle(mediaUrl, context) ?: Bundle()
        intent.putExtra(EditImageActivity.ARG_BUNDLE, bundle)

        EditImageActivity.startIntent(context, intent)
    }

    private fun prepareUCropBundle(mediaUrl: String, context: Context): Bundle? {
        val options = Options()
        options.setShowCropGrid(true)
        options.setFreeStyleCropEnabled(true)
        options.setShowCropFrame(true)
        options.setHideBottomControls(false)
        options.setAspectRatioOptions(
            0,
            AspectRatio("1:2", 1f, 2f),
            AspectRatio("3:4", 3f, 4f),
            AspectRatio("Original", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
            AspectRatio("16:9", 16f, 9f),
            AspectRatio("1:1", 1f, 1f)
        )

        val uCrop = UCrop.of(
            Uri.parse(mediaUrl),
            Uri.fromFile(File(context.cacheDir, "cropped_image.jpg")) // TODO
        ).withOptions(options)

        return uCrop.getIntent(context).extras
    }
}
