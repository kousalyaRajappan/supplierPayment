package za.co.topitup.suppliers.network

import android.content.Context
import android.widget.ImageView
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import za.co.topitup.suppliers.BuildConfig
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.models.Retailer
import za.co.topitup.suppliers.utils.logger

fun loadSupplierImage(context: Context, imageView: ImageView, imageFileName: String,
                      errorImageDrawable: Int = R.drawable.ic_error) {

    val url = Retailer.logoBaseURL.plus("/").plus(imageFileName)
    logger("url load","............."+url)
    val logo = context.imageLoader
    val request = ImageRequest.Builder(context)
        .data(url)
        .decoderFactory(SvgDecoder.Factory())
        .target(imageView)
        .placeholder(R.drawable.ic_download)
        .error(errorImageDrawable)
        .listener(
            onError = { _: ImageRequest, result: ErrorResult ->
                logger("imageLoader", "$url -> ${result.throwable}")
            }
        )
        .addHeader("User-Agent", "${BuildConfig.LIBRARY_PACKAGE_NAME} (v${BuildConfig.VERSION_NAME})")
        .build()

    logo.enqueue(request)
}

fun loadAdvertImage(context: Context, imageView: ImageView, imageFileUrl: String,
                    errorImageDrawable: Int = R.drawable.topitup_logo_android) {

    val image = context.imageLoader
    val request = ImageRequest.Builder(context)
        .data(imageFileUrl)
        .decoderFactory(SvgDecoder.Factory())
        .target(imageView)
        .placeholder(R.drawable.topitup_logo_android)
        .error(errorImageDrawable)
        .listener(
            onError = { _: ImageRequest, result: ErrorResult ->
                logger("imageLoader", "$imageFileUrl -> ${result.throwable}", null)
            }
        )
        .addHeader("User-Agent", "${BuildConfig.LIBRARY_PACKAGE_NAME} (v${BuildConfig.VERSION_NAME})")
        .build()

    image.enqueue(request)
}