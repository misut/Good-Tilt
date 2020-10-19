package com.goodtilt.goodtilt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.source.HelloWorldFragment
import com.goodtilt.goodtilt.source.PermissionFragment
import com.goodtilt.goodtilt.source.TiltFragment
import kotlinx.android.synthetic.main.activity_manual.*
import kotlinx.android.synthetic.main.frag_tilt.view.*

class ManualActivity : AppCompatActivity() {

    val prevListener = View.OnClickListener {
        val prev = manualPager.currentItem - 1
        if (prev >= 0)
            manualPager.setCurrentItem(prev, true)
    }

    val nextListener = View.OnClickListener {
        val next = manualPager.currentItem + 1
        if (next < manualPager.adapter?.count!!)
            manualPager.setCurrentItem(next, true)
        else
            startActivity(Intent(this, MainActivity::class.java))
    }

    class ManualAdapter(fragmentManger: FragmentManager) : FragmentStatePagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return 3
        }

        override fun getItem(position: Int): Fragment {
            when(position) {
                0 -> return HelloWorldFragment()
                1 -> return PermissionFragment()
                2 -> return TiltFragment()
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
        manualPager.adapter = ManualAdapter(supportFragmentManager)
    }
}