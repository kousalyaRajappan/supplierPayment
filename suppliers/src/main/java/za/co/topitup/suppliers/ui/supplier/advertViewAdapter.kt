package za.co.topitup.suppliers.ui.supplier

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import za.co.topitup.suppliers.GlideApp
import za.co.topitup.suppliers.GlideOptions
import za.co.topitup.suppliers.R
import za.co.topitup.suppliers.databinding.AdvertViewBinding
import za.co.topitup.suppliers.network.loadAdvertImage
import javax.annotation.Nullable


class AdvertViewPagerAdapter(private val imageList: List<String>?)
    : RecyclerView.Adapter<AdvertViewPagerAdapter.AdvertViewPagerViewHolder>()
{

    lateinit var con :Context
    inner class AdvertViewPagerViewHolder(val binding: AdvertViewBinding)
        : RecyclerView.ViewHolder(binding.root) {
            val image = binding.advertImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertViewPagerViewHolder {
        con =parent.context
            return AdvertViewPagerViewHolder(
            AdvertViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: AdvertViewPagerViewHolder, position: Int)
    {
        val imageURL = imageList?.get(position % imageList.size)
//        holder.image.setImageResource(image)

        Log.e("image url","url..............."+imageURL)
        if (imageURL != null) {
            GlideApp.with(con).load(imageURL)
                .apply(GlideOptions.option(PageDecoder.PAGE_DECODER, true))
                .placeholder(R.drawable.topitup_logo_android).into(holder.image)


            /*loadAdvertImage(
                holder.itemView.context,
                holder.image,
                imageURL,
                R.drawable.topitup_logo_android,
            )*/
        }

        /*if (position < imageList.size) {
            val image = imageList[position % imageList.size]
            holder.image.setImageResource(image)
        }*/
    }

    override fun getItemCount(): Int {
        return if (imageList?.isNotEmpty() == true) {
            Int.MAX_VALUE
        } else {
            0
        }
        //return imageList.size
    }
}