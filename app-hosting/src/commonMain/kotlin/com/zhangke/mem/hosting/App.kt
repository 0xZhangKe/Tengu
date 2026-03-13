package com.tengu.app.hosting

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.tengu.app.common.module.visitor.IAppScreenVisitor
import com.tengu.app.framework.NavEntryProvider
import com.tengu.app.framework.nav.LocalNavBackStack
import com.tengu.app.framework.theme.TenguTheme
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.getKoin
import org.koin.compose.koinInject

@Composable
fun TenguApp() {
    val koin = getKoin()
    val navEntryProviders = remember { koin.getAll<NavEntryProvider>() }
    val screenVisitor = koinInject<IAppScreenVisitor>()
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
        screenVisitor.startNavKey,
    )
    TenguTheme {
        CompositionLocalProvider(
            LocalNavBackStack provides backStack
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Then add the view model store decorator
                    rememberViewModelStoreNavEntryDecorator()
                ),
                entryProvider = entryProvider {
                    for (provider in navEntryProviders) {
                        with(provider) { build() }
                    }
                },
            )
        }
    }
}
