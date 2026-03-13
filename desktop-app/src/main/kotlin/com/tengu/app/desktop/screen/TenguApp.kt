package com.tengu.app.desktop.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.tengu.app.desktop.screen.home.HomeNavKey
import com.tengu.app.framework.NavEntryProvider
import com.tengu.app.framework.theme.TenguTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.getKoin

@Composable
fun TenguApp() {
    val koin = getKoin()
    val navEntryProviders = remember { koin.getAll<NavEntryProvider>() }
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    for (provider in navEntryProviders) {
                        with(provider) {
                            polymorph()
                        }
                    }
                }
            }
        },
        HomeNavKey,
    )
    TenguTheme {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                for (provider in navEntryProviders) {
                    with(provider) { build() }
                }
            },
        )
    }
}
