package com.narde.keyboard

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

// =====================================================================================================
// Общие типы
// =====================================================================================================

internal enum class KeyStyle { Normal, Special, Accent, Flat }

internal class KeyEnv(
    val colors: KeyboardColors,
    val config: KeyboardConfig,
    val state: KeyboardState,
    /** Высота видимой клавиши. */
    val keyHeight: Dp,
    val feedback: () -> Unit,
    val emit: (StdKey) -> Unit,
) {
    /** Высота зоны касания = клавиша + зазор. */
    val outerHeight: Dp get() = keyHeight + config.keyGap
    val upper: Boolean get() = state.shift != ShiftState.Off

    fun contentColor(style: KeyStyle): Color = when (style) {
        KeyStyle.Normal -> colors.keyContent
        KeyStyle.Special, KeyStyle.Flat -> colors.specialContent
        KeyStyle.Accent -> colors.accentContent
    }
}

/** Отправить символ хосту; одноразовый Shift сбрасывается. */
internal fun KeyEnv.typeChar(text: String) {
    emit(StdKey.CharKey(text))
    if (state.shift == ShiftState.Once) state.shift = ShiftState.Off
}

/** Заглавная версия строки; если длина меняется (ß → SS) — оставляем как есть. */
internal fun String.shifted(upper: Boolean): String {
    if (!upper) return this
    val u = uppercase()
    return if (u.length == length) u else this
}

@Composable
internal fun rememberKeyFeedback(config: KeyboardConfig): () -> Unit {
    val view = LocalView.current
    return remember(view, config.sound, config.haptics) {
        val f: () -> Unit = {
            if (config.sound) {
                try {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                } catch (_: Exception) {
                    // звук не критичен
                }
            }
            if (config.haptics) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        f
    }
}

@Composable
internal fun KeyLabel(text: String, color: Color, sizeSp: Float = 20f, bold: Boolean = false) {
    BasicText(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = sizeSp.sp,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        ),
        maxLines = 1,
        softWrap = false,
    )
}

// =====================================================================================================
// Низкоуровневая работа с указателем
// =====================================================================================================

private enum class Phase { Released, Cancelled }

private data class End(val phase: Phase, val position: Offset)

private suspend fun AwaitPointerEventScope.pollUntilEnd(
    id: PointerId,
    start: Offset,
    onMove: (Offset) -> Unit,
): End {
    var last = start
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes.find { it.id == id } ?: continue
        if (change.pressed) {
            last = change.position
            onMove(last)
            change.consume()
        } else {
            change.consume()
            return End(Phase.Released, change.position)
        }
    }
}

/** То же, но вернёт `null`, если за [timeoutMs] указатель не отпущен. */
private suspend fun AwaitPointerEventScope.pollUntilEndOrTimeout(
    id: PointerId,
    start: Offset,
    timeoutMs: Long,
    onMove: (Offset) -> Unit,
): End? = withTimeoutOrNull(timeoutMs) { pollUntilEnd(id, start, onMove) }

/** Палец отпущен в пределах клавиши (с запасом [slackPx]) — а не «уехал» на другую. */
private fun insideKey(pos: Offset, size: IntSize, slackPx: Float): Boolean =
    pos.x >= -slackPx && pos.x <= size.width + slackPx &&
        pos.y >= -slackPx && pos.y <= size.height + slackPx

// =====================================================================================================
// Каркас клавиши
// =====================================================================================================

@Composable
internal fun KeyFrame(
    env: KeyEnv,
    modifier: Modifier,
    style: KeyStyle,
    pressed: Boolean,
    description: String,
    tag: String,
    pointer: Modifier,
    onSemanticClick: () -> Unit,
    height: Dp = env.outerHeight,
    overlay: @Composable () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    val c = env.colors
    val bg = when (style) {
        KeyStyle.Normal -> if (pressed) c.keyPressed else c.key
        KeyStyle.Special -> if (pressed) c.specialPressed else c.special
        KeyStyle.Accent -> if (pressed) c.accentPressed else c.accent
        KeyStyle.Flat -> if (pressed) c.specialPressed else Color.Transparent
    }
    val half = env.config.keyGap / 2
    Box(
        modifier = modifier
            .height(height)
            .testTag(tag)
            .semantics {
                role = Role.Button
                contentDescription = description
                onClick {
                    onSemanticClick()
                    true
                }
            }
            .then(pointer)
            .padding(horizontal = half, vertical = half),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(env.config.cornerRadius))
                .background(bg)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
            content = content,
        )
        overlay()
    }
}

