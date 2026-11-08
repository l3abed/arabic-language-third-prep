package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryDarkTeal
import com.example.ui.theme.TextMutedLight
import com.example.viewmodel.NavTab

@Composable
fun AppBottomNav(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("app_bottom_nav"),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                title = "الرئيسية",
                icon = Icons.Outlined.Home,
                isSelected = currentTab == NavTab.HOME,
                onClick = { onTabSelected(NavTab.HOME) },
                testTag = "nav_tab_home"
            )
            NavItem(
                title = "الدروس",
                icon = Icons.Outlined.MenuBook,
                isSelected = currentTab == NavTab.LESSONS,
                onClick = { onTabSelected(NavTab.LESSONS) },
                testTag = "nav_tab_lessons"
            )
            NavItem(
                title = "التمارين",
                icon = Icons.Outlined.CheckCircleOutline,
                isSelected = currentTab == NavTab.EXERCISES,
                onClick = { onTabSelected(NavTab.EXERCISES) },
                testTag = "nav_tab_exercises"
            )
            NavItem(
                title = "حسابي",
                icon = Icons.Outlined.Person,
                isSelected = currentTab == NavTab.ACCOUNT,
                onClick = { onTabSelected(NavTab.ACCOUNT) },
                testTag = "nav_tab_account"
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val activeColor = if (MaterialTheme.colorScheme.surface == Color.White) PrimaryDarkTeal else GoldAccent
    val inactiveColor = TextMutedLight

    Column(
        modifier = Modifier
            .weight(1f)
            .testTag(testTag)
            .clickable(onClick = onClick)
            .height(68.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Gold top indicator line matching design screenshot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .background(GoldAccent)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .background(Color.Transparent)
            )
        }

        Column(
            modifier = Modifier.padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = if (isSelected) activeColor else inactiveColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
