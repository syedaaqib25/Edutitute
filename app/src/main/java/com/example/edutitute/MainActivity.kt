package com.example.edutitute

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.edutitute.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DashboardAdapter
    private val features = mutableListOf<DashboardFeature>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecycler()
        populateFeatures()
    }

    /**
     * Sets up the dashboard grid recycler view
     */
    private fun setupRecycler() {
        binding.rvDashboard.layoutManager = GridLayoutManager(this, 2)
        adapter = DashboardAdapter(features) { feature ->
            handleFeatureClick(feature)
        }
        binding.rvDashboard.adapter = adapter
    }

    /**
     * Adds sample features to show on main screen
     * (You can later replace this with Firestore or API data)
     */
    private fun populateFeatures() {
        features.clear()
        features.addAll(
            listOf(
                DashboardFeature("Profile", R.drawable.ic_user),
                DashboardFeature("Manage School", R.drawable.ic_school),
                DashboardFeature("Approve Users", R.drawable.ic_lock),
                DashboardFeature("Attendance", R.drawable.ic_attendance),
                DashboardFeature("Messages", R.drawable.ic_report),
                DashboardFeature("Reports", R.drawable.ic_marks)
            )
        )
        adapter.notifyDataSetChanged()
    }

    /**
     * Handles click events for each dashboard feature
     */
    private fun handleFeatureClick(feature: DashboardFeature) {
        when (feature.name) {
            "Profile" -> Toast.makeText(this, "Open Profile Screen", Toast.LENGTH_SHORT).show()
            "Manage School" -> Toast.makeText(this, "Open Manage School Screen", Toast.LENGTH_SHORT).show()
            "Approve Users" -> Toast.makeText(this, "Open Approve Users", Toast.LENGTH_SHORT).show()
            "Attendance" -> Toast.makeText(this, "Open Attendance Tracker", Toast.LENGTH_SHORT).show()
            "Messages" -> Toast.makeText(this, "Open Messages", Toast.LENGTH_SHORT).show()
            "Reports" -> Toast.makeText(this, "Open Reports", Toast.LENGTH_SHORT).show()
            else -> Toast.makeText(this, "${feature.name} coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}