// =====================================================================================================
// Обычная клавиша-«кнопка» (срабатывает по отпусканию)
// =====================================================================================================

@Composable
internal fun TapKey(
    env: KeyEnv,
    modifier: Modifier,
    style: KeyStyle,
    description: String,
    tag: String,
    onTap: () -> Unit,
    height: Dp = env.outerHeight,
    content: @Composable BoxScope.() -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val tap by rememberUpdatedState(onTap)
    val fb by rememberUpdatedState(env.feedback)
    KeyFrame(
        env = env,
        modifier = modifier,
        style = style,
        pressed = pressed,
        description = description,
        tag = tag,
        height = height,
        onSemanticClick = { tap() },
        pointer = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                pressed = true
                fb()
                val end = pollUntilEnd(down.id, down.position) { }
                pressed = false
                if (end.phase == Phase.Released && insideKey(end.position, size, 8.dp.toPx())) tap()
            }
        },
        content = content,
    )
}

// =====================================================================================================
// Буквенная клавиша: превью, долгое нажатие, выбор варианта скольжением
// =====================================================================================================

/** Геометрия popup с вариантами; все значения в пикселях относительно левого верхнего угла клавиши. */
private class AltGeo(
    val items: List<String>,
    val offsetX: Int,
    val offsetY: Int,
    val itemWpx: Float,
    val padPx: Float,
) {
    fun indexAt(xInKey: Float): Int {
        val rel = xInKey - offsetX - padPx
        return floor(rel / itemWpx).toInt().coerceIn(0, items.size - 1)
    }
}

private class CharCallbacks(
    val hasAlternates: Boolean,
    val longPressMs: Long,
    val onPress: () -> Unit,
    val onLongPress: (Float) -> Unit,
    val onSlide: (Float) -> Unit,
    val onRelease: (committed: Boolean, longPressed: Boolean, x: Float) -> Unit,
)

private suspend fun AwaitPointerEventScope.charGesture(cb: CharCallbacks, slackPx: Float) {
    val down = awaitFirstDown(requireUnconsumed = false)
    down.consume()
    cb.onPress()
    var last = down.position
    var longPressed = false
    val end: End = if (cb.hasAlternates) {
        val timed = pollUntilEndOrTimeout(down.id, last, cb.longPressMs) { last = it }
        if (timed != null) {
            timed
        } else {
            longPressed = true
            cb.onLongPress(last.x)
            pollUntilEnd(down.id, last) {
                last = it
                cb.onSlide(it.x)
            }
        }
    } else {
        pollUntilEnd(down.id, last) { }
    }
    val committed = end.phase == Phase.Released && (longPressed || insideKey(end.position, size, slackPx))
    cb.onRelease(committed, longPressed, end.position.x)
}

private fun buildAltGeo(
    alts: List<String>,
    keyLeft: Float,
    keyWidth: Float,
    windowWidth: Float,
    itemHeightPx: Float,
    density: Density,
): AltGeo {
    val itemW = with(density) { 40.dp.toPx() }
    val pad = with(density) { 4.dp.toPx() }
    val gap = with(density) { 2.dp.toPx() }
    val popupW = alts.size * itemW + 2 * pad
    val leftAnchored = keyLeft + popupW <= windowWidth
    val offX = if (leftAnchored) 0f else keyWidth - popupW
    val items = if (leftAnchored) alts else alts.asReversed()
    val popupH = itemHeightPx + 2 * pad
    return AltGeo(items, offX.roundToInt(), (-(popupH + gap)).roundToInt(), itemW, pad)
}

