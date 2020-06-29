package com.ggtimingsystem.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ggtimingsystem.R

/**
 * A simple [Fragment] subclass.
 * Use the [RunDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RunDetailsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_run_details, container, false)

    companion object {
        fun newInstance(): RunDetailsFragment =
            RunDetailsFragment()
    }
}