package com.kesco.xposed.slideback

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class AppAdapter(val ctx:Context):RecyclerView.Adapter<AppAdapter.AppVH>() {

    val layoutInflater:LayoutInflater

    init {
        layoutInflater = LayoutInflater.from(ctx)
    }

    override fun onBindViewHolder(holder: AppVH?, position: Int) {
        throw UnsupportedOperationException()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AppVH? {
        throw UnsupportedOperationException()
    }

    override fun getItemCount(): Int {
        throw UnsupportedOperationException()
    }

    class AppVH(view: View):RecyclerView.ViewHolder(view) {

    }
}
