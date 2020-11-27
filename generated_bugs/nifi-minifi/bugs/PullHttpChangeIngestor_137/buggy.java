/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.minifi.bootstrap.configuration.ingestors;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.nifi.minifi.bootstrap.ConfigurationFileHolder;
import org.apache.nifi.minifi.bootstrap.RunMiNiFi;
import org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeNotifier;
import org.apache.nifi.minifi.bootstrap.configuration.differentiators.WholeConfigDifferentiator;
import org.apache.nifi.minifi.bootstrap.configuration.differentiators.interfaces.Differentiator;
import org.apache.nifi.minifi.bootstrap.util.ByteBufferInputStream;
import org.apache.nifi.minifi.commons.schema.ConfigSchema;
import org.apache.nifi.minifi.commons.schema.SecurityPropertiesSchema;
import org.apache.nifi.minifi.commons.schema.common.ConvertableSchema;
import org.apache.nifi.minifi.commons.schema.common.StringUtil;
import org.apache.nifi.minifi.commons.schema.serialization.SchemaLoader;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeCoordinator.NOTIFIER_INGESTORS_KEY;
import static org.apache.nifi.minifi.bootstrap.configuration.differentiators.WholeConfigDifferentiator.WHOLE_CONFIG_KEY;


public class PullHttpChangeIngestor extends AbstractPullChangeIngestor {

    private static final int NOT_MODIFIED_STATUS_CODE = 304;
    private static final Map<String, Supplier<Differentiator<ByteBuffer>>> DIFFERENTIATOR_CONSTRUCTOR_MAP;

    static {
        HashMap<String, Supplier<Differentiator<ByteBuffer>>> tempMap = new HashMap<>();
        tempMap.put(WHOLE_CONFIG_KEY, WholeConfigDifferentiator::getByteBufferDifferentiator);

        DIFFERENTIATOR_CONSTRUCTOR_MAP = Collections.unmodifiableMap(tempMap);
    }

    private static final String DEFAULT_CONNECT_TIMEOUT_MS = "5000";
    private static final String DEFAULT_READ_TIMEOUT_MS = "15000";

    private static final String PULL_HTTP_BASE_KEY = NOTIFIER_INGESTORS_KEY + ".pull.http";
    public static final String PULL_HTTP_POLLING_PERIOD_KEY = PULL_HTTP_BASE_KEY + ".period.ms";
    public static final String PORT_KEY = PULL_HTTP_BASE_KEY + ".port";
    public static final String HOST_KEY = PULL_HTTP_BASE_KEY + ".hostname";
    public static final String PATH_KEY = PULL_HTTP_BASE_KEY + ".path";
    public static final String QUERY_KEY = PULL_HTTP_BASE_KEY + ".query";
    public static final String PROXY_HOST_KEY = PULL_HTTP_BASE_KEY + ".proxy.hostname";
    public static final String PROXY_PORT_KEY = PULL_HTTP_BASE_KEY + ".proxy.port";
    public static final String PROXY_USERNAME = PULL_HTTP_BASE_KEY + ".proxy.username";
    public static final String PROXY_PASSWORD = PULL_HTTP_BASE_KEY + ".proxy.password";
    public static final String TRUSTSTORE_LOCATION_KEY = PULL_HTTP_BASE_KEY + ".truststore.location";
    public static final String TRUSTSTORE_PASSWORD_KEY = PULL_HTTP_BASE_KEY + ".truststore.password";
    public static final String TRUSTSTORE_TYPE_KEY = PULL_HTTP_BASE_KEY + ".truststore.type";
    public static final String KEYSTORE_LOCATION_KEY = PULL_HTTP_BASE_KEY + ".keystore.location";
    public static final String KEYSTORE_PASSWORD_KEY = PULL_HTTP_BASE_KEY + ".keystore.password";
    public static final String KEYSTORE_TYPE_KEY = PULL_HTTP_BASE_KEY + ".keystore.type";
    public static final String CONNECT_TIMEOUT_KEY = PULL_HTTP_BASE_KEY + ".connect.timeout.ms";
    public static final String READ_TIMEOUT_KEY = PULL_HTTP_BASE_KEY + ".read.timeout.ms";
    public static final String DIFFERENTIATOR_KEY = PULL_HTTP_BASE_KEY + ".differentiator";
    public static final String USE_ETAG_KEY = PULL_HTTP_BASE_KEY + ".use.etag";
    public static final String OVERRIDE_SECURITY = PULL_HTTP_BASE_KEY + ".override.security";

