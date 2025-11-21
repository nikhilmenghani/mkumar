package com.mkumar.domain.logo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import com.mkumar.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cachedLogo: Bitmap? = null

    fun getLogo(): Bitmap {
        cachedLogo?.let { return it }

        val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            ?: error("Launcher icon drawable missing")

        val logo = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 256
            val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 256
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            drawable.setBounds(0, 0, w, h)
            drawable.draw(canvas)
            bmp
        }

        cachedLogo = logo
        return logo
    }
}
