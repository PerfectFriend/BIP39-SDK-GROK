package com.narde.keyboard

internal class EmojiCategory(val icon: String, val name: String, val emoji: List<String>)

/** Встроенный набор эмодзи (без сети и без внешних зависимостей). Только базовые, широко поддерживаемые. */
internal object EmojiData {
    private const val V = "\uFE0F" // variation selector-16: эмодзи-представление

    private fun list(s: String): List<String> = s.split(' ').filter { it.isNotEmpty() }

    val categories: List<EmojiCategory> = listOf(
        EmojiCategory(
            "😀", "Smileys",
            list(
                "😀 😃 😄 😁 😆 😅 😂 🤣 🙂 🙃 😉 😊 😇 🥰 😍 🤩 😘 😗 😚 😋 😛 😜 🤪 😝 🤑 🤗 🤭 🤔 🤐 😐 😑 😶 " +
                    "😏 😒 🙄 😬 😌 😔 😪 😴 😷 🤒 🤕 🤢 🥵 🥶 😵 🤯 😎 🤓 😕 😟 🙁 😮 😲 😳 🥺 😨 😰 😢 😭 😱 😖 " +
                    "😞 😓 😩 😫 🥱 😤 😡 😠 🤬 😈 💀 💩 🤡 👻 👽 🤖",
            ),
        ),
        EmojiCategory(
            "👍", "People",
            list(
                "👍 👎 👌 ✌$V 🤞 🤟 🤘 🤙 👈 👉 👆 👇 ☝$V ✋ 🤚 🖐$V 🖖 👋 🤝 🙏 👏 🙌 👐 🤲 💪 ✍$V 🤳 💅 👀 👅 👄 " +
                    "👶 🧑 👦 👧 👨 👩 🧓 🤷 🤦 🙋 🙇 💁 🙅 🙆",
            ),
        ),
        EmojiCategory(
            "❤$V", "Symbols",
            list(
                "❤$V 🧡 💛 💚 💙 💜 🖤 🤍 🤎 💔 ❣$V 💕 💞 💓 💗 💖 💘 💝 💯 💢 💥 💫 💦 💨 ✨ ⭐ 🌟 🔥 ✅ ❌ ❓ ❗ " +
                    "⚡ ⚠$V ♻$V 🔒 🔓 🔑 ➕ ➖ ➡$V ⬅$V ⬆$V ⬇$V",
            ),
        ),
        EmojiCategory(
            "🐻", "Nature",
            list(
                "🐶 🐱 🐭 🐹 🐰 🦊 🐻 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🐔 🐧 🐦 🦆 🦉 🐺 🐴 🦄 🐝 🦋 🐌 🐞 🐢 🐍 🐙 🐬 🐳 " +
                    "🌸 🌹 🌻 🌲 🌴 🍀 🍁 🌈 ☀$V 🌙 ☁$V ❄$V 🌊",
            ),
        ),
        EmojiCategory(
            "🍔", "Food",
            list(
                "🍎 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🥑 🥦 🥕 🌽 🥔 🍞 🧀 🥚 🍳 🥓 🍔 🍟 🍕 🌭 🥪 🌮 🍝 " +
                    "🍜 🍣 🍩 🍪 🎂 🍰 🍫 🍿 ☕ 🍵 🍺 🍷 🥂 🍾",
            ),
        ),
        EmojiCategory(
            "💡", "Objects",
            list(
                "⚽ 🏀 🏈 ⚾ 🎾 🏐 🎱 🎮 🎲 🎯 🎵 🎶 🎤 🎧 🎸 🎬 🏆 🥇 🚗 🚕 🚌 🚀 ✈$V 🚲 🏠 🏝$V ⏰ 📱 💻 📷 💡 " +
                    "🔔 📌 📎 ✉$V 📦 🎁 🎉 🎈 💰 💳 🔍",
            ),
        ),
    )
}
