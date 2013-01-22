package org.devnull.client.spring

import org.devnull.client.spring.test.BaseHttpServerIntegrationTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = ['classpath:test-zuul-client-context.xml'])
@ActiveProfiles("pgp-personal")
class ZuulClientPgpPersonalIntegrationTest extends BaseHttpServerIntegrationTest {


    @Value("\${jdbc.zuul.password}")
    String password

    @Ignore("Don't run this unless you have a personal key setup (set GNUPGPASSWD and GNUPGHOME environment variables with a valid key)")
    @Test
    void shouldInjectDecryptedValue() {
        assert password == "supersecure"
    }

}
