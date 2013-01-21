package org.devnull.client.spring.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource

import java.security.Security

class PgpPropertiesDecryptorIntegrationTest {

    PgpPropertiesDecryptor decryptor

    @Before
    void createDecryptor() {
        Security.addProvider(new BouncyCastleProvider())
        def privateKey = new ClassPathResource("/pgp/acme/acme-private-key.asc")
        decryptor = new PgpPropertiesDecryptor(password: "", secretKeyRing: privateKey.file)
    }


    @Test
    void shouldDecryptPgpText() {
        def encrypted = new ClassPathResource("/pgp/acme/acme.encrypted.asc").inputStream.text
        assert decryptor.decrypt(encrypted) == "abc"
    }

    @Test
    void shouldDecryptPgpProperties() {
        def encrypted = new ClassPathResource("/pgp/acme/acme.encrypted.asc").inputStream.text
        def properties = new Properties()
        properties['text.unsecured'] = 'hello'
        properties['text.pgp.secured'] = "ENC($encrypted)"
        def results = decryptor.decrypt(properties)
        assert results['text.unsecured'] == 'hello'
        assert results['text.pgp.secured'] == 'abc'
    }
}
