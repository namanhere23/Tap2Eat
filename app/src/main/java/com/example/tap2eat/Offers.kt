package com.example.tap2eat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton

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



