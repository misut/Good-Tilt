package com.goodtilt.goodtilt.fragment

import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.goodtilt.goodtilt.ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE
import com.goodtilt.goodtilt.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.frag_hello.view.*
import kotlinx.android.synthetic.main.frag_permission.*
import kotlinx.android.synthetic.main.frag_permission.view.*
import kotlinx.android.synthetic.main.frag_permission.view.next

class PermissionFragment : Fragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_permission, container, false)
        rootView.requestOverlay.setOnClickListener {
            if (!checkOverlayPermission()) {
                val uri: Uri = Uri.fromParts("package", activity?.packageName, null)
                val intent =
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }

        rootView.requestAccess.setOnClickListener {
            if (!checkAccessibilityPermissions()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityForResult(intent, ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE)
            }
        }

        val manualActivity = activity as ManualActivity
        rootView.next.setOnClickListener(manualActivity.nextListener)
        rootView.prev.setOnClickListener(manualActivity.prevListener)
        return rootView
    }

    override fun onStart() {
        var check = checkOverlayPermission()
        check = check &&  checkAccessibilityPermissions()
        next.isEnabled = check
        super.onStart()
    }

    fun checkAccessibilityPermissions(): Boolean {
        var result = (Settings.Secure.getInt(
                activity?.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1)
        imageView.isEnabled = !result
        if(result) {
            imageView2.setImageResource(R.drawable.ic_baseline_check_24)
            requestAccess.visibility = View.GONE
        } else {
            imageView2.setImageResource(R.drawable.ic_baseline_close_24)
            requestAccess.visibility = View.VISIBLE
        }
        return result
    }

    fun checkOverlayPermission(): Boolean {
        var result = Settings.canDrawOverlays(activity)
        if(result) {
            imageView.setImageResource(R.drawable.ic_baseline_check_24)
            requestOverlay.visibility = View.GONE
        } else {
            imageView.setImageResource(R.drawable.ic_baseline_close_24)
            requestOverlay.visibility = View.VISIBLE
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (!checkOverlayPermission())
                    Toast.makeText(activity, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                else
                    next.isEnabled = checkAccessibilityPermissions()
            }
            ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                if (!checkAccessibilityPermissions())
                    Toast.makeText(activity, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                else
                    next.isEnabled = checkOverlayPermission()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
