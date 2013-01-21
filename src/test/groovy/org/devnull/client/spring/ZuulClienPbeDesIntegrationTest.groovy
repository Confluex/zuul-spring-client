package org.devnull.client.spring

import org.devnull.client.spring.test.BaseHttpServerIntegrationTest
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ['classpath:test-zuul-client-context.xml'])
@ActiveProfiles("pbe-des")
class ZuulClienPbeDesIntegrationTest extends BaseHttpServerIntegrationTest {

    @Value("\${jdbc.zuul.password}")
    String password

    @Test
    void shouldInjectDecryptedValue() {
        assert password == "supersecure"
    }

    @BeforeClass
    static void cleanTestFile() {
        def testFile = getTestFile()
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @Test
    void shouldSaveFileToFilesystem() {
        def testFile = getTestFile()
        assert testFile.exists()
    }

    @Test
    void shouldStillBeEncrypted() {
        def properties = new Properties()
        def reader = new FileReader(testFile)
        properties.load(reader)
        reader.close()
        assert properties.getProperty("jdbc.zuul.password").startsWith("ENC(")
    }

    static protected File getTestFile() {
        def tmp = new File(System.getProperty("java.io.tmpdir"))
        return new File(tmp, "dev-test-des-config.properties")
    }

}