@Composable
internal fun CharKeyButton(
    env: KeyEnv,
    spec: KeySpec,
    modifier: Modifier,
    hint: String? = null,
    extraAlternates: List<String> = emptyList(),
    style: KeyStyle = KeyStyle.Normal,
) {
    val cfg = env.config
    val upper = env.upper
    val shown = spec.label.shifted(upper)
    val alts: List<String> =
        if (cfg.longPressAlternates) (spec.alternates + extraAlternates).distinct().map { it.shifted(upper) }
        else emptyList()

    val density = LocalDensity.current
    val view = LocalView.current
    var pressed by remember { mutableStateOf(false) }
    var geo by remember { mutableStateOf<AltGeo?>(null) }
    var altIndex by remember { mutableIntStateOf(0) }
    val bounds = remember { FloatArray(2) } // [left в окне, ширина]

    val callbacks = CharCallbacks(
        hasAlternates = alts.isNotEmpty(),
        longPressMs = cfg.longPressTimeoutMs,
        onPress = {
            pressed = true
            env.feedback()
        },
        onLongPress = { x ->
            val g = buildAltGeo(
                alts, bounds[0], bounds[1], view.rootView.width.toFloat(),
                with(density) { env.keyHeight.toPx() }, density,
            )
            geo = g
            altIndex = g.indexAt(x)
        },
        onSlide = { x -> geo?.let { altIndex = it.indexAt(x) } },
        onRelease = { committed, longPressed, x ->
            pressed = false
            val g = geo
            val text = if (longPressed && g != null) g.items[g.indexAt(x)] else shown
            geo = null
            if (committed) env.typeChar(text)
        },
    )
    val cb by rememberUpdatedState(callbacks)
    val currentShown by rememberUpdatedState(shown)

    KeyFrame(
        env = env,
        modifier = modifier,
        style = style,
        pressed = pressed,
        description = shown,
        tag = "key_" + spec.label,
        onSemanticClick = { env.typeChar(currentShown) },
        pointer = Modifier
            .onGloballyPositioned {
                val b = it.boundsInWindow()
                bounds[0] = b.left
                bounds[1] = b.width
            }
            .pointerInput(Unit) {
                awaitEachGesture { charGesture(cb, 8.dp.toPx()) }
            },
        overlay = {
            val g = geo
            if (g != null) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(g.offsetX, g.offsetY),
                    properties = PopupProperties(focusable = false),
                ) {
                    AltStrip(env, g, altIndex)
                }
            } else if (pressed && cfg.keyPreview && !cfg.secureMode && shown.isNotBlank()) {
                val windowW = view.rootView.width.toFloat()
                val bubbleW = maxOf(bounds[1] * 1.25f, with(density) { 44.dp.toPx() })
                val bubbleH = with(density) { (env.outerHeight * 1.2f).toPx() }
                val left = (bounds[0] + (bounds[1] - bubbleW) / 2f).coerceIn(0f, maxOf(0f, windowW - bubbleW))
                val offX = (left - bounds[0]).roundToInt()
                val offY = (-(bubbleH + with(density) { 2.dp.toPx() })).roundToInt()
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(offX, offY),
                    properties = PopupProperties(focusable = false),
                ) {
                    val shape = RoundedCornerShape(10.dp)
                    Box(
                        Modifier
                            .size(with(density) { bubbleW.toDp() }, with(density) { bubbleH.toDp() })
                            .shadow(4.dp, shape)
                            .background(env.colors.popup, shape),
                        contentAlignment = Alignment.Center,
                    ) {
                        KeyLabel(shown, env.colors.popupContent, sizeSp = 28f, bold = true)
                    }
                }
            }
        },
        content = {
            if (hint != null && !cfg.secureMode) {
                Box(Modifier.fillMaxSize()) {
                    KeyLabel(shown, env.contentColor(style), sizeSp = 20f)
                    Box(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                        KeyLabel(hint, env.colors.hint, sizeSp = 10f)
                    }
                }
            } else {
                KeyLabel(shown, env.contentColor(style))
            }
        },
    )
}

