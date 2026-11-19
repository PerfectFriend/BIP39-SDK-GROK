package com.narde.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Страница эмодзи: вкладки категорий → сетка → нижний ряд (ABC / пробел / ⌫ / ⏎).
 * Высота панели = высоте буквенной страницы, чтобы клавиатура не «прыгала».
 */
@Composable
internal fun EmojiPanel(env: KeyEnv, areaRows: Int) {
    val secure = env.config.secureMode
    val recents = env.state.recentEmoji
    val categories = buildList {
        if (recents.isNotEmpty() && !secure) add(EmojiCategory("🕘", "Recent", recents))
        addAll(EmojiData.categories)
    }
    var selected by rememberSaveable { mutableStateOf(0) }
    val current = selected.coerceIn(0, categories.lastIndex)
    val gridHeight = env.outerHeight * (areaRows - 2)
    val s = env.config.strings

    // вкладки
    Row(Modifier.fillMaxWidth()) {
        categories.forEachIndexed { i, cat ->
            TapKey(
                env = env,
                modifier = Modifier.weight(1f),
                style = if (i == current) KeyStyle.Special else KeyStyle.Flat,
                description = cat.name,
                tag = "emoji_tab_$i",
                onTap = { selected = i },
            ) {
                BasicText(cat.icon, style = TextStyle(fontSize = 20.sp))
            }
        }
    }

    // сетка
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = Modifier.fillMaxWidth().height(gridHeight),
    ) {
        items(categories[current].emoji) { e ->
            Box(
                Modifier
                    .height(44.dp)
                    .clickable {
                        env.feedback()
                        env.typeChar(e)
                        if (!secure) env.state.addRecentEmoji(e)
                    },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(e, style = TextStyle(fontSize = 26.sp))
            }
        }
    }

    // нижний ряд
    Row(Modifier.fillMaxWidth()) {
        TapKey(
            env = env, modifier = Modifier.weight(1.5f), style = KeyStyle.Special,
            description = s.letters, tag = "key_mode",
            onTap = { env.state.page = KeyboardPage.Letters },
        ) { KeyLabel("ABC", env.colors.specialContent, sizeSp = 14f, bold = true) }

        SpaceKey(env, Modifier.weight(6f), label = "")

        BackspaceKey(env, Modifier.weight(1.5f))

        TapKey(
            env = env, modifier = Modifier.weight(1.5f), style = KeyStyle.Accent,
            description = s.enter, tag = "key_enter",
            onTap = { env.emit(StdKey.Enter) },
        ) { KeyIconView(KeyIcon.Enter, env.colors.accentContent) }
    }
}
