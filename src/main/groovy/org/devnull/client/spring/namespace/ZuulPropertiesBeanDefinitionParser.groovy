package org.devnull.client.spring.namespace

import org.devnull.client.spring.ZuulPropertiesFactoryBean
import org.devnull.client.spring.cache.PropertiesObjectFileSystemStore
import org.devnull.client.spring.crypto.PbePropertiesDecryptor
import org.devnull.client.spring.crypto.PgpPropertiesDecryptor
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser
import org.springframework.util.xml.DomUtils
import org.w3c.dom.Element

class ZuulPropertiesBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return ZuulPropertiesFactoryBean
    }

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        def config = element.getAttribute("config")
        bean.addConstructorArgValue(config)
        def httpClientRef = element.getAttribute("http-client-ref")
        if (httpClientRef) {
            bean.addPropertyReference("httpClient", httpClientRef)
        }
        ZuulPropertiesFactoryBean.OPTIONAL_ATTRIBUTES.each {
            def option = element.getAttribute(it)
            if (option) {
                bean.addPropertyValue(it, option)
            }
        }
        def fileStore = DomUtils.getChildElementByTagName(element, "file-store")
        if (fileStore) {
            def fileStoreFactory = BeanDefinitionBuilder.rootBeanDefinition(PropertiesObjectFileSystemStore);
            def path = fileStore.getAttribute("path")
            if (path) {
                fileStoreFactory.addConstructorArgValue(path)
            }
            bean.addPropertyValue("propertiesStore", fileStoreFactory.beanDefinition)
        }
        def pgpDecryptor = DomUtils.getChildElementByTagName(element, "pgp-decryptor")
        if (pgpDecryptor) {
            def factory = BeanDefinitionBuilder.rootBeanDefinition(PgpPropertiesDecryptor);
            factory.addPropertyValue("secretKeyRing", pgpDecryptor.getAttribute("secret-key-ring"))
            factory.addPropertyValue("password", pgpDecryptor.getAttribute("password"))
            bean.addPropertyValue("propertiesDecryptor", factory.beanDefinition)
        }
        def pbeDecryptor = DomUtils.getChildElementByTagName(element, "pbe-decryptor")
        if (pbeDecryptor) {
            def factory = BeanDefinitionBuilder.rootBeanDefinition(PbePropertiesDecryptor);
            factory.addConstructorArgValue(pbeDecryptor.getAttribute("algorithm"))
            factory.addConstructorArgValue(pbeDecryptor.getAttribute("password"))
            bean.addPropertyValue("propertiesDecryptor", factory.beanDefinition)
        }
    }
}
