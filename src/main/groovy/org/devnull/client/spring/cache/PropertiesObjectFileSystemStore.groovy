package org.devnull.client.spring.cache

import org.springframework.core.io.Resource

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class PropertiesObjectFileSystemStore implements PropertiesObjectStore {
    File parent
    Lock lock = new ReentrantLock()
    Resource loadIfNotFound
    boolean ignoreResourceNotFound

    PropertiesObjectFileSystemStore(boolean ignoreResourceNotFound) {
        this.parent = new File(System.getProperty("java.io.tmpdir"))
        this.ignoreResourceNotFound = ignoreResourceNotFound
    }

    PropertiesObjectFileSystemStore(boolean ignoreResourceNotFound, File parent) {
        this.parent = parent
        this.ignoreResourceNotFound = ignoreResourceNotFound
    }



    void put(String environment, String name, Properties props) {
        doWhileLocked {
            def file = new File(parent, "${environment}-${name}.properties")

            def writer = new FileWriter(file)
            try {
                props.store(writer, "cached copy")
            } finally {
                writer.close()
            }
        }
    }

    Properties get(String environment, String name) {
        doWhileLocked {
            def props = new Properties()
            def file = getCacheOrFallbackFileIfOneExists(environment, name)
            if (file.exists()) {
                def stream = new FileInputStream(file)
                try {
                    props.load(stream)
                } finally {
                    stream.close()
                }
            }
            return props
        }
    }

    protected def doWhileLocked = { closure ->
        lock.lock()
        try {
            closure()
        }
        finally {
            lock.unlock()
        }
    }

    protected File getCacheOrFallbackFileIfOneExists(String environment, String name){
        def file = new File(parent, "${environment}-${name}.properties")
        if (!file.exists()) {
            if(loadIfNotFound == null){
                checkIgnoreResourceNotFound("Unable to find locally cached copy: ${file.absolutePath}")
            }else if(!loadIfNotFound.exists()){
                checkIgnoreResourceNotFound("Unable to find locally cached copy: ${file.absolutePath}" +
                        " or find fallback loadIfMissing resource: ${loadIfNotFound}")
            }else{
                file = loadIfNotFound.file
            }
        }
        return file
    }

    private void checkIgnoreResourceNotFound(String errorMsg) {
        if (!ignoreResourceNotFound) {
            throw new FileNotFoundException(errorMsg)
        }
    }
}