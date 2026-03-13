package com.tengu.app.feature.acp

import com.tengu.app.common.module.visitor.IAppScreenVisitor
import com.tengu.app.framework.NavEntryProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val acpKoinModule = module {

    factoryOf<IAppScreenVisitor>(::AcpScreenVisitor)
    factoryOf(::AcpEntryProvider) bind NavEntryProvider::class
}
