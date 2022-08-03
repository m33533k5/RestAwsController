package com.example.restawscontroller.api

import com.example.restawscontroller.client.Client
import com.example.restawscontroller.data.AwsIpData
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ip")
class Controller(
    private val client: Client,
) {
    //http://localhost:8081/ip/?region=ALL
    @RequestMapping("/")
    fun getUrlContent(@RequestParam region: String): ResponseEntity<String> {
        var response: AwsIpData? = client.main(region = region)
        if (response != null) {
            client.getPossibleIps(response)
            client.getPossibleIpv6Ips(response)
        };
        return when(getDataIsEmpty(response)){
            true -> ResponseEntity<String>("Keine Ergebnisse gefunden", HttpStatus.OK)
            false -> for (item in client.ips) ResponseEntity(item.toString(), HttpStatus.OK)
            //false -> ResponseEntity<String>(client.ips.forEach{e -> ResponseEntity<String>(e,HttpStatus.OK)}.toString(), HttpStatus.OK)
        }

    }

    fun getDataIsEmpty(data: AwsIpData?): Boolean {
        return data?.prefixes?.isEmpty() == true && data?.ipv6Prefixes?.isEmpty()
    }
}
