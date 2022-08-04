package com.example.restawscontroller.api

import com.example.restawscontroller.client.Client
import com.example.restawscontroller.data.AwsIpData
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
        val response: AwsIpData = client.main(region = region)

        return when(client.getHttpStatusCodeAsString()){
            "Seite erreichbar" -> outputData(response)
            else -> ResponseEntity<String>(client.getHttpStatusCodeAsString(), HttpStatus.OK)
        }

    }

    fun responseDataIsNotEmpty(data: AwsIpData?): Boolean {
        return !(data?.prefixes?.isEmpty() == true && data.ipv6Prefixes.isEmpty())
    }

    fun outputData(response: AwsIpData): ResponseEntity<String>{
        return when (responseDataIsNotEmpty(response)) {
            false -> {
                ResponseEntity<String>("Keine Ergebnisse gefunden.", HttpStatus.OK)
            }
            true -> {
                ResponseEntity<String>(client.getAllIps(response).toString(), HttpStatus.OK)
            }
        }
    }
}
