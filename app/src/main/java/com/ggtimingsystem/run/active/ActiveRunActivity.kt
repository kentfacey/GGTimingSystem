package com.ggtimingsystem.run.active

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ggtimingsystem.R
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.run.AvailableRunsActivity
import com.ggtimingsystem.run.RunDetailsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView


class ActiveRunActivity : AppCompatActivity() {

    private val PROGRESS_TAG = "progressFrag"
    private val RUNNERS_TAG = "runnersFrag"
    private var fManager = supportFragmentManager

    companion object{
        val RUN_KEY = "RUN_KEY"
    }

    // Selects the fragment based on the bottom navigation bar
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item->

        // switches between the fragments depending on which navigation button is clicked
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
                    //Todo pass value from activity to fragment
                    val run = intent.getParcelableExtra<Run>(
                        RunDetailsActivity.RUN_KEY)
                    val bundle = Bundle().apply{
                        putString(RUN_KEY, run.uid)
                    }
                    Log.d("ActiveRunActivity", "Run uid: ${run.uid}")
                    val runnersFragment = RunnersFragment()
                    runnersFragment.arguments = bundle
                    fManager.beginTransaction().add(R.id.container_FrameLayout_active_run, runnersFragment, RUNNERS_TAG).commit()
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