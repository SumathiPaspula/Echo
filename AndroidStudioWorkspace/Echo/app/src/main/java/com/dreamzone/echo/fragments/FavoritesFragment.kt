package com.dreamzone.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.dreamzone.echo.R
import com.dreamzone.echo.Songs
import com.dreamzone.echo.adapters.FavoriteAdapter
import com.dreamzone.echo.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_favorites.*


class FavoritesFragment : Fragment() {

    var myActivity: Activity? = null
    var getFavSongs: ArrayList<Songs>? = null
    var noFavorites: TextView? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null


    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater!!.inflate(R.layout.fragment_favorites, container, false)
        activity?.title = "Favorites"
        favoriteContent = EchoDatabase(myActivity)
        noFavorites = view?.findViewById(R.id.noFavorites)
        nowPlayingBottomBar = view.findViewById(R.id.hiddenBarFavScreen)
        songTitle = view.findViewById(R.id.songTitle)
        playPauseButton = view.findViewById(R.id.playPauseButton)
        recyclerView = view.findViewById(R.id.favContentMain)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.getFavSongs = getSongs()
        if (this.getFavSongs == null) {
            favContentMain?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        } else {
            var favoriteAdapter = FavoriteAdapter(
                this.getFavSongs as ArrayList<Songs>,
                myActivity as Context
            )
            val mLayoutManager = LinearLayoutManager(activity)
            favContentMain?.layoutManager = mLayoutManager
            favContentMain?.itemAnimator = DefaultItemAnimator()
            favContentMain?.adapter = favoriteAdapter
            favContentMain.setHasFixedSize(true)
            favContentMain?.visibility = View.VISIBLE
            noFavorites?.visibility = View.INVISIBLE
        }
        display_favorites_by_searching()
        bottomBarSetup()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_sort)
        item?.isVisible = false
    }

    fun getSongs(): ArrayList<Songs> {
        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            while (songCursor.moveToNext()) {
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.text = PlayingFragment.Statified.currentSongHelper?.songTitle
            PlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                songTitle?.text = PlayingFragment.Statified.currentSongHelper?.songTitle
                PlayingFragment.Staticated.onSongComplete()
            }
            if (PlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                nowPlayingBottomBar?.visibility = View.VISIBLE
            } else {
                nowPlayingBottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {

        nowPlayingBottomBar?.setOnClickListener {

            Statified.mediaPlayer = PlayingFragment.Statified.mediaPlayer
            val playingFragment = PlayingFragment()

            var args = Bundle()
            args.putString("songArtist", PlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", PlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", PlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("songId", PlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", PlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int)
            args.putParcelableArrayList("songData", PlayingFragment.Statified.fetchSongs)
            args.putString("FromFavScreen", "Success")
            playingFragment.arguments = args

            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, playingFragment)
                ?.addToBackStack("SongPlayFragment")
                ?.commit()
        }

        playPauseButton?.setOnClickListener {
            if (PlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                PlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = PlayingFragment.Statified.mediaPlayer?.currentPosition
                        as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                PlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                PlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }
    }

    fun display_favorites_by_searching() {
        if (favoriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            this.getFavSongs = favoriteContent?.queryDBList()
            val fetchListfromDevice = getSongs()
            if (fetchListfromDevice != null)
                for (i in 0 until fetchListfromDevice.size) {
                    for (j in 0 until this.getFavSongs?.size as Int) {
                        if (this.getFavSongs?.get(j)?.songId === fetchListfromDevice?.get(i)?.songId) {
                            refreshList?.add((this.getFavSongs as ArrayList<Songs>)[j])
                        }
                    }
                }
            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            } else {
                val favoriteAdapter = FavoriteAdapter(
                    refreshList as ArrayList<Songs>,
                    myActivity as Context
                )
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }
        } else {
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }
    }


}