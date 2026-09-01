package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.common.AppResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuiltInDeckParserTest {
    private val parser = BuiltInDeckParser()

    @Test
    fun `parses valid deck asset`() {
        val json = """
            {"id":"cet4","name":"四级词汇","badge":"四","tag":"cet4","count":1,
             "words":[{"w":"state","ph":"steit","tr":"n. 状态","pos":"NOUN","frq":31}]}
        """.trimIndent()

        val result = parser.parse(json)

        assertTrue(result is AppResult.Success)
        val asset = (result as AppResult.Success).value
        assertEquals("cet4", asset.id)
        assertEquals(1, asset.words.size)
        assertEquals("state", asset.words[0].w)
        assertEquals("NOUN", asset.words[0].pos)
    }

    @Test
    fun `rejects empty or malformed asset`() {
        assertTrue(parser.parse("{") is AppResult.Failure)
        assertTrue(
            parser.parse("""{"id":"x","name":"n","badge":"b","tag":"t","count":0,"words":[]}""")
                is AppResult.Failure,
        )
    }

    @Test
    fun `part of speech falls back to other`() {
        assertEquals(
            com.luojiaping.onmyenglish.core.model.PartOfSpeech.VERB,
            parser.partOfSpeech("VERB"),
        )
        assertEquals(
            com.luojiaping.onmyenglish.core.model.PartOfSpeech.OTHER,
            parser.partOfSpeech("???"),
        )
    }
}
