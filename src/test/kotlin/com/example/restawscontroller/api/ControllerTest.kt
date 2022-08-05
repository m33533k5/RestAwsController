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
    private lateinit var rawIpDataController: AwsIpData
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
    fun `should find nothing`() {
        val method = controller.javaClass.getDeclaredMethod("responseDataIsNotEmpty", AwsIpData::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        createWireMockServerAndRawIpData(jsonStringEmpty)
        parameters[0] = rawIpDataController
        assertEquals(false, method.invoke(controller, *parameters))
    }

    @Test
    fun `should response with data`(){
        val method = controller.javaClass.getDeclaredMethod("responseDataIsNotEmpty", AwsIpData::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        createWireMockServerAndRawIpData(jsonStringAllIps)
        parameters[0] = rawIpDataController
        assertEquals(true, method.invoke(controller, *parameters))
    }

    @Test
    fun responseDataIsEmpty() {
        val method = controller.javaClass.getDeclaredMethod("responseDataIsNotEmpty", AwsIpData::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        createWireMockServerAndRawIpData(jsonStringIpv6Empty)
        parameters[0] = rawIpDataController
        assertEquals(true, method.invoke(controller, *parameters))


        createWireMockServerAndRawIpData(jsonStringIpEmpty)
        parameters[0] = rawIpDataController
        assertEquals(true, method.invoke(controller, *parameters))

        createWireMockServerAndRawIpData(jsonStringAllIps)
        parameters[0] = rawIpDataController
        assertEquals(true, method.invoke(controller, *parameters))

        createWireMockServerAndRawIpData(jsonStringEmpty)
        parameters[0] = rawIpDataController
        assertEquals(false, method.invoke(controller, *parameters))
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
        val method = client.javaClass.getDeclaredMethod("getResponseFromApi", AwsIpData::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = "${wireMockServer.baseUrl()}/response"
        val response = method.invoke(client, *parameters).toString()
        val jsonNodeList = client.createJacksonMapper(response)
        rawIpDataController = AwsIpData(
            createDate = jsonNodeList.findValue("createDate").textValue(),
            ipv6Prefixes = client.getIpv6Prefixes(jsonNodeList),
            prefixes = client.getIpPrefixes(jsonNodeList),
            syncToken = jsonNodeList.findValue("syncToken").textValue(),
        )
    }

    private fun createWireMockServerAndRawIpData(jsonString: String){
        createWireMockServer(jsonString)
        createRawIpData()
    }
}