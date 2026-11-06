package com.example.bip39onboarding.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 20 built-in visual presets (Cyberpunk × Van Gogh × Gaudí family).
 * Pick once at host setup: [OnboardingThemePresets.config].
 */
object OnboardingThemePresets {

    data class Preset(
        val id: String,
        val nameEn: String,
        val nameRu: String,
        val nameEs: String,
        val colors: OnboardingColors
    )

    private fun hex(s: String): Color = Color(android.graphics.Color.parseColor(s))

    private fun palette(
        void: String,
        panel: String,
        elevated: String,
        neon: String,
        magenta: String,
        amber: String,
        text: String,
        muted: String,
        m1: String,
        m2: String,
        m3: String,
        m4: String,
        m5: String,
        onNeon: String = "#0A0618",
        danger: String = "#FF4D6D"
    ) = OnboardingColors(
        void = hex(void),
        panel = hex(panel),
        panelElevated = hex(elevated),
        strokeNeon = hex(neon),
        strokeMagenta = hex(magenta),
        glowAmber = hex(amber),
        textPrimary = hex(text),
        textMuted = hex(muted),
        textOnNeon = hex(onNeon),
        mosaicTeal = hex(m1),
        mosaicCoral = hex(m2),
        mosaicGold = hex(m3),
        mosaicIndigo = hex(m4),
        mosaicCream = hex(m5),
        danger = hex(danger)
    )

