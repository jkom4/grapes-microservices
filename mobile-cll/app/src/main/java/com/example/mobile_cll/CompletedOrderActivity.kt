package com.example.mobile_cll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile_cll.ui.theme.MobileCLLTheme
import com.example.mobile_cll.view.CompletedOrderScreen
import com.example.mobile_cll.view.LastScreen
import com.example.mobile_cll.viewmodel.CompletedOrderViewModel

class CompletedOrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            MobileCLLTheme {
                NavHost(navController, startDestination = "completedOrder") {
                    composable("completedOrder") {
                        CompletedOrderScreen(navController = navController)
                    }
                    composable("lastScreen") {
                        LastScreen(navController, this@CompletedOrderActivity)
                    }
                }
            }
        }
    }
}
