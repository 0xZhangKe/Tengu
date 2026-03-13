package com.tengu.app.feature.acp

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.tengu.app.feature.acp.screen.home.AcpHomeNavKey
import com.tengu.app.feature.acp.screen.home.AcpHomeScreen
import com.tengu.app.framework.NavEntryProvider
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

class AcpEntryProvider : NavEntryProvider {

    override fun EntryProviderScope<NavKey>.build() {
        entry<AcpHomeNavKey> {
            AcpHomeScreen()
        }
    }

    override fun PolymorphicModuleBuilder<NavKey>.polymorph() {
        subclass(AcpHomeNavKey::class)
    }
}