    private final AtomicReference<OkHttpClient> httpClientReference = new AtomicReference<>();
    private final AtomicReference<Integer> portReference = new AtomicReference<>();
    private final AtomicReference<String> hostReference = new AtomicReference<>();
    private final AtomicReference<String> pathReference = new AtomicReference<>();
    private final AtomicReference<String> queryReference = new AtomicReference<>();
    private volatile Differentiator<ByteBuffer> differentiator;
    private volatile String connectionScheme;
    private volatile String lastEtag = "";
    private volatile boolean useEtag = false;
    private volatile boolean overrideSecurity = false;

    public PullHttpChangeIngestor() {
        logger = LoggerFactory.getLogger(PullHttpChangeIngestor.class);
    }

    @Override
public void initialize(java.util.Properties properties, org.apache.nifi.minifi.bootstrap.ConfigurationFileHolder configurationFileHolder, org.apache.nifi.minifi.bootstrap.configuration.ConfigurationChangeNotifier configurationChangeNotifier) {
    super.initialize(properties, configurationFileHolder, configurationChangeNotifier);
    pollingPeriodMS.set(java.lang.Integer.parseInt(properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PULL_HTTP_POLLING_PERIOD_KEY, org.apache.nifi.minifi.bootstrap.configuration.ingestors.AbstractPullChangeIngestor.DEFAULT_POLLING_PERIOD)));
    if (pollingPeriodMS.get() < 1) {
        throw new java.lang.IllegalArgumentException(("Property, " + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PULL_HTTP_POLLING_PERIOD_KEY) + ", for the polling period ms must be set with a positive integer.");
    }
    final java.lang.String host = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.HOST_KEY);
    if ((host == null) || host.isEmpty()) {
        throw new java.lang.IllegalArgumentException(("Property, " + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.HOST_KEY) + ", for the hostname to pull configurations from must be specified.");
    }
    final java.lang.String path = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PATH_KEY, "/");
    final java.lang.String query = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.QUERY_KEY, "");
    final java.lang.String portString = ((java.lang.String) (properties.get(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PORT_KEY)));
    final java.lang.Integer port;
    {
        port = java.lang.Integer.parseInt(/* NPEX_NULL_EXP */
        portString);
    }
    portReference.set(port);
    hostReference.set(host);
    pathReference.set(path);
    queryReference.set(query);
    final java.lang.String useEtagString = ((java.lang.String) (properties.getOrDefault(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.USE_ETAG_KEY, "false")));
    if ("true".equalsIgnoreCase(useEtagString) || "false".equalsIgnoreCase(useEtagString)) {
        useEtag = java.lang.Boolean.parseBoolean(useEtagString);
    } else {
        throw new java.lang.IllegalArgumentException((((("Property, " + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.USE_ETAG_KEY) + ", to specify whether to use the ETag header, must either be a value boolean value (\"true\" or \"false\") or left to ") + "the default value of \"false\". It is set to \"") + useEtagString) + "\".");
    }
    final java.lang.String overrideSecurityProperties = ((java.lang.String) (properties.getOrDefault(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.OVERRIDE_SECURITY, "false")));
    if ("true".equalsIgnoreCase(overrideSecurityProperties) || "false".equalsIgnoreCase(overrideSecurityProperties)) {
        overrideSecurity = java.lang.Boolean.parseBoolean(overrideSecurityProperties);
    } else {
        throw new java.lang.IllegalArgumentException((((("Property, " + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.OVERRIDE_SECURITY) + ", to specify whether to override security properties must either be a value boolean value (\"true\" or \"false\")") + " or left to the default value of \"false\". It is set to \"") + overrideSecurityProperties) + "\".");
    }
    httpClientReference.set(null);
    final okhttp3.OkHttpClient.Builder okHttpClientBuilder = new okhttp3.OkHttpClient.Builder();
    // Set timeouts
    okHttpClientBuilder.connectTimeout(java.lang.Long.parseLong(properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.CONNECT_TIMEOUT_KEY, org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DEFAULT_CONNECT_TIMEOUT_MS)), java.util.concurrent.TimeUnit.MILLISECONDS);
    okHttpClientBuilder.readTimeout(java.lang.Long.parseLong(properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.READ_TIMEOUT_KEY, org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DEFAULT_READ_TIMEOUT_MS)), java.util.concurrent.TimeUnit.MILLISECONDS);
    // Set whether to follow redirects
    okHttpClientBuilder.followRedirects(true);
    java.lang.String proxyHost = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PROXY_HOST_KEY, "");
    if (!proxyHost.isEmpty()) {
        java.lang.String proxyPort = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PROXY_PORT_KEY);
        if ((proxyPort == null) || proxyPort.isEmpty()) {
            throw new java.lang.IllegalArgumentException("Proxy port required if proxy specified.");
        }
        okHttpClientBuilder.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new java.net.InetSocketAddress(proxyHost, java.lang.Integer.parseInt(proxyPort))));
        java.lang.String proxyUsername = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PROXY_USERNAME);
        if (proxyUsername != null) {
            java.lang.String proxyPassword = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.PROXY_PASSWORD);
            if (proxyPassword == null) {
                throw new java.lang.IllegalArgumentException("Must specify proxy password with proxy username.");
            }
            okHttpClientBuilder.proxyAuthenticator(( route, response) -> response.request().newBuilder().addHeader("Proxy-Authorization", okhttp3.Credentials.basic(proxyUsername, proxyPassword)).build());
        }
    }
    // check if the ssl path is set and add the factory if so
    if (properties.containsKey(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.KEYSTORE_LOCATION_KEY)) {
        try {
            setSslSocketFactory(okHttpClientBuilder, properties);
            connectionScheme = "https";
        } catch (java.lang.Exception e) {
            throw new java.lang.IllegalStateException(e);
        }
    } else {
        connectionScheme = "http";
    }
    httpClientReference.set(okHttpClientBuilder.build());
    final java.lang.String differentiatorName = properties.getProperty(org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DIFFERENTIATOR_KEY);
    if ((differentiatorName != null) && (!differentiatorName.isEmpty())) {
        java.util.function.Supplier<org.apache.nifi.minifi.bootstrap.configuration.differentiators.interfaces.Differentiator<java.nio.ByteBuffer>> differentiatorSupplier = org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DIFFERENTIATOR_CONSTRUCTOR_MAP.get(differentiatorName);
        if (differentiatorSupplier == null) {
            throw new java.lang.IllegalArgumentException(((((("Property, " + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DIFFERENTIATOR_KEY) + ", has value ") + differentiatorName) + " which does not ") + "correspond to any in the PullHttpChangeIngestor Map:") + org.apache.nifi.minifi.bootstrap.configuration.ingestors.PullHttpChangeIngestor.DIFFERENTIATOR_CONSTRUCTOR_MAP.keySet());
        }
        differentiator = differentiatorSupplier.get();
    } else {
        differentiator = org.apache.nifi.minifi.bootstrap.configuration.differentiators.WholeConfigDifferentiator.getByteBufferDifferentiator();
    }
    differentiator.initialize(properties, configurationFileHolder);
}


    @Override
    public void run() {
        logger.debug("Attempting to pull new config");
        HttpUrl.Builder builder = new HttpUrl.Builder()
                .host(hostReference.get())
                .port(portReference.get())
                .encodedPath(pathReference.get());
        final String query = queryReference.get();
        if (!StringUtil.isNullOrEmpty(query)) {
            builder = builder.encodedQuery(query);
        }
        final HttpUrl url = builder
                .scheme(connectionScheme)
                .build();

        final Request.Builder requestBuilder = new Request.Builder()
                .get()
                .url(url);

        if (useEtag) {
            requestBuilder.addHeader("If-None-Match", lastEtag);
        }

        final Request request = requestBuilder.build();

        ResponseBody body = null;
        try (Response response = httpClientReference.get().newCall(request).execute()) {
            logger.debug("Response received: {}", response.toString());

            int code = response.code();

            if (code == NOT_MODIFIED_STATUS_CODE) {
                return;
            }

            if (code >= 400) {
                throw new IOException("Got response code " + code + " while trying to pull configuration: " + response.body().string());
            }

            body = response.body();

            if (body == null) {
                logger.warn("No body returned when pulling a new configuration");
                return;
            }

            final ByteBuffer bodyByteBuffer = ByteBuffer.wrap(body.bytes());
            ByteBuffer readOnlyNewConfig = null;

            // checking if some parts of the configuration must be preserved
            if (overrideSecurity) {
                readOnlyNewConfig = bodyByteBuffer.asReadOnlyBuffer();
            } else {
                logger.debug("Preserving previous security properties...");

                // get the current security properties from the current configuration file
                final File configFile = new File(properties.get().getProperty(RunMiNiFi.MINIFI_CONFIG_FILE_KEY));
                ConvertableSchema<ConfigSchema> configSchema = SchemaLoader.loadConvertableSchemaFromYaml(new FileInputStream(configFile));
                ConfigSchema currentSchema = configSchema.convert();
                SecurityPropertiesSchema secProps = currentSchema.getSecurityProperties();

                // override the security properties in the pulled configuration with the previous properties
                configSchema = SchemaLoader.loadConvertableSchemaFromYaml(new ByteBufferInputStream(bodyByteBuffer.duplicate()));
                ConfigSchema newSchema = configSchema.convert();
                newSchema.setSecurityProperties(secProps);

                // return the updated configuration preserving the previous security configuration
                readOnlyNewConfig = ByteBuffer.wrap(new Yaml().dump(newSchema.toMap()).getBytes()).asReadOnlyBuffer();
            }

            if (differentiator.isNew(readOnlyNewConfig)) {
                logger.debug("New change received, notifying listener");
                configurationChangeNotifier.notifyListeners(readOnlyNewConfig);
                logger.debug("Listeners notified");
            } else {
                logger.debug("Pulled config same as currently running.");
            }

            if (useEtag) {
                lastEtag = (new StringBuilder("\""))
                        .append(response.header("ETag").trim())
                        .append("\"").toString();
            }
        } catch (Exception e) {
            logger.warn("Hit an exception while trying to pull", e);
        }
    }

    private void setSslSocketFactory(OkHttpClient.Builder okHttpClientBuilder, Properties properties) throws Exception {
        final String keystoreLocation = properties.getProperty(KEYSTORE_LOCATION_KEY);
        final String keystorePass = properties.getProperty(KEYSTORE_PASSWORD_KEY);
        final String keystoreType = properties.getProperty(KEYSTORE_TYPE_KEY);

        assertKeystorePropertiesSet(keystoreLocation, keystorePass, keystoreType);

        // prepare the keystore
        final KeyStore keyStore = KeyStore.getInstance(keystoreType);

        try (FileInputStream keyStoreStream = new FileInputStream(keystoreLocation)) {
            keyStore.load(keyStoreStream, keystorePass.toCharArray());
        }

        final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keystorePass.toCharArray());

        // load truststore
        final String truststoreLocation = properties.getProperty(TRUSTSTORE_LOCATION_KEY);
        final String truststorePass = properties.getProperty(TRUSTSTORE_PASSWORD_KEY);
        final String truststoreType = properties.getProperty(TRUSTSTORE_TYPE_KEY);
        assertTruststorePropertiesSet(truststoreLocation, truststorePass, truststoreType);

        KeyStore truststore = KeyStore.getInstance(truststoreType);
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("X509");
        truststore.load(new FileInputStream(truststoreLocation), truststorePass.toCharArray());
        trustManagerFactory.init(truststore);

        final X509TrustManager x509TrustManager;
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers[0] != null) {
            x509TrustManager = (X509TrustManager) trustManagers[0];
        } else {
            throw new IllegalStateException("List of trust managers is null");
        }

        SSLContext tempSslContext;
        try {
            tempSslContext = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Unable to use 'TLS' for the PullHttpChangeIngestor due to NoSuchAlgorithmException. Will attempt to use the default algorithm.", e);
            tempSslContext = SSLContext.getDefault();
        }

        final SSLContext sslContext = tempSslContext;
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        okHttpClientBuilder.sslSocketFactory(sslSocketFactory, x509TrustManager);
    }

    private void assertKeystorePropertiesSet(String location, String password, String type) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException(KEYSTORE_LOCATION_KEY + " is null or is empty");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(KEYSTORE_LOCATION_KEY + " is set but " + KEYSTORE_PASSWORD_KEY + " is not (or is empty). If the location is set, the password must also be.");
        }

        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException(KEYSTORE_LOCATION_KEY + " is set but " + KEYSTORE_TYPE_KEY + " is not (or is empty). If the location is set, the type must also be.");
        }
    }

    private void assertTruststorePropertiesSet(String location, String password, String type) {
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException(TRUSTSTORE_LOCATION_KEY + " is not set or is empty");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(TRUSTSTORE_LOCATION_KEY + " is set but " + TRUSTSTORE_PASSWORD_KEY + " is not (or is empty). If the location is set, the password must also be.");
        }

        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException(TRUSTSTORE_LOCATION_KEY + " is set but " + TRUSTSTORE_TYPE_KEY + " is not (or is empty). If the location is set, the type must also be.");
        }
    }

    protected void setDifferentiator(Differentiator<ByteBuffer> differentiator) {
        this.differentiator = differentiator;
    }

    public void setLastEtag(String lastEtag) {
        this.lastEtag = lastEtag;
    }

    public void setUseEtag(boolean useEtag) {
        this.useEtag = useEtag;
    }
}
