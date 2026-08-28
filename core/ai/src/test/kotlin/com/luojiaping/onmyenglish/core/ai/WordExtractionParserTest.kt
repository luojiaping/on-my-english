package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.PartOfSpeech
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordExtractionParserTest {
    private val parser = WordExtractionParser(Json { ignoreUnknownKeys = true })

    @Test
    fun `parses fenced response and aliases`() {
        val raw = """
            Here is the result:
            ```json
            {"words":[{"word":"resilient","pos":"adjective","meaning":"able to recover","chinese":"有韧性的"}]}
            ```
        """.trimIndent()

        val result = parser.parse(raw)

        assertTrue(result is AppResult.Success)
        val word = (result as AppResult.Success).value.single()
        assertEquals("resilient", word.headword)
        assertEquals(PartOfSpeech.ADJECTIVE, word.partOfSpeech)
        assertEquals("有韧性的", word.translation)
    }

    @Test
    fun `balanced extraction ignores braces inside strings`() {
        val raw = "prefix {\"words\":[{\"headword\":\"brace\",\"definition\":\"a { mark }\"}]} suffix"

        val result = parser.parse(raw)

        assertTrue(result is AppResult.Success)
    }
}
