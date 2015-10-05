package org.devnull.client.spring.cache

import org.apache.commons.io.FileUtils
import org.junit.Before
import org.junit.Test
import org.springframework.core.io.ClassPathResource

import java.util.concurrent.locks.Lock
import java.util.regex.Pattern

import static org.junit.Assert.fail
import static org.mockito.Mockito.*

class PropertiesObjectFileSystemStoreTest {

    PropertiesObjectFileSystemStore store
    File parent
    Properties properties

    @Before
    void createStore() {
        properties = new Properties()
        properties.setProperty("a.b.c", "def")
        parent = new File("${System.getProperty("java.io.tmpdir")}/test-data")
        parent.exists() ? FileUtils.cleanDirectory(parent) : parent.mkdirs()
        store = new PropertiesObjectFileSystemStore(false,parent)
    }

    @Test
    void shouldCreateNewPropertiesFileInParentDir() {
        store.put("dev", "test-config", properties)
        def result = getStoredProperties("dev-test-config.properties")
        assert result == properties
    }

    @Test
    void shouldReplaceExistingPropertiesFile() {
        store.put("dev", "test-config", properties)
        def newProperties = new Properties()
        newProperties.setProperty("foo", "bar")
        store.put("dev", "test-config", newProperties)
        def result = getStoredProperties("dev-test-config.properties")
        assert result.size() == 1
        assert result.getProperty("foo") == "bar"
    }

    @Test
    void shouldReadStoredFilesFromParent() {
        store.put("dev", "test-config", properties)
        def result = store.get("dev", "test-config")
        assert result == properties
        assert !result.is(properties)
    }

    @Test
    void shouldLockAndUnlock() {
        store.lock = mock(Lock)
        store.put("dev", "test-config", properties)
        store.get("dev", "test-config")
        verify(store.lock, times(2)).lock()
        verify(store.lock, times(2)).unlock()
    }

    @Test
    void shouldReleaseLocksUponException() {
        store.lock = mock(Lock)
        def exception = null
        try {
            store.put("dev", "test-config", null)
        } catch (Exception e) {
            exception = e
        }
        assert exception
        verify(store.lock).unlock()
    }

    @Test
    void shouldUseTmpDirAsParentByDefault() {
        def expected = new File(System.getProperty("java.io.tmpdir"))
        assert expected.exists()
        assert expected.canWrite()
        def store = new PropertiesObjectFileSystemStore(false)
        assert store.parent == expected
    }

    @Test
    void shouldUseLoadIfNotFoundFileIfCacheIsNotAvailable(){
        store.loadIfNotFound = new ClassPathResource("fallback.properties")
        def result = store.get("dev", "test-config")
        assert result.size() == 1
        assert result.get("a.b.c") == "loadIfNotFound"
    }

    def cacheFileErrorMessagePtrn = "Unable to find locally cached copy:.*dev-should-not-exist.properties"
    def cacheAndFallbackErrorMessagePtrn = /${cacheFileErrorMessagePtrn} or find fallback loadIfMissing resource: class path resource \[waldo.properties\]/

    @Test
    void shouldErrorIfGetForFileDoesNotExist() {
        doesErrorMessagePatternMatch(cacheFileErrorMessagePtrn)
    }

    @Test
    void shouldErrorIfGetForFileDoesNotExistInCacheOrInOverride() {
        store.loadIfNotFound = new ClassPathResource('waldo.properties')
        doesErrorMessagePatternMatch(cacheAndFallbackErrorMessagePtrn)
    }

    @Test
    void shouldNotGetErrorIfGetForFileDoesNotExistAndIgnoreResourcesTrue() {
        store.ignoreResourceNotFound = true
        Properties p = store.get("dev", "should-not-exist")
        assert p.isEmpty()
    }

    @Test
    void shouldNotErrorIfGetForFileDoesNotExistInCacheOrInOverrideIgnoreResourcesTrue() {
        store.loadIfNotFound = new ClassPathResource('waldo.properties')
        store.ignoreResourceNotFound = true
        Properties p = store.get("dev", "should-not-exist")
        assert p.isEmpty()
    }

    private void doesErrorMessagePatternMatch(def errorMsgPattern){
        try {
            store.get("dev", "should-not-exist")
            //and a fail @Rule and ExpectedException wouldn't work for me
            // too many matcher conflicts between junit, hamcrest, and mockito
            fail("Should have thrown FileNotFoundException")
        }catch(FileNotFoundException e){
            assert e.getMessage() ==~errorMsgPattern
        }
    }

    protected Properties getStoredProperties(String name) {
        def files = parent.listFiles()
        assert files.size() == 1
        def file = files.first()
        assert file.name == name
        def properties = new Properties()
        def reader = new FileReader(file)
        properties.load(reader)
        reader.close()
        return properties
    }
}
