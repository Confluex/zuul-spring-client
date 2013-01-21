package org.devnull.client.spring

import org.devnull.client.spring.test.BaseHttpServerIntegrationTest
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ['classpath:test-zuul-client-context.xml'])
@ActiveProfiles("pbe-aes")
class ZuulClientPbeAesIntegrationTest extends BaseHttpServerIntegrationTest {


    @Value("\${jdbc.zuul.password}")
    String password

    @Test
    void shouldInjectDecryptedValue() {
        assert password == "supersecure"
    }

    @Test
    void shouldNotSaveFileToFilesystem() {
        def tmp = new File(System.getProperty("java.io.tmpdir"))
        def testFile = new File(tmp, "prod-test-aes-config.properties")
        assert !testFile.exists()
    }
}
