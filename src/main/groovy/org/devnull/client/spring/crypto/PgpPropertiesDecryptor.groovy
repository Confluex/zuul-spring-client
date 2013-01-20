package org.devnull.client.spring.crypto

import groovy.util.logging.Slf4j
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPCompressedData
import org.bouncycastle.openpgp.PGPEncryptedDataList
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPLiteralData
import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPPrivateKey
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection
import org.bouncycastle.openpgp.PGPUtil
import org.springframework.core.io.Resource

import java.security.Security

@Slf4j
public class PgpPropertiesDecryptor {

    Resource secretKeyRing
    String password

    Properties decrypt(Properties properties) {
        def decrypted = new Properties()
        properties.each { k, v ->
            def encrypted = v =~ /ENC\(.*\)/
            decrypted[k] = encrypted ? decrypt(encrypted[1].toString()) : v
        }
        return decrypted
    }


    protected PGPPrivateKey findSecretKey(InputStream keyIn, long keyID) {
        def pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
        return pgpSec?.getSecretKey(keyID)?.extractPrivateKey(password.toCharArray(), "BC");
    }

    protected String decrypt(String value) {
        Security.addProvider(new BouncyCastleProvider());
        def is = PGPUtil.getDecoderStream(new ByteArrayInputStream(value.bytes));

        def pgpF = new PGPObjectFactory(is);
        def o = pgpF.nextObject();
        PGPEncryptedDataList enc = o instanceof PGPEncryptedDataList ? o : pgpF.nextObject() as PGPEncryptedDataList

        def encryptedData = enc.encryptedDataObjects.next() as PGPPublicKeyEncryptedData
        def key = findSecretKey(secretKeyRing.inputStream, encryptedData.keyID)
        def dataStream = encryptedData.getDataStream(key, "BC")
        def compressed = new PGPObjectFactory(dataStream).nextObject() as PGPCompressedData
        def literal = new PGPObjectFactory(compressed.dataStream).nextObject() as PGPLiteralData
        def out = new ByteArrayOutputStream()
        out << literal.inputStream

        if (encryptedData.isIntegrityProtected()) {
            if (!encryptedData.verify()) {
                throw new PGPException("Message failed integrity check");
            }
        }

        return new String(out.toByteArray())
    }

}