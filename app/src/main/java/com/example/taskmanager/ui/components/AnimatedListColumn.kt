package com.example.taskmanager.ui.components

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> AnimatedListColumn(
    items: List<T>,
    keySelector: (T) -> String,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    itemContent: @Composable (index: Int, item: T, isDragging: Boolean) -> Unit,
) {
    val listState = rememberLazyListState()
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = draggedItemIndex == null // Disable scroll while dragging
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> keySelector(item) }
        ) { index, item ->
            Box(
                modifier = Modifier
                    .animateItemPlacement(animationSpec = tween(300))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                draggedItemIndex = index
                                Log.d("draggedItemIndex", "Dragging item at index $draggedItemIndex")
                            }
                        )
                    }
                    .draggable(
                        interactionSource = interactionSource,
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            draggedItemIndex?.let { fromIndex ->
                                val newIndex = (fromIndex + (delta / 100)).coerceIn(0f,
                                    (items.size - 1).toFloat()
                                ).toInt()
                                if (newIndex != fromIndex) {
                                    scope.launch {
                                        onMove(fromIndex, newIndex)
                                        draggedItemIndex = newIndex
                                    }
                                }
                            }
                        },
                        onDragStarted = {
                            Log.d("drag", "Drag started for index $index")
                        },
                        onDragStopped = {
                            Log.d("drag", "Drag stopped")
                            draggedItemIndex = null
                        }
                    )
            ) {
                itemContent(index, item, index == draggedItemIndex)
            }
        }
    }
}





