package com.narde.keyboard

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

public fun TextFieldValue.applyKey(key: StdKey, insertNewlineOnEnter: Boolean = false): TextFieldValue {
    val edit = TextEditing.apply(text, selection.min, selection.max, key, insertNewlineOnEnter) ?: return this
    return TextFieldValue(edit.text, TextRange(edit.selStart, edit.selEnd))
}

public fun TextFieldState.applyKey(key: StdKey, insertNewlineOnEnter: Boolean = false): Boolean {
    val sel = selection
    val result = TextEditing.apply(text.toString(), sel.min, sel.max, key, insertNewlineOnEnter) ?: return false
    edit {
        replace(result.replaceStart, result.replaceEnd, result.replacement)
        selection = TextRange(result.selStart, result.selEnd)
    }
    return true
}

public val TextFieldValue.currentWord: String
    get() = TextEditing.currentWord(text, selection.min)

public val TextFieldValue.shouldAutoCapitalize: Boolean
    get() = selection.collapsed && TextEditing.shouldAutoCapitalize(text, selection.min)
