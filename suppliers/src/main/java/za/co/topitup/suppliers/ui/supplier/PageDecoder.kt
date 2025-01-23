package za.co.topitup.suppliers.ui.supplier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.load.Option
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import java.io.InputStream

class PageDecoder(
    private val bitmapPool: BitmapPool
) : ResourceDecoder<InputStream, Bitmap> {

    companion object {
        val PAGE_DECODER: Option<Boolean> = Option.memory("abc")
    }

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<Bitmap>? {
        return BitmapResource.obtain(BitmapFactory.decodeStream(source), bitmapPool)
    }

    override fun handles(source: InputStream, options: Options): Boolean = options.get(PAGE_DECODER) ?: false

}