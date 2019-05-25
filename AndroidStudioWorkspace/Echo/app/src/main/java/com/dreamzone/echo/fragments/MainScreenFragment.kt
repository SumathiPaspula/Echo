package com.dreamzone.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.dreamzone.echo.R
import com.dreamzone.echo.Songs
import com.dreamzone.echo.adapters.MainScreenAdapter
import java.util.*
import kotlin.collections.ArrayList
import android.support.v7.widget.RecyclerView as RecyclerView1


class MainScreenFragment : Fragment() {

    var getSongsList: ArrayList<Songs>? = null
    var nowPlayingBottomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var recyclerView: RecyclerView1? = null
    var myActivity: Activity? = null
    var trackPosition: Int = 0

    var _mainScreenAdapter: MainScreenAdapter? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        activity?.title = "All Songs"
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        noSongs = view?.findViewById(R.id.noSongs)
        nowPlayingBottomBar = view?.findViewById(R.id.hiddenBarMainScreen)
        songTitle = view?.findViewById(R.id.songTitle)
        playPauseButton = view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.contentMain)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getSongs()

        val prefs = activity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)
        val action_sort_ascending = prefs?.getString("action_sort_ascending", "true")
        val action_sort_recent = prefs?.getString("action_sort_recent", "false")

        if (getSongsList == null) {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        } else {
            _mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)

            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = _mainScreenAdapter
        }
        if (getSongsList != null) {
            if (action_sort_ascending!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            } else if (action_sort_recent!!.equals("true", ignoreCase = true)) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                _mainScreenAdapter?.notifyDataSetChanged()
            }
        }
        bottomBarSetup()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        when (switcher) {
            R.id.action_sort_ascending -> {
                val editor = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
                editor?.putString("action_sort_ascending", "true")
                editor?.putString("action_sort_recent", "false")
                editor?.apply()
                if (getSongsList != null) {
                    Collections.sort(getSongsList, Songs.Statified.nameComparator)
                }
                _mainScreenAdapter?.notifyDataSetChanged()
                return false
            }
            R.id.action_sort_recent -> {
                val editortwo = myActivity?.getSharedPreferences("action_sort", Context.MODE_PRIVATE)?.edit()
                editortwo?.putString("action_sort_recent", "true")
                editortwo?.putString("action_sort_ascending", "false")
                editortwo?.apply()
                if (getSongsList != null) {
                    Collections.sort(getSongsList, Songs.Statified.dateComparator)
                }
                _mainScreenAdapter?.notifyDataSetChanged()
                return false
            }
            else -> return super.onOptionsItemSelected(item)
        }
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
            songTitle?.setText(PlayingFragment.Statified.currentSongHelper?.songTitle)
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
            args.putString("FromMainScreen", "Success")
            playingFragment.arguments = args

            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, playingFragment, "SongPlayingFragment")
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
}
