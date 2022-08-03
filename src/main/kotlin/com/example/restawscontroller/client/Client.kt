package com.example.restawscontroller.client

import com.example.restawscontroller.data.AwsIpData
import com.example.restawscontroller.data.Ipv6Prefixe
import com.example.restawscontroller.data.Prefixe
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.net.URL

@Component
class Client {

    var ips: MutableList<String> = mutableListOf()

    fun main(region: String): AwsIpData {
        val rawIpData = getDataFromAPI()

        return when(region){
            "ALL" -> rawIpData
            else -> {
                AwsIpData(
                    createDate = rawIpData.createDate,
                    syncToken = rawIpData.syncToken,
                    ipv6Prefixes = getIpv6RegionByFilter(rawIpData, region),
                    prefixes = getRegionByFilter(rawIpData, region)
                )
            }
        }
    }

    fun getDataFromAPI(): AwsIpData {
        val response: String = URL("https://ip-ranges.amazonaws.com/ip-ranges.json").readText()

        val mapper = jacksonObjectMapper()
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
        val jsonNodeList = mapper.readTree(response)

        val ipv6Prefixes = jsonNodeList.findValue("ipv6_prefixes").map { ipv6_prefixesNode ->
            Ipv6Prefixe(
                ipv6Prefix = ipv6_prefixesNode.get("ipv6_prefix").textValue(),
                region = ipv6_prefixesNode.get("region").textValue(),
                service = ipv6_prefixesNode.get("service").textValue(),
                networkBorderGroup = ipv6_prefixesNode.get("network_border_group").textValue(),
            )
        }

        val ipPrefixe = jsonNodeList.findValue("prefixes").toList().map { prefixesNode ->
            Prefixe(
                ipPrefix = prefixesNode.get("ip_prefix").textValue(),
                region = prefixesNode.get("region").textValue(),
                service = prefixesNode.get("service").textValue(),
                networkBorderGroup = prefixesNode.get("network_border_group").textValue(),
            )
        }

        return AwsIpData(
            createDate = jsonNodeList.findValue("createDate").textValue(),
            ipv6Prefixes = ipv6Prefixes,
            prefixes = ipPrefixe,
            syncToken = jsonNodeList.findValue("syncToken").textValue(),
        )
    }

    fun getIpv6RegionByFilter(rawIpData: AwsIpData, region: String): List<Ipv6Prefixe> {
        return rawIpData.ipv6Prefixes.filter {
            it.region?.substringBefore("-")?.equals(region, ignoreCase = true) ?: false
        }
    }

    fun getRegionByFilter(rawIpData: AwsIpData, region: String): List<Prefixe> {
        return rawIpData.prefixes.filter {
            it.region?.substringBefore("-")?.equals(region, ignoreCase = true) ?: false
        }
    }

    fun getPossibleIps(rawIpData: AwsIpData){
        for(item in 0 until rawIpData.prefixes.size){
            ips.add(item, rawIpData.prefixes[item].ipPrefix.toString())
        }
    }

    fun getPossibleIpv6Ips(rawIpData: AwsIpData){
        for(item in 0 until rawIpData.ipv6Prefixes.size){
            ips.add(item, rawIpData.ipv6Prefixes[item].ipv6Prefix.toString())
        }
    }

}
