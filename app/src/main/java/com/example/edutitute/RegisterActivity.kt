package com.example.edutitute

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edutitute.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupDropdowns()
        setupListeners()
    }

    private fun setupDropdowns() {
        val roles = listOf("Headmaster", "Moderator", "Teacher", "Student")
        val genders = listOf("Male", "Female", "Other")

        binding.roleDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        )
        binding.genderDropdown.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        )
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val schoolId = binding.etSchoolId.text.toString().trim()
            val role = binding.roleDropdown.text.toString().trim()
            val first = binding.etFirstName.text.toString().trim()
            val last = binding.etLastName.text.toString().trim()
            val gender = binding.genderDropdown.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(role, first, last, gender, email, password)) {
                checkSchoolBeforeRegister(schoolId, role, first, last, gender, email, password)
            }
        }
    }

    /**
     * Step 1 — Validate school and headmaster constraints before creating user
     */
    private fun checkSchoolBeforeRegister(
        schoolId: String,
        role: String,
        first: String,
        last: String,
        gender: String,
        email: String,
        password: String
    ) {
        db.collection("schools").document(schoolId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // School already exists
                    if (role == "Headmaster") {
                        toastLong("A Headmaster already exists for School ID $schoolId.")
                        resetButton()
                    } else {
                        // Allowed: register under existing school
                        registerUser(schoolId, role, first, last, gender, email, password)
                    }
                } else {
                    // School does not exist yet
                    if (role == "Headmaster") {
                        registerUser(schoolId, role, first, last, gender, email, password)
                    } else {
                        toastLong("School ID $schoolId does not exist. Please ask your Headmaster to create it first.")
                        resetButton()
                    }
                }
            }
            .addOnFailureListener {
                toast("Failed to check school: ${it.message}")
                resetButton()
            }
    }

    /**
     * Step 2 — Register user normally
     */
    private fun registerUser(
        schoolId: String,
        role: String,
        first: String,
        last: String,
        gender: String,
        email: String,
        password: String
    ) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Registering..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: return@addOnSuccessListener
                val username = generateUsername(schoolId, role)

                val userData = mapOf(
                    "uid" to uid,
                    "firstName" to first,
                    "lastName" to last,
                    "gender" to gender,
                    "email" to email,
                    "role" to role,
                    "schoolId" to schoolId,
                    "username" to username,
                    "approved" to (role == "Headmaster"),
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        if (role == "Headmaster") {
                            createSchoolRecord(schoolId, "$first $last")
                        }
                        toastLong("Registration successful for School ID: $schoolId")
                        finish()
                    }
                    .addOnFailureListener { e ->
                        toast("Error saving user: ${e.message}")
                        resetButton()
                    }
            }
            .addOnFailureListener { e ->
                toast("Error: ${e.message}")
                resetButton()
            }
    }

    /**
     * Step 3 — Create school entry if Headmaster
     */
    private fun createSchoolRecord(schoolId: String, headmasterName: String) {
        val schoolData = mapOf(
            "schoolId" to schoolId,
            "headmaster" to headmasterName,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("schools").document(schoolId)
            .set(schoolData)
            .addOnSuccessListener {
                toast("New school registered successfully!")
            }
            .addOnFailureListener {
                toast("Failed to create school record: ${it.message}")
            }
    }

    private fun generateUsername(schoolId: String, role: String): String {
        val prefix = when (role) {
            "Headmaster" -> "H"
            "Moderator" -> "M"
            "Teacher" -> "T"
            "Student" -> "S"
            else -> "U"
        }
        val randomNum = (100..999).random()
        return "$prefix$randomNum"
    }

    private fun validateInputs(
        role: String,
        first: String,
        last: String,
        gender: String,
        email: String,
        password: String
    ): Boolean {
        return when {
            role.isEmpty() -> toast("Select role")
            first.isEmpty() -> toast("Enter first name")
            last.isEmpty() -> toast("Enter last name")
            gender.isEmpty() -> toast("Select gender")
            email.isEmpty() -> toast("Enter email")
            password.length < 6 -> toast("Password must be at least 6 characters")
            else -> true
        }
    }

    private fun resetButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Create Account"
    }

    private fun toast(msg: String): Boolean {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun toastLong(msg: String): Boolean {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        return false
    }
}
