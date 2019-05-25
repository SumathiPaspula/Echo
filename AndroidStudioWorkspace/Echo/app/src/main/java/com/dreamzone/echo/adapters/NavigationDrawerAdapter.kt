package com.dreamzone.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.dreamzone.echo.MainActivity
import com.dreamzone.echo.R
import com.dreamzone.echo.fragments.AboutUsFragment
import com.dreamzone.echo.fragments.FavoritesFragment
import com.dreamzone.echo.fragments.MainScreenFragment
import com.dreamzone.echo.fragments.SettingsFragment


class NavigationDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context) :
    RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {


    private var contentList: ArrayList<String>? = null
    private var getImages: IntArray? = null
    private var mContext: Context? = null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onBindViewHolder(p0: NavViewHolder, p1: Int) {
        p0.icon?.setBackgroundResource(getImages?.get(p1) as Int)
        p0.text?.text = contentList?.get(p1)
        p0.contentHolder?.setOnClickListener {
            when (p1) {
                0 -> {
                    val mainScreenFragment = MainScreenFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
                }
                1 -> {
                    val favoritesFragment = FavoritesFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, favoritesFragment)
                        .commit()
                }
                2 -> {
                    val settingsFragment = SettingsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, settingsFragment)
                        .commit()
                }
                3 -> {
                    val aboutUsFragment = AboutUsFragment()
                    (mContext as MainActivity).supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.details_fragment, aboutUsFragment)
                        .commit()
                }
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        }
    }


    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): NavViewHolder {
        val itemView = LayoutInflater.from(p0?.context)
            .inflate(R.layout.navigation_list, p0, false)
        return NavViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size

    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: ImageView? = null
        var text: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            icon = itemView.findViewById(R.id.list_item_icon)
            text = itemView.findViewById(R.id.list_item_name)
            contentHolder = itemView.findViewById(R.id.nav_list_item)


        }
    }

}