package com.example.mobile_cll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.mobile_cll.models.DatabaseHelper
import com.example.mobile_cll.ui.theme.MobileCLLTheme
import com.example.mobile_cll.views.components.MyNavigation


/**
 * Main entry point of the application, responsible for setting up navigation and the UI theme.
 * It initializes the DatabaseHelper and delegates navigation to MyNavigation composable.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created. Sets up the database helper, UI content,
     * and navigation structure using Jetpack Compose and Navigation.
     *
     * @param savedInstanceState If non-null, this activity is being re-initialized with previously saved state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val databaseHelper = DatabaseHelper(this)

        setContent {
            MobileCLLTheme {
                val navController = rememberNavController()
                MyNavigation(navController = navController, databaseHelper = databaseHelper)
            }
        }
    }
}