package org.uigrammar.fuzzer

import org.uigrammar.grammar.Production

data class SearchData(
    val node: Node,
    val baseExpansion: Production,
    val currentExpansion: Production,
    val currentDepth: Int
)