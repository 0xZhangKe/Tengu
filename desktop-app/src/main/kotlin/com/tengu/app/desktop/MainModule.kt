package com.tengu.app.desktop

import com.tengu.app.desktop.screen.home.HomeViewModel
import com.tengu.app.framework.NavEntryProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::HomeViewModel)

    factoryOf(::MainEntryProvider) bind NavEntryProvider::class
}
