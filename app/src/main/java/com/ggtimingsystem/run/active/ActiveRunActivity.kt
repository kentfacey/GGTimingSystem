package com.ggtimingsystem.run.active

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.ggtimingsystem.R
import com.ggtimingsystem.database.Database
import com.ggtimingsystem.main.HomeFragment
import com.ggtimingsystem.main.RunFragment
import com.ggtimingsystem.main.SettingsFragment
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.run.AvailableRunsActivity
import com.ggtimingsystem.run.RunDetailsActivity
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_active_run.*
import kotlinx.android.synthetic.main.fragment_run_progress.*
import java.time.Duration.between
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*


class ActiveRunActivity : AppCompatActivity() {

    private val PROGRESS_TAG = "progressFrag"
    private val RUNNERS_TAG = "runnersFrag"
    private var fManager = supportFragmentManager

    // Selects the fragment based on the bottom navigation bar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item->

        when (item.itemId){

            R.id.progress_navigation_active_run ->{

                if(fManager.findFragmentByTag(PROGRESS_TAG) != null){
                    //if the fragment exists, show it.
                    fManager.beginTransaction().show(fManager.findFragmentByTag(PROGRESS_TAG)!!).commit()
                }
                else {
                    //if the fragment does not exist, add it to fragment manager.
                    fManager.beginTransaction().add(R.id.container_FrameLayout_active_run, RunProgressFragment(), PROGRESS_TAG).commit()
                }
                if(fManager.findFragmentByTag(RUNNERS_TAG) != null){
                    //if the other fragment is visible, hide it.
                    fManager.beginTransaction().hide(fManager.findFragmentByTag(RUNNERS_TAG)!!).commit()
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.runners_navigation_active_run -> {

                if(fManager.findFragmentByTag(RUNNERS_TAG) != null){
                    //if the fragment exists, show it.
                    fManager.beginTransaction().show(fManager.findFragmentByTag(RUNNERS_TAG)!!).commit()
                }
                else {
                    //if the fragment does not exist, add it to fragment manager.
                    fManager.beginTransaction().add(R.id.container_FrameLayout_active_run, RunnersFragment(), RUNNERS_TAG).commit()
                }
                if(fManager.findFragmentByTag(PROGRESS_TAG) != null){
                    //if the other fragment is visible, hide it.
                    fManager.beginTransaction().hide(fManager.findFragmentByTag(PROGRESS_TAG)!!).commit()
                }

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_run)

        fManager.beginTransaction().add(R.id.container_FrameLayout_active_run, RunProgressFragment(), PROGRESS_TAG).commit()

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView_active_run)

        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    fun killActivity(){
        finish()
    }
    private fun openFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container_FrameLayout_active_run, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}