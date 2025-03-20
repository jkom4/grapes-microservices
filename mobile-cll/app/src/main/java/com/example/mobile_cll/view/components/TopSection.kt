package com.example.mobile_cll.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4CAD7E))
            .height(140.dp)
            .padding(horizontal = 35.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text("Hello Mathys", fontSize = 18.sp, color = Color.White)
        Text("4 trips to do", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}


