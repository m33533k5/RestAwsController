package com.example.restawscontroller.client

import com.example.restawscontroller.data.AwsIpData
import com.example.restawscontroller.data.Ipv6Prefixe
import com.example.restawscontroller.data.Prefixe
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.net.URL

@Component
class Client(
    webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder.build()
    private var listOfAllIps: MutableList<String> = mutableListOf()

    fun main(region: String): AwsIpData {
        val rawIpData = getDataFromAPI()
        return createAwsIpData(region, rawIpData)
    }

    fun getDataFromAPI(): AwsIpData {
        val url = "https://ip-ranges.amazonaws.com/ip-ranges.json"

        val response = getResponseFromApi(url)
        val jsonNodeList = createJacksonMapper(response)

        return AwsIpData(
            createDate = jsonNodeList.findValue("createDate").textValue(),
            ipv6Prefixes = getIpv6Prefixes(jsonNodeList),
            prefixes = getIpPrefixes(jsonNodeList),
            syncToken = jsonNodeList.findValue("syncToken").textValue(),
        )
    }

    fun getStatusCode(url: String): Mono<String> = webClient
        .get()
        .uri(url)
        .exchangeToMono { it.rawStatusCode().toMono() }
        .map { checkUrlCode(it) }

    fun checkUrlCode(responseCode: Int): String = when (responseCode) {
        in 200..299 -> "erreichbar"
        in 400..499 -> "erreichbar ${errorMessage(responseCode, "Client")}"
        in 500..599 -> "nicht erreichbar ${errorMessage(responseCode, "Server")}"
        in 300..399 -> "Weiterleitungen werden nicht zugelassen"
        else -> "Nicht definierter Status"
    }

    fun errorMessage(responseCode: Int, responsible: String?) = "(${responsible} error - Code: ${responseCode})"

    fun getResponseFromApi(url: String): String {
        return URL(url).readText()
    }

    fun createJacksonMapper(response: String): JsonNode {
        val mapper = jacksonObjectMapper()
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        return mapper.readTree(response)
    }

    fun getIpv6Prefixes(jsonNodeList: JsonNode): List<Ipv6Prefixe> {
        return jsonNodeList.findValue("ipv6_prefixes").map { ipv6_prefixesNode ->
            Ipv6Prefixe(
                ipv6Prefix = ipv6_prefixesNode.get("ipv6_prefix").textValue(),
                region = ipv6_prefixesNode.get("region").textValue(),
                service = ipv6_prefixesNode.get("service").textValue(),
                networkBorderGroup = ipv6_prefixesNode.get("network_border_group").textValue(),
            )
        }
    }

    fun getIpPrefixes(jsonNodeList: JsonNode): List<Prefixe> {
        return jsonNodeList.findValue("prefixes").toList().map { prefixesNode ->
            Prefixe(
                ipPrefix = prefixesNode.get("ip_prefix").textValue(),
                region = prefixesNode.get("region").textValue(),
                service = prefixesNode.get("service").textValue(),
                networkBorderGroup = prefixesNode.get("network_border_group").textValue(),
            )
        }
    }

    fun createAwsIpData(region: String, rawIpData: AwsIpData): AwsIpData {
        return when (region) {
            "ALL" -> rawIpData
            else -> {
                AwsIpData(
                    createDate = rawIpData.createDate,
                    syncToken = rawIpData.syncToken,
                    ipv6Prefixes = getIpv6RegionByFilter(rawIpData, region),
                    prefixes = getIpRegionByFilter(rawIpData, region)
                )
            }
        }
    }

    fun getIpv6RegionByFilter(rawIpData: AwsIpData, region: String): List<Ipv6Prefixe> {
        return rawIpData.ipv6Prefixes.filter {
            it.region?.substringBefore("-")?.equals(region, ignoreCase = true) ?: false
        }
    }

    fun getIpRegionByFilter(rawIpData: AwsIpData, region: String): List<Prefixe> {
        return rawIpData.prefixes.filter {
            it.region?.substringBefore("-")?.equals(region, ignoreCase = true) ?: false
        }
    }

    fun getPossibleIps(rawIpData: AwsIpData) {
        for (item in 0 until rawIpData.prefixes.size) {
            listOfAllIps.add(item, rawIpData.prefixes[item].ipPrefix.toString())
        }
    }

    fun getPossibleIpv6Ips(rawIpData: AwsIpData) {
        for (item in 0 until rawIpData.ipv6Prefixes.size) {
            listOfAllIps.add(item, rawIpData.ipv6Prefixes[item].ipv6Prefix.toString())
        }
    }

    fun getAllIps(rawIpData: AwsIpData): MutableList<String> {
        getPossibleIps(rawIpData)
        getPossibleIpv6Ips(rawIpData)
        return listOfAllIps
    }
}
