package com.example.restawscontroller.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import reactor.kotlin.test.test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
@AutoConfigureWebClient
internal class ClientTest(
    @Autowired val client: Client
) {
    @Autowired private lateinit var wireMockServer: WireMockServer

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    /*
    @ParameterizedTest
    @MethodSource("ranges")
    fun shouldReturnValidStatusResponse(input: Int, expected: String) {

        wireMockServer.stubFor(
            WireMock.get("/response")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(input)
                )
        )

        client
            .getStatusCode("${wireMockServer.baseUrl()}/response")
            .test()
            .expectNext(expected)
            .verifyComplete()

        verify(getRequestedFor(urlEqualTo("/response")))
    }

    companion object {

        @JvmStatic
        fun ranges() = listOf(
            200..299 to "Seite erreichbar",
            300..399 to "Weiterleitung",
            400..499 to "Seite erreichbar (Client error - Code: %s)",
            500..599 to "Seite nicht erreichbar (Server error - Code: %s)",
        )
            .flatMap { (range, string) ->
                range.map { Arguments.of(it, string.format(it)) }
            }
    }

     */

    @Test
    fun main() {
    }

    @Test
    fun getDataFromAPI() {
    }

    @Test
    fun getResponseFromApi() {
    }

    @Test
    fun createJacksonMapper() {
    }

    @Test
    fun getIpv6Prefixes() {
    }

    @Test
    fun getIpPrefixes() {
    }

    @Test
    fun createAwsIpData() {
    }

    @Test
    fun getIpv6RegionByFilter() {
    }

    @Test
    fun getIpRegionByFilter() {
    }

    @Test
    fun getPossibleIps() {
    }

    @Test
    fun getPossibleIpv6Ips() {
    }

    @Test
    fun getAllIps() {
    }
}