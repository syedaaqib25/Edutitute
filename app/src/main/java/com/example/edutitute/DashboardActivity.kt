package com.example.edutitute

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.edutitute.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: DashboardAdapter
    private val features = mutableListOf<DashboardFeature>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecycler()
        loadUserData()
        setupLogout()
    }

    private fun setupRecycler() {
        binding.recyclerDashboard.layoutManager = GridLayoutManager(this, 2)
        adapter = DashboardAdapter(features) { selectedFeature ->
            handleFeatureClick(selectedFeature)
        }
        binding.recyclerDashboard.adapter = adapter
    }

    private fun loadUserData() {
        val user = auth.currentUser ?: return
        db.collection("users").whereEqualTo("uid", user.uid).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty) {
                    val doc = docs.first()
                    val firstName = doc.getString("firstName") ?: "User"
                    val role = doc.getString("role") ?: "User"
                    val schoolId = doc.getString("schoolId") ?: "Unknown"

                    binding.tvWelcome.text = "Welcome, $firstName!"
                    binding.tvRoleInfo.text = "Role: $role | School: $schoolId"

                    populateFeatures(role)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFeatures(role: String) {
        features.clear()

        // Common for all roles
        val commonFeatures = listOf(
            DashboardFeature("Profile", R.drawable.ic_user),
            DashboardFeature("Messages", R.drawable.ic_message) // Updated to proper message icon
        )

        when (role) {
            "Headmaster" -> features.addAll(
                listOf(
                    DashboardFeature("Manage School", R.drawable.ic_school),
                    DashboardFeature("View Staff", R.drawable.ic_teacher),
                    DashboardFeature("Attendance", R.drawable.ic_attendance),
                    DashboardFeature("Approve Users", R.drawable.ic_user),
                    DashboardFeature("Report Logs", R.drawable.ic_report)
                ) + commonFeatures
            )

            "Moderator" -> features.addAll(
                listOf(
                    DashboardFeature("Approve Users", R.drawable.ic_user),
                    DashboardFeature("View Users", R.drawable.ic_teacher),
                    DashboardFeature("Attendance", R.drawable.ic_attendance)
                ) + commonFeatures
            )

            "Teacher" -> features.addAll(
                listOf(
                    DashboardFeature("Marks", R.drawable.ic_marks),
                    DashboardFeature("Timetable", R.drawable.ic_timetable),
                    DashboardFeature("Attendance", R.drawable.ic_attendance)
                ) + commonFeatures
            )

            "Student" -> features.addAll(
                listOf(
                    DashboardFeature("My Attendance", R.drawable.ic_attendance),
                    DashboardFeature("My Timetable", R.drawable.ic_timetable),
                    DashboardFeature("My Marks", R.drawable.ic_marks)
                ) + commonFeatures
            )

            else -> features.addAll(commonFeatures)
        }

        adapter.notifyDataSetChanged()
    }

    private fun handleFeatureClick(feature: DashboardFeature) {
        when (feature.name) {
            "Profile" -> startActivity(Intent(this, ProfileActivity::class.java))
            "Approve Users" -> startActivity(Intent(this, ApproveUsersActivity::class.java))
            "Messages" -> startActivity(Intent(this, MessagesActivity::class.java))

            "Manage School" ->
                Toast.makeText(this, "Manage School feature coming soon!", Toast.LENGTH_SHORT).show()
            "View Staff", "View Users" ->
                Toast.makeText(this, "View Staff feature coming soon!", Toast.LENGTH_SHORT).show()
            "Attendance", "My Attendance" ->
                Toast.makeText(this, "Attendance feature coming soon!", Toast.LENGTH_SHORT).show()
            "Marks", "My Marks" ->
                Toast.makeText(this, "Marks feature coming soon!", Toast.LENGTH_SHORT).show()
            else ->
                Toast.makeText(this, "${feature.name} feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLogout() {
        binding.ivLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
