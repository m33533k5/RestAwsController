package com.example.restawscontroller.client

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.event.ContextClosedEvent

class WireMockContextInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {

        val wmServer = WireMockServer(WireMockConfiguration().dynamicPort())
        wmServer.start()
        configureFor(wmServer.port())

        applicationContext.beanFactory.registerSingleton("wireMock", wmServer)

        applicationContext.addApplicationListener {
            if (it is ContextClosedEvent) {
                wmServer.stop()
            }
        }
    }

}