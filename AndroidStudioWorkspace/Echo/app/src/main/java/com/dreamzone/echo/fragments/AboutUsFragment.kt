package com.dreamzone.echo.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ImageButton
import com.dreamzone.echo.R

class AboutUsFragment : Fragment() {

    private val aboutLogo: ImageButton? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about_us, container, false)
        activity?.title = "About Us"
        aboutLogo?.alpha = 0.5f
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

}