    val all: List<Preset> = listOf(
        Preset("neon_night", "Neon Night", "Неоновая ночь", "Noche neón",
            palette("#0A0618", "#12102A", "#1A1838", "#00F0FF", "#FF2D95", "#F4C15D", "#F2EFE9", "#9A94B8",
                "#00F0FF", "#FF2D95", "#F4C15D", "#7C5CFF", "#2EE6A6")),
        Preset("starry_amber", "Starry Amber", "Звёздный янтарь", "Ámbar estrellado",
            palette("#0B0A14", "#16122A", "#221C3A", "#F4C15D", "#FF6B9A", "#FFE08A", "#FFF8E7", "#A89BC8",
                "#F4C15D", "#FF6B9A", "#FFE08A", "#7C5CFF", "#00D4FF")),
        Preset("gaudi_mosaic", "Gaudí Mosaic", "Мозаика Гауди", "Mosaico Gaudí",
            palette("#0D1520", "#142030", "#1C2E42", "#2EE6A6", "#FF6B4A", "#F4C15D", "#EAF6F0", "#8AA8B8",
                "#2EE6A6", "#FF6B4A", "#F4C15D", "#4DA3FF", "#C77DFF")),
        Preset("cyber_sakura", "Cyber Sakura", "Кибер-сакура", "Sakura cyber",
            palette("#12010F", "#1E0A1A", "#2A1228", "#FF4DA6", "#00E5FF", "#FFB4D9", "#FFE8F4", "#B894A8",
                "#FF4DA6", "#00E5FF", "#FFB4D9", "#C77DFF", "#FF6B9A")),
        Preset("deep_ocean", "Deep Ocean", "Глубокий океан", "Océano profundo",
            palette("#020B14", "#0A1628", "#12243A", "#00D4FF", "#3D7CFF", "#7FDBDA", "#E6F7FF", "#7A9AB0",
                "#00D4FF", "#3D7CFF", "#2EE6A6", "#7FDBDA", "#5C7CFA")),
        Preset("violet_haze", "Violet Haze", "Фиолетовая дымка", "Niebla violeta",
            palette("#0C0618", "#180F2E", "#241845", "#B794F6", "#FF7AD9", "#E9D5FF", "#F5F0FF", "#A094C0",
                "#B794F6", "#FF7AD9", "#7C5CFF", "#00F0FF", "#F4C15D")),
        Preset("ember_grid", "Ember Grid", "Угольная сетка", "Rejilla ember",
            palette("#100806", "#1C100C", "#2A1810", "#FF6B2D", "#FF2D55", "#FFC857", "#FFF0E6", "#B89888",
                "#FF6B2D", "#FF2D55", "#FFC857", "#FF8A5B", "#C44536")),
        Preset("mint_terminal", "Mint Terminal", "Мятный терминал", "Terminal menta",
            palette("#061410", "#0C2218", "#123020", "#3DFFB5", "#00C2A8", "#B8FFDF", "#E8FFF4", "#7AA898",
                "#3DFFB5", "#00C2A8", "#00F0FF", "#F4C15D", "#7C5CFF")),
        Preset("noir_gold", "Noir Gold", "Нуар и золото", "Noir y oro",
            palette("#0A0A0A", "#161616", "#222222", "#D4AF37", "#C9A227", "#F5E6A8", "#F5F0E6", "#9A9488",
                "#D4AF37", "#C9A227", "#F5E6A8", "#8B7355", "#E8D5A3")),
        Preset("arctic_neon", "Arctic Neon", "Арктический неон", "Neón ártico",
            palette("#060C14", "#0E1A28", "#162838", "#7DF9FF", "#A5F3FC", "#E0F7FA", "#F0FDFF", "#8AA8B8",
                "#7DF9FF", "#A5F3FC", "#00F0FF", "#B794F6", "#F4C15D")),
        Preset("magenta_pulse", "Magenta Pulse", "Пурпурный пульс", "Pulso magenta",
            palette("#100018", "#1C0A28", "#2A1438", "#FF2D95", "#FF6BCB", "#FFB8E0", "#FFF0F8", "#B894A8",
                "#FF2D95", "#FF6BCB", "#00F0FF", "#7C5CFF", "#F4C15D")),
        Preset("forest_glitch", "Forest Glitch", "Лесной глитч", "Glitch bosque",
            palette("#061208", "#0C1E10", "#142C18", "#7CFF6B", "#2EE6A6", "#C8FFB8", "#F0FFE8", "#88A890",
                "#7CFF6B", "#2EE6A6", "#F4C15D", "#00F0FF", "#FF6B4A")),
        Preset("sunset_arch", "Sunset Arch", "Закатная арка", "Arco atardecer",
            palette("#140A08", "#221410", "#302018", "#FF8A5B", "#FF2D55", "#FFD166", "#FFF5EB", "#B8A090",
                "#FF8A5B", "#FF2D55", "#FFD166", "#FF6B9A", "#F4C15D")),
        Preset("indigo_ink", "Indigo Ink", "Индиго-чернила", "Tinta índigo",
            palette("#080814", "#101028", "#181840", "#5C7CFA", "#748FFC", "#BAC8FF", "#EEF1FF", "#8890B0",
                "#5C7CFA", "#748FFC", "#00F0FF", "#B794F6", "#FF6B9A")),
        Preset("copper_wire", "Copper Wire", "Медный провод", "Cable cobre",
            palette("#120C08", "#1E1610", "#2A2018", "#E07A3D", "#C45C26", "#F0C0A0", "#FFF4EC", "#A89080",
                "#E07A3D", "#C45C26", "#F4C15D", "#FF6B4A", "#8B5A2B")),
        Preset("pearl_dark", "Pearl Dark", "Тёмный перламутр", "Perla oscura",
            palette("#0E0E12", "#18181F", "#24242E", "#E8E0F0", "#C4B5FD", "#F5F0FF", "#FAFAFC", "#9A94A8",
                "#E8E0F0", "#C4B5FD", "#00F0FF", "#FF6B9A", "#F4C15D")),
        Preset("toxic_lime", "Toxic Lime", "Токсичный лайм", "Lima tóxica",
            palette("#0A1206", "#121E0A", "#1A2C10", "#B8FF00", "#7CFF00", "#E8FF99", "#F5FFE6", "#90A878",
                "#B8FF00", "#7CFF00", "#00F0FF", "#FF2D95", "#F4C15D")),
        Preset("royal_trencadis", "Royal Trencadís", "Королевский тренкадис", "Trencadís real",
            palette("#0A0814", "#141028", "#1E1838", "#FFD700", "#9B59B6", "#00CED1", "#FFF8E7", "#A094B8",
                "#FFD700", "#9B59B6", "#00CED1", "#FF6B9A", "#2EE6A6")),
        Preset("mono_cyan", "Mono Cyan", "Моно-циан", "Mono cian",
            palette("#070B0E", "#0E161C", "#152028", "#00E5FF", "#00B8D4", "#B2EBF2", "#E0F7FA", "#78909C",
                "#00E5FF", "#00B8D4", "#80DEEA", "#4DD0E1", "#26C6DA")),
        Preset("classic_void", "Classic Void", "Классическая пустота", "Vacío clásico",
            DefaultOnboardingColors)
    )

    fun byId(id: String): Preset = all.find { it.id == id } ?: all.first()

    fun ids(): List<String> = all.map { it.id }

    fun config(
        presetId: String = "neon_night",
        reduceMotion: Boolean = false,
        showStepProgress: Boolean = true
    ): OnboardingThemeConfig {
        return OnboardingThemeConfig(
            colors = byId(presetId).colors,
            reduceMotion = reduceMotion,
            showStepProgress = showStepProgress
        )
    }
}
