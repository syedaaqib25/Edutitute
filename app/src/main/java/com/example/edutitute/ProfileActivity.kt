package com.example.edutitute

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.edutitute.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        animateViews()
        loadUserProfile()
        setupBackButton()
    }

    private fun animateViews() {
        binding.profileCard.apply {
            alpha = 0f
            translationY = 40f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setStartDelay(200)
                .start()
        }

        binding.btnBack.apply {
            alpha = 0f
            translationY = 30f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setStartDelay(400)
                .start()
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No logged-in user found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("users")
            .whereEqualTo("uid", user.uid)
            .get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.first()
                    val firstName = doc.getString("firstName") ?: "Unknown"
                    val lastName = doc.getString("lastName") ?: ""
                    val fullName = "$firstName $lastName".trim()
                    val email = doc.getString("email") ?: "Not available"
                    val role = doc.getString("role") ?: "User"
                    val schoolId = doc.getString("schoolId") ?: "N/A"
                    val gender = doc.getString("gender") ?: "Not specified"

                    binding.tvFullName.text = fullName
                    binding.tvEmail.text = "Email: $email"
                    binding.tvRole.text = role
                    binding.tvSchoolId.text = "School ID: $schoolId"
                    binding.tvGender.text = "Gender: $gender"

                    // Apply dynamic color to role chip
                    setRoleChipColor(role)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun setRoleChipColor(role: String) {
        val context = this
        val colorRes = when (role.lowercase()) {
            "headmaster" -> R.color.amber_500
            "moderator" -> R.color.teal_700
            "teacher" -> R.color.indigo_700
            "student" -> R.color.green_600
            else -> R.color.gray
        }

        val color = ContextCompat.getColor(context, colorRes)
        binding.tvRole.setBackgroundColor(color)
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
