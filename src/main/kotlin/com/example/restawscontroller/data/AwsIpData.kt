package com.example.restawscontroller.data

data class AwsIpData(
    val createDate: String,
    val ipv6Prefixes: List<Ipv6Prefixe>,
    val prefixes: List<Prefixe>,
    val syncToken: String
)