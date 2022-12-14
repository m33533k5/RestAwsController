package com.example.restawscontroller.client

import com.example.restawscontroller.data.AwsIpData
import com.example.restawscontroller.data.Ipv6Prefixe
import com.example.restawscontroller.data.Prefixe
import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import reactor.kotlin.test.test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [WireMockContextInitializer::class])
@AutoConfigureWebClient
internal class ClientTest(
    @Autowired val client: Client
) {
    @Autowired
    private lateinit var wireMockServer: WireMockServer
    private lateinit var rawIpDataClient: AwsIpData
    private lateinit var testResponse: String
    private lateinit var jsonNode: JsonNode
    private var listOfAllIps: MutableList<String> = mutableListOf()
    private val jsonString: String =
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

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @ParameterizedTest
    @MethodSource("httpResponseCodeRanges")
    fun `should return valid status response`(input: Int, expected: String) {
        wireMockServer.stubFor(
            get("/response")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(input)
                )
        )

        client
            .getHttpStatusCode("${wireMockServer.baseUrl()}/response")
            .test()
            .expectNext(expected)
            .verifyComplete()

        verify(getRequestedFor(urlEqualTo("/response")))
    }

    companion object {

        @JvmStatic
        fun httpResponseCodeRanges() = listOf(
            200..299 to "Seite erreichbar",
            300..399 to "Weiterleitung",
            400..499 to "Seite erreichbar (Client error - Code: %s)",
            500..599 to "Seite nicht erreichbar (Server error - Code: %s)",
        )
            .flatMap { (range, string) ->
                range.map { Arguments.of(it, string.format(it)) }
            }
    }

    @Test
    fun `should get correct Data from API`() {
        createWireMockServer()

        val response = getAccessToPrivateMethod("getResponseFromApi", "${wireMockServer.baseUrl()}/response")
        val jsonNodeList = client.createJacksonMapper(response)

        assertEquals("2022-08-03-01-13-07", jsonNodeList.findValue("createDate").textValue())
        assertEquals("1659489187", jsonNodeList.findValue("syncToken").textValue())
        assertEquals(
            "[Ipv6Prefixe(ipv6Prefix=2a05:d07a:a000::/40, networkBorderGroup=eu-south-1, region=eu-south-1, service=AMAZON)]",
            client.getIpv6Prefixes(jsonNodeList).toString()
        )
        assertEquals(
            "[Prefixe(ipPrefix=13.34.37.64/27, networkBorderGroup=ap-southeast-4, region=ap-southeast-4, service=AMAZON)]",
            client.getIpPrefixes(jsonNodeList).toString()
        )
    }

    @Test
    fun `should get correct error message`() {
        val method = client.javaClass.getDeclaredMethod("errorMessage", Int::class.java, String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)
        parameters[0] = 123
        parameters[1] = "Server"
        val expect = "(Server error - Code: 123)"
        assertEquals(expect, method.invoke(client, *parameters))
    }

    @Test
    fun `should get correct response`() {
        wireMockServer.stubFor(
            get("/response")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("Test")
                )
        )

        testResponse = getAccessToPrivateMethod("getResponseFromApi", "${wireMockServer.baseUrl()}/response")
        assertEquals("Test", testResponse)
    }

    @Test
    fun `should create a Json Node`() {
        jsonNode = client.createJacksonMapper(jsonString)
        assertEquals("1659489187", jsonNode.findValue("syncToken").textValue())
        assertEquals("2022-08-03-01-13-07", jsonNode.findValue("createDate").textValue())
    }

    @Test
    fun `should get correct ipv6 list`() {
        jsonNode = client.createJacksonMapper(jsonString)
        val ipv6List: List<Ipv6Prefixe> = client.getIpv6Prefixes(jsonNode)
        assertEquals("2a05:d07a:a000::/40", ipv6List[0].ipv6Prefix.toString())
        assertEquals("eu-south-1", ipv6List[0].region.toString())
        assertEquals("AMAZON", ipv6List[0].service.toString())
        assertEquals("eu-south-1", ipv6List[0].networkBorderGroup.toString())
    }

    @Test
    fun `should get correct ip list`() {
        jsonNode = client.createJacksonMapper(jsonString)
        val ipv6List: List<Prefixe> = client.getIpPrefixes(jsonNode)
        assertEquals("13.34.37.64/27", ipv6List[0].ipPrefix.toString())
        assertEquals("ap-southeast-4", ipv6List[0].region.toString())
        assertEquals("AMAZON", ipv6List[0].service.toString())
        assertEquals("ap-southeast-4", ipv6List[0].networkBorderGroup.toString())
    }

    @Test
    fun `should create data with ips`() {
        createWireMockServerAndRawIpData()

        var allData = client.createAwsIpData("ALL", rawIpDataClient)
        assertEquals(1, allData.ipv6Prefixes.size)
        assertEquals(1, allData.prefixes.size)
        assertEquals("2022-08-03-01-13-07", allData.createDate)
        assertEquals("1659489187", allData.syncToken)

        allData = client.createAwsIpData("EU", rawIpDataClient)
        assertEquals(1, allData.ipv6Prefixes.size)
        assertEquals(0, allData.prefixes.size)
    }

    @Test
    fun `should get correct ipv6 with filter`() {
        createWireMockServerAndRawIpData()

        assertEquals(
            "[Ipv6Prefixe(ipv6Prefix=2a05:d07a:a000::/40, networkBorderGroup=eu-south-1, region=eu-south-1, service=AMAZON)]",
            getAccessToPrivateMethod("getIpv6RegionByFilter", rawIpDataClient, "eu")
        )

        assertEquals("[]", getAccessToPrivateMethod("getIpv6RegionByFilter", rawIpDataClient, "qw"))
    }

    @Test
    fun `should get correct ip with filter`() {
        createWireMockServerAndRawIpData()

        assertEquals(
            "[Prefixe(ipPrefix=13.34.37.64/27, networkBorderGroup=ap-southeast-4, region=ap-southeast-4, service=AMAZON)]",
            getAccessToPrivateMethod("getIpRegionByFilter", rawIpDataClient, "ap")
        )
        assertEquals("[]", getAccessToPrivateMethod("getIpRegionByFilter", rawIpDataClient, "qw"))
    }

    @Test
    fun `should get all possible ip data`() {
        createWireMockServerAndRawIpData()

        val mockClient: Client = mock(Client::class.java)
        mockClient.getPossibleIps(rawIpDataClient)
        verify(mockClient, times(1)).getPossibleIps(rawIpDataClient)
    }

    @Test
    fun `should get all possible ipv6 data`() {
        createWireMockServerAndRawIpData()

        val mockClient: Client = mock(Client::class.java)
        mockClient.getPossibleIpv6Ips(rawIpDataClient)
        verify(mockClient, times(1)).getPossibleIpv6Ips(rawIpDataClient)
    }

    @Test
    fun `should get possible ip and ipv6 data in a list`() {
        createWireMockServerAndRawIpData()

        listOfAllIps = client.getAllIps(rawIpDataClient)

        assertEquals("2a05:d07a:a000::/40", listOfAllIps[0])
        assertEquals("13.34.37.64/27", listOfAllIps[1])
    }

    private fun createWireMockServer() {
        wireMockServer.stubFor(
            get("/response")
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonString)
                )
        )
    }

    private fun getAccessToPrivateMethod(methodName: String, parameterString: String): String {
        val method = client.javaClass.getDeclaredMethod(methodName, String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(1)
        parameters[0] = parameterString
        return method.invoke(client, *parameters).toString()
    }

    private fun getAccessToPrivateMethod(
        methodName: String,
        parameterAwsIpData: AwsIpData,
        parameterString: String
    ): String {
        val method = client.javaClass.getDeclaredMethod(methodName, AwsIpData::class.java, String::class.java)
        method.isAccessible = true
        val parameters = arrayOfNulls<Any>(2)
        parameters[0] = parameterAwsIpData
        parameters[1] = parameterString
        return method.invoke(client, *parameters).toString()
    }

    private fun createRawIpData() {
        val response = getAccessToPrivateMethod("getResponseFromApi", "${wireMockServer.baseUrl()}/response")
        val jsonNodeList = client.createJacksonMapper(response)
        rawIpDataClient = AwsIpData(
            createDate = jsonNodeList.findValue("createDate").textValue(),
            ipv6Prefixes = client.getIpv6Prefixes(jsonNodeList),
            prefixes = client.getIpPrefixes(jsonNodeList),
            syncToken = jsonNodeList.findValue("syncToken").textValue(),
        )
    }

    private fun createWireMockServerAndRawIpData() {
        createWireMockServer()
        createRawIpData()
    }
}