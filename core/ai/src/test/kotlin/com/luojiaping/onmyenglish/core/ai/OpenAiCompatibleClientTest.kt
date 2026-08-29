package com.luojiaping.onmyenglish.core.ai

import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAiCompatibleClientTest {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun `local endpoint sends string content without authorization`() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("http://127.0.0.1:11434/v1/chat/completions", request.url.toString())
            assertFalse(request.headers.contains(HttpHeaders.Authorization))
            val body = (request.body as TextContent).text
            assertTrue(body.contains("\"content\":\"Reply with OK\""))
            respond(
                content = """
                    {"choices":[{"message":{"content":"OK"}}],"usage":{"prompt_tokens":3,"completion_tokens":1}}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val subject = OpenAiCompatibleClient(client, json)

        val result = subject.complete(
            settings = AiProviderSettings(
                baseUrl = "http://127.0.0.1:11434/v1",
                apiKey = "",
                chatModel = "local-model",
                visionModel = "local-model",
            ),
            request = LlmRequest(
                model = "local-model",
                temperature = 0.0,
                messages = listOf(
                    LlmMessage(LlmRole.USER, listOf(LlmContent.Text("Reply with OK"))),
                ),
            ),
        )

        assertTrue(result is AppResult.Success)
        val response = (result as AppResult.Success).value
        assertEquals("OK", response.text)
        assertEquals(3, response.promptTokens)
        assertEquals(1, response.completionTokens)
        client.close()
    }

    @Test
    fun `http endpoint with api key is rejected before network request`() = runBlocking {
        val engine = MockEngine { error("Network must not be called") }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val subject = OpenAiCompatibleClient(client, json)

        val result = subject.complete(
            settings = AiProviderSettings(
                baseUrl = "http://192.168.1.2:11434/v1",
                apiKey = "secret",
                chatModel = "local-model",
                visionModel = "local-model",
            ),
            request = LlmRequest(
                model = "local-model",
                temperature = 0.0,
                messages = listOf(
                    LlmMessage(LlmRole.USER, listOf(LlmContent.Text("hello"))),
                ),
            ),
        )

        assertTrue(result is AppResult.Failure)
        client.close()
    }
}
