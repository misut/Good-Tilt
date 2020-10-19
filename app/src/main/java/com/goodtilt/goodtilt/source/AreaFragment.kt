package com.goodtilt.goodtilt.source
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.frag_area.view.*

class AreaFragment : Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_area, container, false)
        val manualActivity = activity as ManualActivity
        rootView.next.setOnClickListener(manualActivity.nextListener)
        rootView.prev.setOnClickListener(manualActivity.prevListener)

        //rootView.seekBarWidth.setOnSeekBarChangeListener()
        return rootView
    }
}