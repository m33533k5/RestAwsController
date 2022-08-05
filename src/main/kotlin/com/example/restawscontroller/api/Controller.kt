package com.example.restawscontroller.api

import com.example.restawscontroller.client.Client
import com.example.restawscontroller.data.AwsIpData
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
    fun getUrlContent(@RequestParam region: String) {
        val response: AwsIpData = client.main(region = region)

        return when(client.getHttpStatusCodeAsString()){
            "Seite erreichbar" -> outputData(response)
            else -> println(client.getHttpStatusCodeAsString())
        }

    }

    private fun responseDataIsNotEmpty(data: AwsIpData?): Boolean {
        return !(data?.prefixes?.isEmpty() == true && data.ipv6Prefixes.isEmpty())
    }

    private fun outputData(response: AwsIpData){
        return when (responseDataIsNotEmpty(response)) {
            false -> {
                println("Keine Ergebnisse gefunden.")
            }
            true -> {
                println(client.getAllIps(response).joinToString(separator = "\n"))
            }
        }
    }
}
