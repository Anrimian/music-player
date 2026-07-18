package com.github.anrimian.musicplayer.ui.utils.compose.components.swipe.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.platform.ViewConfiguration
import kotlin.math.abs
import kotlin.math.hypot

internal suspend fun PointerInputScope.detectSwipeAction(
    isRtl: Boolean,
    isSwipeFromStart: Boolean = false,
    onDrag: (delta: Float) -> Unit,
    onEnd: (velocity: Float) -> Unit,
    onCancel: () -> Unit
) {
    val velocityTracker = VelocityTracker()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var overSlop = 0f

        val dragPointerId = down.id
        var totalDx = 0f
        var totalDy = 0f
        var passedSlop = false
        var matchedDirection = false

        while (!passedSlop) {
            val event = awaitPointerEvent()
            val change = event.changes.find { it.id == dragPointerId }
            if (change == null || change.isConsumed) {
                break
            }

            if (change.changedToUp() || change.isConsumed) {
                break
            }
            
            // ignore long press
            val timeDelta = change.uptimeMillis - down.uptimeMillis
            if (timeDelta > viewConfiguration.longPressTimeoutMillis) {
                break
            }

            val dx = change.position.x - down.position.x
            val dy = change.position.y - down.position.y
            totalDx = dx
            totalDy = dy

            val dist = hypot(dx, dy)
            if (dist > viewConfiguration.touchSlop) {
                passedSlop = true


                val isStartEndSwipe = if (isRtl) dx < 0 else dx > 0
                val isEndStartSwipe = if (isRtl) dx > 0 else dx < 0

                val isActionDirection = if (isSwipeFromStart) isStartEndSwipe else isEndStartSwipe
                val isHorizontal = abs(dx) > abs(dy) * 2 // ~26.5 degrees

                if (isActionDirection && isHorizontal) {
                    matchedDirection = true
                    overSlop = if (isSwipeFromStart) {
                         if (isRtl) -dx else dx
                    } else {
                         if (isRtl) -dx else dx
                    }

                    change.consume()
                }
            }
        }

        if (passedSlop && matchedDirection) {
            velocityTracker.resetTracking()

            val initialDelta = if (isRtl) -totalDx else totalDx

            onDrag(initialDelta)

            if (
                drag(dragPointerId) { change ->
                    change.consume()
                    velocityTracker.addPointerInputChange(change)
                    val delta = change.position.x - change.previousPosition.x
                    val adjustedDelta = if (isRtl) -delta else delta
                    onDrag(adjustedDelta)
                }
            ) {
                val velocity = velocityTracker.calculateVelocity()
                val xVelocity = velocity.x
                val adjustedVelocity = if (isRtl) -xVelocity else xVelocity
                onEnd(adjustedVelocity)
            } else {
                onCancel()
            }
        }
    }
}
