package com.babyMonitor

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // Show and Manage the Drawer and Back Icon
        setupActionBarWithNavController(navController, drawerLayout)

        // Handle Navigation item clicks
        // This works with no further action on your part if the menu and destination id’s match.
        navigationView.setupWithNavController(navController)

        // Allow drawables in the icons instead of tinted icons
        navigationView.itemIconTintList = null

        // Set the version value in the drawer footer
        findViewById<TextView>(R.id.version_value).text = BuildConfig.VERSION_NAME
    }

    override fun onSupportNavigateUp(): Boolean {
        // Allows NavigationUI to support proper up navigation or the drawer layout
        // drawer menu, depending on the situation
        return navController.navigateUp(drawerLayout) || super.onSupportNavigateUp()
    }

}