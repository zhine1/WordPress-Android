package org.wordpress.android.ui.reader.discover.interests

data class TagUiState(
    val title: String,
    val slug: String,
    val isChecked: Boolean = false,
    val visible: Boolean = true,
    val maxWidth: Int = Int.MAX_VALUE
)
