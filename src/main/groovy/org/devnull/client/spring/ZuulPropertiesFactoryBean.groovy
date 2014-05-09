package org.devnull.client.spring

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.devnull.client.spring.cache.PropertiesObjectStore
import org.devnull.client.spring.crypto.PropertiesDecryptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

import java.security.Security

class ZuulPropertiesFactoryBean extends PropertySourcesPlaceholderConfigurer implements InitializingBean, DisposableBean, FactoryBean<Properties> {

    final def log = LoggerFactory.getLogger(this.class)

    static {
        Security.addProvider(new BouncyCastleProvider())
    }

    static final List<String> OPTIONAL_ATTRIBUTES = ["server", "environment", "password", "algorithm"]

    /**
     * Used to invoke the web service. If not supplied, a default client will be provided.
     */
    HttpClient httpClient

    /**
     * URL for the server.
     */
    String server = "http://localhost:80/zuul";
    
    /**
     * Environment for the configuration
     *
     * default = "dev"
     */
    String environment = "dev"

    /**
     * Name of the configuration to fetch
     */
    String config

    /**
     * Used to cache requests.
     *
     * @see org.devnull.client.spring.cache.PropertiesObjectFileSystemStore
     */
    PropertiesObjectStore propertiesStore

    /**
     * Used to do the actual decryption.
     *
     * @see org.devnull.client.spring.crypto.PbePropertiesDecryptor
     * @see org.devnull.client.spring.crypto.PgpPropertiesDecryptor
     */
    PropertiesDecryptor propertiesDecryptor

    ZuulPropertiesFactoryBean(String config) {
        this.config = config
    }

    Properties fetchProperties() {
        def handler = new BasicResponseHandler();
        def get = new HttpGet(uri)
        log.info("Fetching properties from {}", get)
        try {
            def responseBody = httpClient.execute(get, handler);
            def properties = new Properties()
            properties.load(new StringReader(responseBody))
            log.debug("Loading properties: {}", properties)
            if (propertiesStore) {
                propertiesStore.put(environment, config, properties)
            }
            return properties
        } catch (Exception e) {
            log.error("Unable to fetch remote properties file from: {}, error:{}. Attempting to find cached copy..", get, e.message)
            if (!propertiesStore) {
                log.error("Cache not configured. Giving up...")
                log.error("Hint: use zuul:file-store to configure locally cached copies as a fail-safe.")
                throw e
            }
            return propertiesStore.get(environment, config)
        }
    }

    URI getUri() {
        return new URI("${server}/settings/${environment}/${config}.properties")
    }

    void afterPropertiesSet() {
        if (!httpClient) {
            httpClient = new DefaultHttpClient();
        }
        // TDOO only execute if auto-config is enabled
        super.setProperties(getObject())
    }

    void destroy() {
        httpClient.connectionManager.shutdown()
    }

    Properties getObject() {
        def properties = fetchProperties()
        if (propertiesDecryptor) {
            properties = propertiesDecryptor.decrypt(properties)
        }
        return properties
    }

    Class<?> getObjectType() {
        return Properties
    }

    boolean isSingleton() {
        return false
    }


}
