[![Build Status](https://travis-ci.org/mcantrell/zuul-spring-client.png?branch=master)](https://travis-ci.org/mcantrell/zuul-spring-client)

# Zuul Spring Client

This project provides Spring helpers and namespaces for integrating with the web services provided by the
[Zuul Project](https://github.com/mcantrell/Zuul/wiki).


**Maven Dependency**
```xml
<groupId>org.devnull</groupId>
<artifactId>zuul-spring-client</artifactId>
<version>1.2</version>
```

**context.xml**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:zuul="http://www.devnull.org/schema/zuul-spring-client"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
http://www.devnull.org/schema/zuul-spring-client http://www.devnull.org/schema/zuul-spring-client-1.2.xsd
http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd">


    <context:property-placeholder properties-ref="appDataConfig"/>
    <zuul:properties id="appDataConfig" config="app-data-config" environment="prod">
        <zuul:file-store/>
    </zuul:properties>
</beans>
```

### Dynamic Configuration of Environment

There are a variety of strategies for configuring the environment property (Spring expression language, profiles, etc.). I've configured an example which uses spring profiles:

* [Context](https://github.com/mcantrell/zuul-spring-client/blob/master/src/test/resources/test-zuul-profiles-context.xml)
* [Tests](https://github.com/mcantrell/zuul-spring-client/tree/master/src/test/groovy/org/devnull/client/spring/profiles)


### Spring Namespace Reference
<hr/>

**zuul:properties attributes**
<table>
	<thead>
		<tr>
			<th>Attribute</th>
			<th>Description</th>
			<th>Default</th>
			<th>Required</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>config</td>
			<td>Name of the configuration to render</td>
			<td>n/a</td>
			<td>true</td>
		<tr>
		<tr>
			<td>host</td>
			<td>DNS or IP address of the zuul server</td>
			<td>localhost</td>
			<td>false</td>
		<tr>
		<tr>
			<td>port</td>
			<td>TCP port where the server is running</td>
			<td>80</td>
			<td>false</td>
		<tr>
		<tr>
			<td>context</td>
			<td>URI path to the root zuul application</td>
			<td>/zuul</td>
			<td>false</td>
		<tr>
		<tr>
			<td>environment</td>
			<td>Which environment set to retrieve</td>
			<td>dev</td>
			<td>false</td>
		<tr>
		<tr>
			<td>ssl</td>
			<td>Set to true if zuul endpoints are hosted via HTTPS</td>
			<td>false</td>
			<td>false</td>
		<tr>
		<tr>
			<td>password</td>
			<td>Password key used to decrypt settings. Will look for system property ZUUL_PASSWORD if not supplied here.</td>
			<td>null</td>
			<td>false</td>
		<tr>
        <tr>
            <td>algorithm</td>
            <td>
                <p>Optionally provide an encryption algorithm. Since previous versions of Zuul used PBEWithMD5AndDES by
                default, it is the default if a value is not supplied in order to maintain backwards compatibility. Using a
                stronger algorithm is strongly suggested. Available values: </p>
                <ul>
                    <li>PBEWITHSHA256AND128BITAES-CBC-BC (AES Bouncy Castle)</li>
                    <li>PBEWithSHAAnd2-KeyTripleDES-CBC (Triple DES Bouncy Castle)</li>
                    <li>PBEWithMD5AndTripleDES (Triple DES JCE)</li>
                    <li>PBEWithMD5AndDES (DES JCE)</li>
                </ul>
                <p>
                    **See:**
                    [Jasypt Documentation](http://www.jasypt.org/encrypting-configuration.html)
                    [Zuul Encryption Documentation](https://github.com/mcantrell/Zuul/wiki/Encryption)
                </p>
            </td>
            <td>PBEWithMD5AndDES</td>
            <td>false</td>
        <tr>
		<tr>
			<td>http-client-ref</td>
			<td>Reference to a custom httpcomponents http-client</td>
			<td>A default client is created by default. You can override if needed</td>
			<td>false</td>
		<tr>
	</tbody>
</table>
<hr/>

**zuul:file-store attributes**

The zuul:file-store element is optional. It caches copies of the files (with encrypted values) to the local filesystem. If configured, it will be used as a backup strategy if the zuul web services are unavailable.

_If left un-configured, the application will throw an exception upon startup if the service is not available._

<table>
	<thead>
		<tr>
			<th>Attribute</th>
			<th>Description</th>
			<th>Default</th>
			<th>Required</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>path</td>
			<td>File Resource to contain the cached files.</td>
			<td>Uses the java.io.tmp system property by default</td>
			<td>false</td>
		<tr>
	</tbody>
</table>
<hr/>

**Decryption**

If you have encrypted properties, you'll need to configure the password. As of version 1.1, there are two options:

* Use the password attribute on the zuul:properties element
* Set the system environment variable: ZUUL_PASSWORD


<blockquote>The System environment variable can be passed to the JVM via a parameter</blockquote>
<pre>
-DZUUL_PASSWORD=badpassword1
</pre>


# License

   Copyright 2012 Mike Cantrell

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
