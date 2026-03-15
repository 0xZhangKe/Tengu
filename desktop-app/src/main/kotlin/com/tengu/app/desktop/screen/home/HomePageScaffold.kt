package com.tengu.app.desktop.screen.home

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tengu.app.framework.utils.dpToPx
import com.tengu.app.framework.utils.pxToDp
import kotlinx.coroutines.launch
import java.awt.Cursor
import kotlin.math.max
import kotlin.math.min

private const val drawerWidthInScreenRatio = 0.24F
private const val maxDrawerWidthInScreenRatio = 0.4F
private val drawerMinWidth = 140.dp
private val drawerResizeHandleWidth = 10.dp
private val drawerOuterPadding = 4.dp
private val contentOuterPadding = 6.dp
private val minContentWidth = 320.dp

@Composable
fun HomePageScaffold(
    modifier: Modifier,
    drawer: @Composable BoxScope.() -> Unit,
    topBar: @Composable BoxScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    var drawerWidth by remember { mutableStateOf(0.dp) }
    var containerWidthPx by remember { mutableStateOf(0) }
    val resizeCursor = remember { PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)) }
    val drawerDragState = rememberDraggableState { delta ->
        if (containerWidthPx == 0) {
            return@rememberDraggableState
        }
        val currentWidthPx = drawerWidth.dpToPx(density)
        val targetWidthPx = (currentWidthPx + delta).coerceIn(
            minimumValue = drawerMinWidth.dpToPx(density),
            maximumValue = computeDrawerMaxWidthPx(
                containerWidthPx = containerWidthPx,
                density = density,
            ),
        )
        drawerWidth = targetWidthPx.pxToDp(density)
    }

    Scaffold(modifier = modifier.onSizeChanged {
        containerWidthPx = it.width
        if (drawerWidth == 0.dp) {
            coroutineScope.launch {
                val drawerWidthInPx = drawerWidth.dpToPx(density)
                val targetWidth = min(
                    it.width * drawerWidthInScreenRatio,
                    computeDrawerMaxWidthPx(
                        containerWidthPx = it.width,
                        density = density,
                    ),
                )
                Animatable(drawerWidthInPx)
                    .animateTo(targetWidth) {
                        drawerWidth = value.pxToDp(density)
                    }
            }
        }
    }) {
        val contentStartPadding = drawerOuterPadding + drawerWidth + drawerResizeHandleWidth

        Box(
            modifier = Modifier
                .padding(
                    start = drawerOuterPadding,
                    top = drawerOuterPadding,
                    bottom = drawerOuterPadding,
                )
                .fillMaxHeight()
                .width(drawerWidth)
        ) {
            drawer()
        }
        Box(
            modifier = Modifier
                .padding(top = drawerOuterPadding, bottom = drawerOuterPadding)
                .padding(start = drawerOuterPadding + drawerWidth)
                .fillMaxHeight()
                .width(drawerResizeHandleWidth)
                .pointerHoverIcon(resizeCursor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = drawerDragState,
                ),
        )

        Box(
            modifier = Modifier.fillMaxWidth()
                .padding(start = contentStartPadding)
        ) {
            topBar()
        }
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(
                    start = contentStartPadding,
                    top = contentOuterPadding,
                    end = contentOuterPadding * 2,
                    bottom = contentOuterPadding,
                )
        ) {
            content()
        }
    }
}

private fun computeDrawerMaxWidthPx(
    containerWidthPx: Int,
    density: androidx.compose.ui.unit.Density,
): Float {
    val ratioWidthPx = containerWidthPx * maxDrawerWidthInScreenRatio
    val maxWidthByContentPx =
        containerWidthPx - minContentWidth.dpToPx(density) - drawerResizeHandleWidth.dpToPx(density) -
            (drawerOuterPadding.dpToPx(density) * 2)
    return max(
        drawerMinWidth.dpToPx(density),
        min(ratioWidthPx, maxWidthByContentPx),
    )
}
