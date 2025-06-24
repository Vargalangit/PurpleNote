package com.UASMP.purplenote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ThirdFragment : Fragment(R.layout.fragment_third) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ambil tombol logout dari layout
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Aksi klik logout
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Arahkan kembali ke Login setelah logout
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