@Composable
private fun AltStrip(env: KeyEnv, g: AltGeo, selected: Int) {
    val c = env.colors
    val density = LocalDensity.current
    val shape = RoundedCornerShape(10.dp)
    val itemW = with(density) { g.itemWpx.toDp() }
    val pad = with(density) { g.padPx.toDp() }
    Row(
        Modifier
            .shadow(6.dp, shape)
            .background(c.popup, shape)
            .padding(pad),
    ) {
        g.items.forEachIndexed { i, text ->
            val isSel = i == selected
            Box(
                Modifier
                    .width(itemW)
                    .height(env.keyHeight)
                    .background(if (isSel) c.popupSelected else Color.Transparent, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                KeyLabel(text, if (isSel) c.popupSelectedContent else c.popupContent, sizeSp = 22f)
            }
        }
    }
}

// =====================================================================================================
// ⌫ с авто-повтором
// =====================================================================================================

private const val WORD_DELETE_AFTER = 12

@Composable
internal fun BackspaceKey(env: KeyEnv, modifier: Modifier, style: KeyStyle = KeyStyle.Special) {
    var pressed by remember { mutableStateOf(false) }
    val emit by rememberUpdatedState(env.emit)
    val fb by rememberUpdatedState(env.feedback)
    val doRepeat = env.config.backspaceRepeat
    val words = env.config.wordDeleteOnLongHold
    KeyFrame(
        env = env,
        modifier = modifier,
        style = style,
        pressed = pressed,
        description = env.config.strings.backspace,
        tag = "key_backspace",
        onSemanticClick = { emit(StdKey.Backspace) },
        pointer = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                pressed = true
                fb()
                emit(StdKey.Backspace)
                if (doRepeat) {
                    var count = 0
                    var delayMs = 85L
                    var end = pollUntilEndOrTimeout(down.id, down.position, 400L) { }
                    while (end == null) {
                        count++
                        emit(if (words && count > WORD_DELETE_AFTER) StdKey.DeleteWord else StdKey.Backspace)
                        end = pollUntilEndOrTimeout(down.id, down.position, delayMs) { }
                        delayMs = (delayMs * 9 / 10).coerceAtLeast(35L)
                    }
                } else {
                    pollUntilEnd(down.id, down.position) { }
                }
                pressed = false
            }
        },
    ) {
        KeyIconView(KeyIcon.Backspace, env.contentColor(style))
    }
}

// =====================================================================================================
// Пробел: тап = пробел, горизонтальный свайп = курсор
// =====================================================================================================

@Composable
internal fun SpaceKey(env: KeyEnv, modifier: Modifier, label: String) {
    var pressed by remember { mutableStateOf(false) }
    val emit by rememberUpdatedState(env.emit)
    val fb by rememberUpdatedState(env.feedback)
    val swipe = env.config.spaceSwipeCursor
    KeyFrame(
        env = env,
        modifier = modifier,
        style = KeyStyle.Normal,
        pressed = pressed,
        description = env.config.strings.space,
        tag = "key_space",
        onSemanticClick = { emit(StdKey.Space) },
        pointer = Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                down.consume()
                pressed = true
                fb()
                val startX = down.position.x
                val slop = viewConfiguration.touchSlop
                val step = 14.dp.toPx()
                var moved = false
                var lastX = startX
                var acc = 0f
                val end = pollUntilEnd(down.id, down.position) { p ->
                    if (swipe) {
                        if (!moved && abs(p.x - startX) > slop * 2) {
                            moved = true
                            lastX = p.x
                        } else if (moved) {
                            acc += p.x - lastX
                            lastX = p.x
                            while (acc >= step) {
                                acc -= step
                                emit(StdKey.CursorMove(1))
                            }
                            while (acc <= -step) {
                                acc += step
                                emit(StdKey.CursorMove(-1))
                            }
                        }
                    }
                }
                pressed = false
                if (!moved && end.phase == Phase.Released && insideKey(end.position, size, 8.dp.toPx())) {
                    emit(StdKey.Space)
                }
            }
        },
    ) {
        KeyLabel(label, env.colors.hint, sizeSp = 13f)
    }
}
