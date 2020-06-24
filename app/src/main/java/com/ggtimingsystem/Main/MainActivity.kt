package com.ggtimingsystem.Main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.ggtimingsystem.*
import com.google.android.material.bottomnavigation.BottomNavigationView

// Top navigation bar and bottom navigation bar creation
class MainActivity : AppCompatActivity() {

    lateinit var toolbar : Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "Home"

        setSupportActionBar(toolbar)

        val homeFragment =
            HomeFragment.newInstance()
        openFragment(homeFragment)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    // Selects the fragment based on the bottom navigation bar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener {item->
        when (item.itemId){
            R.id.navigation_home ->{
                val homeFragment =
                    HomeFragment.newInstance()
                toolbar.title = "Home"
                openFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_run -> {
                val runFragment =
                    RunFragment.newInstance()
                toolbar.title = "Run"
                openFragment(runFragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                val settingsFragment =
                    SettingsFragment.newInstance()
                toolbar.title = "Settings"
                openFragment(settingsFragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun openFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
        Log.d("This works", "This works")
    }

    public fun openFragmentRun(view : View){
        val runDetailsFragment =
            RunDetailsFragment.newInstance()
        openFragment(runDetailsFragment)
    }
}