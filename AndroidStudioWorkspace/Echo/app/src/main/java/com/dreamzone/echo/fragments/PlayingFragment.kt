package com.dreamzone.echo.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.dreamzone.echo.CurrentSongHelper
import com.dreamzone.echo.R
import com.dreamzone.echo.Songs
import com.dreamzone.echo.databases.EchoDatabase
import java.util.*
import java.util.concurrent.TimeUnit

class PlayingFragment : Fragment() {

    @SuppressLint("StaticFieldLeak")
    object Statified {
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekBar: SeekBar? = null
        var songArtistView: TextView? = null
        var songTitleView: TextView? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var currentSongHelper: CurrentSongHelper? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var fav: ImageButton? = null

        var favoriteContent: EchoDatabase? = null
        var mSensorManager: SensorManager? = null
        var mSenserListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"

        var updateSongTime = object : Runnable {
            override fun run() {
                try {
                    val getCurrent = mediaPlayer?.currentPosition
                    val min = checkTimeMin(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))
                    val sec = checkTimeSec(
                        (TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long) -
                                TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))) % 60
                    )
                    startTimeText?.text = String.format("%s:%s", min, sec)

                    seekBar?.progress = getCurrent?.toInt() as Int
                    Handler().postDelayed(this, 1000)
                } catch (e: Exception) {
                }
            }
        }

        fun checkTimeSec(time: Long): String {
            if (time > 9) return time.toString() else return "0$time"
        }

        fun checkTimeMin(time: Long): String {
            if (time > 9) return time.toString() else return "0$time"
        }
    }

    object Staticated {
        var MY_PREF_SHUFFLE = "Shuffle feature"
        var MY_PREF_LOOP = "Loop feature"

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition++
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition
            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songArtist = nextSong?.artist
            Statified.currentSongHelper?.songId = nextSong?.songId as Long
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity as Context,
                    Uri.parse(Statified.currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                processInformation(Statified.mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )
            }
        }


        fun onSongComplete() {

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true
                    val nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songArtist = nextSong?.artist
                    Statified.currentSongHelper?.songId = nextSong?.songId as Long

                    updateTextViews(
                        Statified.currentSongHelper?.songTitle as String,
                        Statified.currentSongHelper?.songArtist as String
                    )

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(
                            Statified.myActivity as Context,
                            Uri.parse(Statified.currentSongHelper?.songPath)
                        )
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }

            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )
            }
        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            var songTitleUpdated = songTitle
            var songArtistUpdated = songArtist
            if (songTitle.equals("<unknown>", true)) {
                songTitleUpdated = "Unknown"
            }
            if (songArtist.equals("<unknown>", true)) {
                songArtistUpdated = "Unknown"
            }
            Statified.songTitleView?.text = songTitleUpdated
            Statified.songArtistView?.text = songArtistUpdated
        }

        fun processInformation(mediaPlayer: MediaPlayer) {

            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekBar?.max = finalTime
            var min = Statified.checkTimeMin(TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()))
            var sec = Statified.checkTimeSec(
                (TimeUnit.MILLISECONDS.toSeconds(startTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        startTime.toLong()
                    )
                ) % 60) as Long
            )
            Statified.startTimeText?.text = String.format("%s:%s", min, sec)
            min = Statified.checkTimeMin(TimeUnit.MILLISECONDS.toMinutes(finalTime.toLong()))
            sec = Statified.checkTimeSec(
                TimeUnit.MILLISECONDS.toSeconds(finalTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        finalTime.toLong()
                    )
                ) as Long
            )
            Statified.endTimeText?.text = String.format("%s:%s", min, sec)

            Statified.seekBar?.setProgress(startTime)

            Handler().postDelayed(Statified.updateSongTime, 1000)
        }

    }

    var mAcceleration: Float = 0f
    var mAccelerationCurrent: Float = 0f
    var mAccelerationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"
        Statified.seekBar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fav = view?.findViewById(R.id.favouriteButton)
        Statified.fav?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisualization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }


    override fun onPause() {
        Statified.audioVisualization?.onPause()
        super.onPause()
        Statified.mSensorManager?.unregisterListener(Statified.mSenserListener)
    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisualization?.onResume()
        Statified.mSensorManager?.registerListener(
            Statified.mSenserListener, Statified.mSensorManager
                ?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onDestroy() {
        Statified.audioVisualization?.release()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAcceleration = 0.0f
        mAccelerationCurrent = SensorManager.GRAVITY_EARTH
        mAccelerationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var playSongTitle: String?
        var playSongArtist: String? = null
        var playSongId: Long

        try {
            path = arguments?.getString("path")
            playSongTitle = arguments?.getString("songTitle")
            playSongArtist = arguments?.getString("songArtist")
            playSongId = arguments!!.getInt("songId").toLong()
            Statified.currentPosition = arguments!!.getInt("songPosition")
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")


            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songArtist = playSongArtist
            Statified.currentSongHelper?.songTitle = playSongTitle
            Statified.currentSongHelper?.songId = playSongId
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition

            Staticated.updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }

        var fromFavScreen = arguments?.get("FromFavScreen") as? String
        var fromMainScreen = arguments?.get("FromMainScreen") as? String
        if (fromFavScreen != null) {
            Statified.mediaPlayer = FavoritesFragment.Statified.mediaPlayer
        } else if (fromMainScreen != null) {
            Statified.mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        } else {

            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity as Context, Uri.parse(path))
                Statified.mediaPlayer?.prepare()

            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()

        val visualizerHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisualization?.linkTo(visualizerHandler)

        var prefsForShuffle =
            Statified.myActivity?.getSharedPreferences(Staticated.MY_PREF_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREF_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.currentSongHelper?.isLoop = false
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
        }

        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fav?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fav?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
        }
    }

    fun clickHandler() {

        Statified.fav?.setOnClickListener {
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )
                Statified.favoriteContent?.deleteFavourite((Statified.currentSongHelper?.songId?.toInt() as Int))
                Toast.makeText(Statified.myActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fav?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
                Statified.favoriteContent?.storeAsFavorite(
                    Statified.currentSongHelper?.songId?.toInt(), Statified.currentSongHelper?.songArtist,
                    Statified.currentSongHelper?.songTitle, Statified.currentSongHelper?.songPath
                )
                Toast.makeText(Statified.myActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
            }
        }

        Statified.shuffleImageButton?.setOnClickListener {
            var editorShuffle = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_LOOP,
                Context.MODE_PRIVATE
            )?.edit()
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()

            }
        }

        Statified.nextImageButton?.setOnClickListener {

            Statified.currentSongHelper?.isPlaying = true
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }
        }
        Statified.previousImageButton?.setOnClickListener {
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        }
        Statified.loopImageButton?.setOnClickListener {
            var editorShuffle = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_SHUFFLE,
                Context.MODE_PRIVATE
            )?.edit()
            var editorLoop = Statified.myActivity?.getSharedPreferences(
                Staticated.MY_PREF_LOOP,
                Context.MODE_PRIVATE
            )?.edit()
            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {
                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }
        }
        Statified.playPauseImageButton?.setOnClickListener {
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                Statified.mediaPlayer?.start()
                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        }

        Statified.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBarget: SeekBar?) {
                Statified.seekBar?.progress = Statified.seekBar?.progress as Int
                Statified.mediaPlayer?.seekTo(Statified.seekBar?.progress as Int)
            }
        })

    }


    fun playPrevious() {
        Statified.currentPosition--
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songArtist = nextSong?.artist
        Statified.currentSongHelper?.songId = nextSong?.songId as Long

        Staticated.updateTextViews(
            Statified.currentSongHelper?.songTitle as String,
            Statified.currentSongHelper?.songArtist as String
        )

        Statified.mediaPlayer?.reset()

        try {
            Statified.mediaPlayer?.setDataSource(
                Statified.myActivity as Context,
                Uri.parse(Statified.currentSongHelper?.songPath)
            )
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fav?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fav?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))
        }
    }

    fun bindShakeListener() {
        Statified.mSenserListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelerationLast = mAccelerationCurrent
                mAccelerationCurrent = Math.sqrt(((x * x + y * y + z * z).toDouble())).toFloat()
                val delta = mAccelerationCurrent - mAccelerationLast
                mAcceleration = mAcceleration * 0.9f + delta

                if (mAcceleration > 12) {
                    val prefs =
                        Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("shakeFeature", false)
                    if (isAllowed as Boolean) {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

        }

    }
}

