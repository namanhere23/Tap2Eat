package com.example.tap2eat

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.os.bundleOf
import com.google.android.material.button.MaterialButton
import java.io.File

class Offers : Fragment(R.layout.fragment_offers) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val offerText = arguments?.getString("offerText") ?: ""
        val offerCode = arguments?.getInt("offerCode") ?: 0

        val blockTextView=view.findViewById<MaterialButton>(R.id.offerText)
        val blockButton=view.findViewById<ConstraintLayout>(R.id.offerButton)

        blockTextView.text = offerText.toString()
        if(offerCode%2==1) {
            blockButton.setBackgroundResource(R.drawable.pinkcard)
        }
        else {
            blockButton.setBackgroundResource(R.drawable.bluecard)
        }
        }

    }



