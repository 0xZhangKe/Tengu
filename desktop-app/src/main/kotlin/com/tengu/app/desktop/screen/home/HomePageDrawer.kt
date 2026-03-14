package com.tengu.app.desktop.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.framework.theme.TenguTheme

@Composable
fun HomePageDrawer() {
    HomeSurface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = "Setting",
                style = TenguTheme.typography.titleMedium,
            )
        }
    }
}
