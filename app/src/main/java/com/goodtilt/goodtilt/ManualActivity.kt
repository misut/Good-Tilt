package com.goodtilt.goodtilt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goodtilt.goodtilt.fragment.*
import kotlinx.android.synthetic.main.activity_manual.*

class ManualActivity : AppCompatActivity() {
    var skipGuide = false
    val prevListener = View.OnClickListener {
        onBackPressed()
    }

    val skipListener = View.OnClickListener {
        skipGuide = true
        manualPager.setCurrentItem(4, true)
    }

    val noneSkipListener = View.OnClickListener {
        skipGuide = false
        manualPager.setCurrentItem(3, true)
    }


    val nextListener = View.OnClickListener {
        val next = manualPager.currentItem + 1
        if (next < manualPager.adapter?.itemCount!!)
            manualPager.setCurrentItem(next, true)
        else
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    inner class ManualAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return 6
        }

        override fun createFragment(position: Int): Fragment {
            when(position) {
                0 -> return HelloWorldFragment()
                1 -> return PermissionFragment()
                2 -> return NeedGuideFragment()
                3 -> return GuideFragment()
                4 -> return AreaFragment()
                5 -> return TiltFragment()
            }
            return Fragment()
        }
    }


    override fun onBackPressed() {
        val prev = manualPager.currentItem - 1
        if (prev == 3 && skipGuide)
            manualPager.setCurrentItem(prev - 1, true)
        else if (prev >= 0)
            manualPager.setCurrentItem(prev, true)
        else
            super.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            //getString("tilt_left", "NONE")?.let { actionList[0] = KeyAction.valueOf(it) }
        }
        manualPager.isUserInputEnabled = false
        manualPager.adapter = ManualAdapter(this)
    }
}