package com.megix

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.phisher98.BuildConfig

class BottomFragment(private val plugin: CineStream) : BottomSheetDialogFragment() {

    private var cookieInput: EditText? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val id = plugin.resources!!.getIdentifier(
            "bottom_sheet_layout",
            "layout",
            BuildConfig.LIBRARY_PACKAGE_NAME
        )
        val layout = plugin.resources!!.getLayout(id)
        val view = inflater.inflate(layout, container, false)

        val outlineId = plugin.resources!!.getIdentifier(
            "outline",
            "drawable",
            BuildConfig.LIBRARY_PACKAGE_NAME
        )

        val contentGroup = view.findView<ViewGroup>("server_group")
        contentGroup.removeAllViews()

        // Create the Input Field
        cookieInput = EditText(context).apply {
            hint = "Paste Superstream Cookie here..."

            // --- CONNECTED: Pre-fill with the saved variable ---
            setText(CineStream.superstreamCookie)
            // -------------------------------------------------

            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            textSize = 14f
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            gravity = Gravity.TOP or Gravity.START

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(20, 20, 20, 20)
            }

            background = plugin.resources!!.getDrawable(outlineId, null)
            setPadding(30, 30, 30, 30)
        }

        contentGroup.addView(cookieInput)

        // Save Button Logic
        val saveIconId = plugin.resources!!.getIdentifier(
            "save_icon",
            "drawable",
            BuildConfig.LIBRARY_PACKAGE_NAME
        )
        val saveBtn = view.findView<ImageView>("save")
        saveBtn.setImageDrawable(plugin.resources!!.getDrawable(saveIconId, null))
        saveBtn.background = plugin.resources!!.getDrawable(outlineId, null)

        saveBtn.setOnClickListener {
            val cookieString = cookieInput?.text.toString().trim()

            if (cookieString.isNotEmpty()) {
                // 1. Save to the memory variable
                CineStream.superstreamCookie = cookieString

                // 2. Save to Phone Storage (SharedPreferences) so it remembers after restart
                val prefs = context?.getSharedPreferences("cinestream_settings", Context.MODE_PRIVATE)
                prefs?.edit()?.putString("superstream_cookie", cookieString)?.apply()

                Toast.makeText(context, "Cookie Saved!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Please paste the cookie first", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun <T : View> View.findView(name: String): T {
        val id = plugin.resources!!.getIdentifier(name, "id", BuildConfig.LIBRARY_PACKAGE_NAME)
        return findViewById(id)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }
}
