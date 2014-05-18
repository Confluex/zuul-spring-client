package org.devnull.client.spring

import org.apache.http.client.HttpClient
import org.apache.http.client.HttpResponseException
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.impl.client.BasicResponseHandler
import org.devnull.client.spring.cache.PropertiesObjectStore
import org.devnull.client.spring.crypto.PropertiesDecryptor
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers
import org.springframework.core.io.ClassPathResource

import static org.mockito.Mockito.*

class ZuulPropertiesFactoryBeanTest {

    ZuulPropertiesFactoryBean factory

    @Before
    void createClient() {
        factory = new ZuulPropertiesFactoryBean("app-data-config")
        factory.httpClient = mock(HttpClient)
        factory.propertiesDecryptor = mock(PropertiesDecryptor)
        factory.propertiesStore = mock(PropertiesObjectStore)
    }

    @Test
    void shouldFetchAndDecryptPropertiesFile() {
        def properties = mockResponseFromFile()
        def expected = new Properties()
        when(factory.propertiesDecryptor.decrypt(properties)).thenReturn(expected)
        def result = factory.object
        verify(factory.propertiesDecryptor).decrypt(properties)
        assert result == expected
    }

    @Test(expected = EncryptionOperationNotPossibleException)
    void shouldFailToDecryptWithIncorrectPassword() {
        def properties = mockResponseFromFile()
        when(factory.propertiesDecryptor.decrypt(properties)).thenThrow(new EncryptionOperationNotPossibleException("test"))
        factory.object
    }


    @Test
    void shouldUseHttpByDefault() {
        assert factory.uri.scheme == "http"
    }

    @Test
    void shouldUseHttpsIfConfigured() {
        factory.server = "https://localhost/zuul/settings/dev/app-data-config.properties"
        assert factory.uri.scheme == "https"
    }

    @Test
    void shouldBuildUriFromDefaultParameters() {
        assert factory.uri == new URI("http://localhost:80/zuul/settings/dev/app-data-config.properties")
    }

    @Test
    void shouldBuildUriFromConfiguredParameters() {
        factory.server = "http://config.devnull.org:8080"
        factory.environment = "qa"
        assert factory.uri == new URI("http://config.devnull.org:8080/settings/qa/app-data-config.properties")
    }

    @Test
    void shouldUseProvidedHttpClient() {
        def mockHttpClient = mock(HttpClient)
        factory.httpClient = mockHttpClient
        factory.afterPropertiesSet()
        assert factory.httpClient.is(mockHttpClient)
    }

    @Test
    void shouldConstructHttpClientIfNotProvided() {
        factory.httpClient = null
        factory.afterPropertiesSet()
        assert factory.httpClient
    }

    @Test
    void shouldShutDownConnectionManagerOnDestroy() {
        def manager = mock(ClientConnectionManager)
        when(factory.httpClient.connectionManager).thenReturn(manager)
        factory.destroy()
        verify(manager).shutdown()
    }

    @Test
    void shouldNotBeSingletonBean() {
        assert !factory.singleton
    }

    @Test
    void shouldHaveCorrectBeanType() {
        assert factory.objectType == Properties
    }


    @Test
    void shouldStorePropertiesIfConfigured() {
        mockResponseFromFile()
        def properties = factory.fetchProperties()
        verify(factory.propertiesStore).put(factory.environment, factory.config, properties)
    }

    @Test
    void shouldRetrieveFileFromPropertyStoreIfServiceErrors() {
        mockServerErrorResponse()
        def expected = new Properties()
        when(factory.propertiesStore.get(factory.environment, factory.config)).thenReturn(expected)
        when(factory.propertiesDecryptor.decrypt(expected)).thenReturn(expected)
        def results = factory.object
        verify(factory.propertiesStore).get(factory.environment, factory.config)
        assert results == expected
    }

    @Test(expected = HttpResponseException)
    void shouldErrorIfServiceErrorsAndNoPropertyStoreConfigured() {
        factory.propertiesStore = null
        mockServerErrorResponse()
        factory.object
    }


    protected Properties mockResponseFromFile() {
        def mockResponse = new ClassPathResource("/responses/mock-server-response-aes.properties").inputStream.text
        def httpGet = Matchers.any(HttpGet)
        def handler = Matchers.any(BasicResponseHandler)
        when(factory.httpClient.execute(httpGet as HttpGet, handler as BasicResponseHandler)).thenReturn(mockResponse)
        def properties = new Properties()
        properties.load(new StringReader(mockResponse))
        return properties
    }

    protected void mockServerErrorResponse() {
        def httpGet = Matchers.any(HttpGet)
        def handler = Matchers.any(BasicResponseHandler)
        def exception = new HttpResponseException(500, "test error")
        when(factory.httpClient.execute(httpGet as HttpGet, handler as BasicResponseHandler)).thenThrow(exception)
    }

}
