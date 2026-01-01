package com.example.edutitute

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edutitute.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔥 Firebase initialization
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 🌈 Smooth fade-in animation
        window.decorView.alpha = 0f
        window.decorView.animate().alpha(1f).setDuration(500).start()
        binding.appLogo.animate().alpha(1f).setDuration(800).start()
        binding.loginCard.animate().alpha(1f).translationYBy(-20f).setDuration(900).setStartDelay(500).start()

        // 🏫 Load School IDs
        loadSchoolIds()

        // ⚙️ Setup button actions
        setupClickListeners()
    }

    /**
     * Load available school IDs dynamically
     */
    private fun loadSchoolIds() {
        db.collection("schools")
            .get()
            .addOnSuccessListener { result ->
                val schoolList = result.documents.map { it.id }
                if (schoolList.isEmpty()) {
                    Toast.makeText(this, "No schools found in database", Toast.LENGTH_SHORT).show()
                } else {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, schoolList)
                    binding.schoolDropdown.setAdapter(adapter)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load school list", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Click listeners for buttons
     */
    private fun setupClickListeners() {
        // 🔑 Login
        binding.btnSignIn.setOnClickListener {
            val schoolId = binding.schoolDropdown.text.toString().trim()
            val email = binding.userIdInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(schoolId, email, password)) {
                performLogin(schoolId, email, password)
            }
        }

        // 📝 Register
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ❓ Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.userIdInput.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset link sent to $email", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * Validate input fields
     */
    private fun validateInputs(schoolId: String, email: String, password: String): Boolean {
        return when {
            schoolId.isEmpty() -> {
                Toast.makeText(this, "Please select a School ID", Toast.LENGTH_SHORT).show()
                false
            }

            email.isEmpty() -> {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                false
            }

            password.isEmpty() -> {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                false
            }

            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    /**
     * Perform Firebase Authentication
     */
    private fun performLogin(schoolId: String, email: String, password: String) {
        binding.btnSignIn.isEnabled = false
        binding.btnSignIn.text = "Logging in..."
        binding.btnSignIn.alpha = 0.7f

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    verifyUserInFirestore(uid, schoolId)
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    /**
     * Verify if the authenticated user exists and is approved in Firestore
     */
    private fun verifyUserInFirestore(uid: String, schoolId: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val isApproved = doc.getBoolean("approved") ?: false
                    val userSchoolId = doc.getString("schoolId") ?: ""
                    val role = doc.getString("role") ?: "User"
                    val name = "${doc.getString("firstName")} ${doc.getString("lastName")}".trim()

                    if (userSchoolId != schoolId) {
                        Toast.makeText(this, "Wrong school selected", Toast.LENGTH_LONG).show()
                        auth.signOut()
                        resetButton()
                        return@addOnSuccessListener
                    }

                    if (isApproved) {
                        Toast.makeText(this, "Welcome $role $name!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Your account is pending approval", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                } else {
                    Toast.makeText(this, "User record not found", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                }
                resetButton()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error connecting to database", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    /**
     * Reset login button state
     */
    private fun resetButton() {
        binding.btnSignIn.isEnabled = true
        binding.btnSignIn.text = "Login"
        binding.btnSignIn.alpha = 1f
    }
}
