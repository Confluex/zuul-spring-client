package org.devnull.client.spring.test

import org.junit.AfterClass
import org.junit.BeforeClass
import org.mortbay.jetty.Server
import org.springframework.core.io.ClassPathResource
import org.devnull.client.spring.ZuulPropertiesFactoryBean

abstract class BaseHttpServerIntegrationTest {
    static Server server

    @BeforeClass
    static void createServer() {
        server = new Server(8081)
        def resources = [
                "/zuul/settings/dev/test-des-config.properties" :new ClassPathResource("/responses/mock-server-response-des.properties"),
                "/zuul/settings/prod/test-aes-config.properties" :new ClassPathResource("/responses/mock-server-response-aes.properties"),
                "/zuul/settings/prod/test-pgp-config.properties" :new ClassPathResource("/responses/mock-server-response-pgp.properties")
        ]
        server.handler = new ResourceRequestHandler(resources: resources)
        server.start()
    }

    @AfterClass
    static void stopServer() {
        server.stop()
    }

}
