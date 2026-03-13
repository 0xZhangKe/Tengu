package com.tengu.app.feature.acp

import androidx.navigation3.runtime.NavKey
import com.tengu.app.common.module.visitor.IAppScreenVisitor
import com.tengu.app.feature.acp.screen.home.AcpHomeNavKey

class AcpScreenVisitor : IAppScreenVisitor {

    override val startNavKey: NavKey = AcpHomeNavKey
}
