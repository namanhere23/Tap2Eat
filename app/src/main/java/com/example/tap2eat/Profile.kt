package com.example.tap2eat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.io.File


class Profile : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val person = arguments?.getSerializable("EXTRA_USER_DETAILS") as? UserDetails
            val profilePic = view.findViewById<ImageView>(R.id.profilePic)
            person?.email?.let { email ->
                loadUserByEmail(email) { user ->
                    if (user != null) {

                        if (!user.photo.isNullOrEmpty()) {
                            profilePic.setImageURI(Uri.fromFile(File(user.photo)))
                        } else {
                            profilePic.setImageResource(R.drawable.ic_profile_pic)
                        }
                    }
                }
            }

            profilePic.setOnClickListener {
                Intent(requireContext(), Details_Page::class.java).also {
                    it.putExtra("EXTRA_USER_DETAILS", person)
                    startActivity(it)
                }
            }
        }

    private fun loadUserByEmail(email: String, onResult: (UserDetails?) -> Unit) {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userId = email.replace(".", "_")
        usersRef.child(userId).get()
            .addOnSuccessListener { snap ->
                val user = snap.getValue(UserDetails::class.java)
                onResult(user)
            }
            .addOnFailureListener { e ->
                onResult(null)
            }
    }
}