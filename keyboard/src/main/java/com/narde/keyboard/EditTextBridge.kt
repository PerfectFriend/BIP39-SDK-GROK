package com.narde.keyboard

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

public fun EditText.applyKey(key: StdKey, insertNewlineOnEnter: Boolean = false): Boolean {
    val edit = TextEditing.apply(
        text = text.toString(),
        selStart = selectionStart.coerceAtLeast(0),
        selEnd = selectionEnd.coerceAtLeast(0),
        key = key,
        insertNewlineOnEnter = insertNewlineOnEnter,
    ) ?: return false
    if (edit.replaceStart != edit.replaceEnd || edit.replacement.isNotEmpty()) {
        text.replace(edit.replaceStart, edit.replaceEnd, edit.replacement)
    }
    setSelection(edit.selStart.coerceAtMost(text.length), edit.selEnd.coerceAtMost(text.length))
    return true
}

public fun EditText.useInAppKeyboard() {
    setShowSoftInputOnFocus(false)
    hideSystemKeyboard()
}

public fun EditText.hideSystemKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}
