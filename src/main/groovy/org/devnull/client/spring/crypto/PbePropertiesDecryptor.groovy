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
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig
import org.jasypt.properties.EncryptableProperties
import org.springframework.core.io.Resource

import java.security.Security

@Slf4j
public class PbePropertiesDecryptor implements PropertiesDecryptor {

    /**
     * Available algorithms for use with their associated meta-data.
     */
    static final Map ALGORITHM_CONFIG = [
            'PBEWITHSHA256AND128BITAES-CBC-BC': [
                    provider: BouncyCastleProvider.PROVIDER_NAME,
                    hashIterations: 1000,
                    secure:true
            ],
            'PBEWithSHAAnd2-KeyTripleDES-CBC': [
                    provider: BouncyCastleProvider.PROVIDER_NAME,
                    hashIterations: 1000,
                    secure:true
            ],
            'PBEWithMD5AndTripleDES' : [
                    provider: null,
                    hashIterations: 1000,
                    secure:true
            ],
            'PBEWithMD5AndDES' : [
                    provider: null,
                    hashIterations: 1000,
                    secure:false
            ]
    ]

    StandardPBEStringEncryptor encryptor

    PbePropertiesDecryptor(String algorithm, String password) {
        def algorithmConfig = ALGORITHM_CONFIG[algorithm]
        encryptor = new StandardPBEStringEncryptor()
        encryptor.password = password
        encryptor.algorithm = algorithm
        encryptor.keyObtentionIterations = algorithmConfig.hashIterations
        if (algorithmConfig.provider)
            encryptor.providerName = algorithmConfig.provider
        if (!algorithmConfig.secure) {
            log.warn("{} is not considered a secure algorithm. Please consider using an alternative.", algorithm)
        }
    }

    Properties decrypt(Properties properties) {
        return new EncryptableProperties(properties, encryptor)
    }


}