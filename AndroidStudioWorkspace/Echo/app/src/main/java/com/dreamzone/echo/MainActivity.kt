package com.dreamzone.echo

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.dreamzone.echo.adapters.NavigationDrawerAdapter
import com.dreamzone.echo.fragments.MainScreenFragment
import com.dreamzone.echo.fragments.PlayingFragment

class MainActivity : AppCompatActivity() {
    var navigationIconList: ArrayList<String> = arrayListOf()
    var navigationImageList = intArrayOf(
        R.drawable.navigation_allsongs, R.drawable.navigation_favorites,
        R.drawable.navigation_settings, R.drawable.navigation_aboutus
    )

    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManager? = null
    }

    var trackNotificationBuilder: Notification? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        Statified.drawerLayout = findViewById(R.id.drawer_layout)

        navigationIconList.add("All Songs")
        navigationIconList.add("Favorites")
        navigationIconList.add("Settings")
        navigationIconList.add("About Us")

        val toggle = ActionBarDrawerToggle(
            this@MainActivity, Statified.drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()

        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
            .commit()

        var navigationAdapter = NavigationDrawerAdapter(navigationIconList, navigationImageList, this)

        navigationAdapter.notifyDataSetChanged()

        var navRecycleView = findViewById<RecyclerView>(R.id.navigation_recycler_view)
        navRecycleView.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager?
        navRecycleView.itemAnimator = DefaultItemAnimator()
        navRecycleView.adapter = navigationAdapter
        navRecycleView.setHasFixedSize(true)
        val intent = Intent(this@MainActivity, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this@MainActivity, System.currentTimeMillis().toInt(),
            intent, 0
        )
        trackNotificationBuilder = Notification.Builder(this)
            .setContentTitle("A track is playing in background")
            .setSmallIcon(R.drawable.echo_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(true)
            .build()
        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    public override fun onStart() {

        try {
            Statified.notificationManager?.cancel(1111)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStart()
    }

    override fun onResume() {

        try {
            Statified.notificationManager?.cancel(1111)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onResume()
    }

    override fun onStop() {
        try {
            if (PlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.notificationManager?.notify(1111, trackNotificationBuilder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onStop()
    }

    override fun onDestroy() {
        try {
            Statified.notificationManager?.cancel(1111)
        } catch (e: Exception) {

            e.printStackTrace()
        }
        super.onDestroy()

    }

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }

    }

}
