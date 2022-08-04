package com.example.restawscontroller.api

import com.example.restawscontroller.client.Client
import com.example.restawscontroller.client.WireMockContextInitializer
import com.example.restawscontroller.data.AwsIpData
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
@AutoConfigureWebClient
internal class ControllerTest(
    @Autowired val client: Client,
    @Autowired val controller: Controller
) {
    @Autowired private lateinit var wireMockServer: WireMockServer
    private lateinit var rawIpData: AwsIpData
    private val jsonStringAllIps: String =
        """
                {
                  "syncToken": "1659489187",
                  "createDate": "2022-08-03-01-13-07",
                  "prefixes": [
                    {
                      "ip_prefix": "13.34.37.64/27",
                      "region": "ap-southeast-4",
                      "service": "AMAZON",
                      "network_border_group": "ap-southeast-4"
                    }
                ],
                  "ipv6_prefixes": [
                    {
                      "ipv6_prefix": "2a05:d07a:a000::/40",
                      "region": "eu-south-1",
                      "service": "AMAZON",
                      "network_border_group": "eu-south-1"
                    }
                  ]
                }
            """.trimIndent()
    private val jsonStringIpv6Empty: String =
        """
                {
                  "syncToken": "1659489187",
                  "createDate": "2022-08-03-01-13-07",
                  "prefixes": [
                    {
                      "ip_prefix": "13.34.37.64/27",
                      "region": "ap-southeast-4",
                      "service": "AMAZON",
                      "network_border_group": "ap-southeast-4"
                    }
                ],
                  "ipv6_prefixes": [
                  ]
                }
            """.trimIndent()
    private val jsonStringIpEmpty: String =
        """
                {
                  "syncToken": "1659489187",
                  "createDate": "2022-08-03-01-13-07",
                  "prefixes": [
                ],
                  "ipv6_prefixes": [
                    {
                      "ipv6_prefix": "2a05:d07a:a000::/40",
                      "region": "eu-south-1",
                      "service": "AMAZON",
                      "network_border_group": "eu-south-1"
                    }
                  ]
                }
            """.trimIndent()
    private val jsonStringEmpty: String =
        """
                {
                  "syncToken": "1659489187",
                  "createDate": "2022-08-03-01-13-07",
                  "prefixes": [
                ],
                  "ipv6_prefixes": [
                  ]
                }
            """.trimIndent()

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @Test
    fun `should response with Keine Ergebnisse gefunden`() {
        createWireMockServerAndRawIpData(jsonStringEmpty)
        assertEquals(false, controller.responseDataIsNotEmpty(rawIpData))
        assertEquals("Keine Ergebnisse gefunden.", controller.getUrlContent("asd").body.toString())
        assertEquals("200 OK", controller.getUrlContent("asd").statusCode.toString())
    }

    @Test
    fun `should response with data`(){
        createWireMockServerAndRawIpData(jsonStringAllIps)
        assertEquals(true, controller.responseDataIsNotEmpty(rawIpData))
        assertNotNull(controller.getUrlContent("eu").body.toString())
        assertEquals("200 OK", controller.getUrlContent("eu").statusCode.toString())
    }

    @Test
    fun responseDataIsEmpty() {
        createWireMockServerAndRawIpData(jsonStringIpv6Empty)
        assertEquals(true, controller.responseDataIsNotEmpty(rawIpData))

        createWireMockServerAndRawIpData(jsonStringIpEmpty)
        assertEquals(true, controller.responseDataIsNotEmpty(rawIpData))

        createWireMockServerAndRawIpData(jsonStringAllIps)
        assertEquals(true, controller.responseDataIsNotEmpty(rawIpData))

        createWireMockServerAndRawIpData(jsonStringEmpty)
        assertEquals(false, controller.responseDataIsNotEmpty(rawIpData))
    }

    private fun createWireMockServer(jsonString: String){
        wireMockServer.stubFor(
            WireMock.get("/response")
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonString)
                )
        )
    }

    private fun createRawIpData(){
        val response = client.getResponseFromApi("${wireMockServer.baseUrl()}/response")
        val jsonNodeList = client.createJacksonMapper(response)
        rawIpData = AwsIpData(
            createDate = jsonNodeList.findValue("createDate").textValue(),
            ipv6Prefixes = client.getIpv6Prefixes(jsonNodeList),
            prefixes = client.getIpPrefixes(jsonNodeList),
            syncToken = jsonNodeList.findValue("syncToken").textValue(),
        )
    }

    fun createWireMockServerAndRawIpData(jsonString: String){
        createWireMockServer(jsonString)
        createRawIpData()
    }
}