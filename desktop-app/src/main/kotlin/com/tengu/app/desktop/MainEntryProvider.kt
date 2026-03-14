package com.tengu.app.desktop

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tengu.app.desktop.screen.home.HomeNavKey
import com.tengu.app.desktop.screen.home.HomeScreen
import com.tengu.app.framework.NavEntryProvider
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

class MainEntryProvider: NavEntryProvider {

    override fun EntryProviderScope<NavKey>.build() {
        entry<HomeNavKey> {
            HomeScreen()
        }
    }

    override fun PolymorphicModuleBuilder<NavKey>.polymorph() {
        subclass(HomeNavKey::class)
    }
}
