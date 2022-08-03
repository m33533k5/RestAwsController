package com.example.restawscontroller.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
@AutoConfigureWebClient
internal class ClientTest(
    @Autowired val client: Client
) {
    private lateinit var wireMockServer: WireMockServer

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @Test fun shouldReturnValidStatusCode(){
        wireMockServer.stubFor(
            WireMock.get("/ip/")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")
                )
        )

        client
            .getResponseFromApi("https://ip-ranges.amazonaws.com/ip-ranges.json")
            .test()
            .verifyComplete()
    }

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