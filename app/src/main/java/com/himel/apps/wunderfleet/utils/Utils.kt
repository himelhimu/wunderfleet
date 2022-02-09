package com.himel.apps.wunderfleet.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("imageUrl")
fun bindingAdapter(view:ImageView,imageUrl:String?){
    Glide.with(view).load(imageUrl).into(view)
}