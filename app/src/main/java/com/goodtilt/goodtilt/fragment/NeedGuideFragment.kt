package com.goodtilt.goodtilt.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.frag_need_guide.view.*

class NeedGuideFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_need_guide, container, false)
        val manualActivity = activity as ManualActivity
        rootView.next.setOnClickListener(manualActivity.noneSkipListener)
        rootView.skip.setOnClickListener(manualActivity.skipListener)
        rootView.prev.setOnClickListener(manualActivity.prevListener)
        return rootView
    }


}