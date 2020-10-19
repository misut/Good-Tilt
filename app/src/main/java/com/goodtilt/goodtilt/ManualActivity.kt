package com.goodtilt.goodtilt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.source.HelloWorldFragment
import com.goodtilt.goodtilt.source.TiltFragment
import kotlinx.android.synthetic.main.activity_manual.*

class ManualActivity : AppCompatActivity() {

    class ManualAdapter(fragmentManger: FragmentManager) : FragmentStatePagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            when(position) {
                0 -> return HelloWorldFragment()
                1 -> return TiltFragment()
            }
            return Fragment()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            //getString("tilt_left", "NONE")?.let { actionList[0] = KeyAction.valueOf(it) }
        }

        manual_pager.adapter = ManualAdapter(supportFragmentManager)
    }

    fun prevPage(){

    }

    fun nextPage(){

    }
}