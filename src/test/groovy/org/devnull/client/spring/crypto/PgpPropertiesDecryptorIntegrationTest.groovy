package org.devnull.client.spring.crypto

import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource

class PgpPropertiesDecryptorIntegrationTest {

    PgpPropertiesDecryptor decryptor

    @Before
    void createDecryptor() {
        def privateKey = new ClassPathResource("/pgp/acme/acme-private-key.asc")
        decryptor = new PgpPropertiesDecryptor(password: "", secretKeyRing: privateKey)
    }


    @Test
    void shouldDecryptPgpText() {
        def encrypted = new ClassPathResource("/pgp/acme/acme.encrypted.asc").inputStream.text
        assert decryptor.decrypt(encrypted) == "abc"
    }
}
