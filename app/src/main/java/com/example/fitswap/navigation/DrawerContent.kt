package com.example.fitswap.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.fitswap.R

@Composable
fun DrawerContent(
    currentRoute: String?,
    onDestinationClick: (Destination) -> Unit,
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        HorizontalDivider()
        topLevelDestinations.forEach { destination ->
            NavigationDrawerItem(
                label = { Text(destination.label) },
                selected = currentRoute == destination.route,
                onClick = { onDestinationClick(destination) },
                modifier = Modifier
                    .testTag("drawerItem_${destination.route}")
                    .padding(horizontal = 12.dp)
            )
        }
    }
}
