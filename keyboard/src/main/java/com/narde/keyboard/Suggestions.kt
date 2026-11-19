package com.narde.keyboard

/** Источник подсказок. Реализация целиком на стороне приложения: сеть/диск библиотеке не нужны. */
public fun interface SuggestionProvider {
    /** До [limit] подсказок для начатого слова [prefix] (регистр префикса не обязан совпадать). */
    public fun suggest(prefix: String, limit: Int): List<String>
}

/**
 * Локальные подсказки на словаре в памяти: ничего не пишется на диск и не уходит в сеть.
 * Слова ранжируются по частоте; [learn] повышает вес слова. Потокобезопасно.
 *
 * Порядок слов в стартовом словаре задаёт начальный приоритет (раньше — важнее).
 * Если нужна персистентность — храните список выученных слов сами и подавайте в конструктор.
 */
public class InMemorySuggestions(words: Collection<String> = emptyList()) : SuggestionProvider {
    private val lock = Any()
    private val weights = HashMap<String, Int>()

    init {
        val size = words.size
        words.forEachIndexed { i, w ->
            val key = w.trim().lowercase()
            if (key.isNotEmpty()) weights[key] = maxOf(weights[key] ?: 0, size - i)
        }
    }

    /** Запомнить использование слова (повышает приоритет). Слишком короткие слова игнорируются. */
    public fun learn(word: String) {
        val key = word.trim().lowercase()
        if (key.length < 2) return
        synchronized(lock) { weights[key] = (weights[key] ?: 0) + LEARN_BOOST }
    }

    public fun forget(word: String) {
        synchronized(lock) { weights.remove(word.trim().lowercase()) }
    }

    public fun clear() {
        synchronized(lock) { weights.clear() }
    }

    public override fun suggest(prefix: String, limit: Int): List<String> {
        if (prefix.isEmpty() || limit <= 0) return emptyList()
        val p = prefix.lowercase()
        val capitalize = prefix.first().isUpperCase()
        val allCaps = prefix.length > 1 && prefix.all { !it.isLetter() || it.isUpperCase() }
        val found = synchronized(lock) {
            weights.entries
                .filter { it.key.startsWith(p) && it.key != p }
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .take(limit)
                .map { it.key }
        }
        return found.map {
            when {
                allCaps -> it.uppercase()
                capitalize -> it.replaceFirstChar { c -> c.uppercase() }
                else -> it
            }
        }
    }

    private companion object {
        const val LEARN_BOOST = 10
    }
}

/** Небольшие стартовые словари самых частых слов (чтобы подсказки работали «из коробки»). */
public object CommonWords {
    public val Russian: List<String> = (
        "и в не на я что он с как это по но они мы из к у так за все то же от вы бы ещё уже или если только " +
            "когда может быть есть был была были будет нет да очень тоже потом сейчас сегодня завтра вчера привет " +
            "пока спасибо пожалуйста хорошо плохо давай можно нужно надо почему зачем где куда откуда кто какой " +
            "который тогда здесь там тут всегда никогда иногда сообщение сообщения отправить получить позвонить " +
            "напиши ответь скажи подожди скоро быстро медленно работа дома встреча время минут час день неделя"
        ).split(' ')

    public val English: List<String> = (
        "the be to of and a in that have i it for not on with he as you do at this but his by from they we say " +
            "her she or an will my one all would there their what so up out if about who get which go me when make " +
            "can like time no just him know take people into year your good some could them see other than then now " +
            "look only come its over think also back after use two how our work first well way even new want because " +
            "any these give day most us hello thanks please sorry message send call later today tomorrow yesterday " +
            "meeting question answer problem great awesome maybe okay"
        ).split(' ')
}
