package org.abhi.synapse.http;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.abhi.synapse.config.SynapseConfig;
import org.abhi.synapse.core.exception.SynapseException;
import org.abhi.synapse.core.model.SynapseResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpsTlsTest {

    private static final String STORE_PASSWORD = "changeit";

    private HttpsServer server;
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        Path store = storePath();
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (java.io.InputStream in = Files.newInputStream(store)) {
            keyStore.load(in, STORE_PASSWORD.toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, STORE_PASSWORD.toCharArray());
        SSLContext serverContext = SSLContext.getInstance("TLS");
        serverContext.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress("localhost", 0), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(serverContext));
        server.createContext("/v1/chat/completions", exchange -> {
            String body = "{\"id\":\"cmpl-tls\",\"object\":\"chat.completion\",\"model\":\"gpt-4\","
                    + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"TLS OK\"},"
                    + "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":3,\"total_tokens\":8}}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body.getBytes());
            }
        });
        server.start();
        port = server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static Path storePath() throws Exception {
        java.net.URL url = HttpsTlsTest.class.getResource("/synapse-test-keystore.p12");
        if (url == null) throw new IOException("missing test keystore resource");
        return Path.of(url.toURI());
    }

    private SynapseConfig.Builder baseBuilder() {
        return SynapseConfig.builder()
                .baseUrl("https://localhost:" + port)
                .endpoint("/v1/chat/completions")
                .apiKey("sk-test")
                .modelName("gpt-4")
                .maxRetries(0);
    }

    @Test
    void clientTrustingServerCertificateSucceeds() throws Exception {
        SynapseHub hub = new SynapseHub(baseBuilder()
                .trustStore(storePath(), STORE_PASSWORD)
                .build());
        try {
            SynapseResponse response = hub.sendPrompt("ping", null);
            assertThat(response.getContent()).isEqualTo("TLS OK");
        } finally {
            hub.close();
        }
    }

    @Test
    void defaultClientFailsHandshakeAgainstSelfSignedCert() {
        SynapseHub hub = new SynapseHub(baseBuilder().build());
        try {
            assertThatThrownBy(() -> hub.sendPrompt("ping", null))
                    .isInstanceOf(SynapseException.class);
        } finally {
            hub.close();
        }
    }

    @Test
    void trustAllAcceptsSelfSignedCert() throws Exception {
        SynapseHub hub = new SynapseHub(baseBuilder()
                .trustAll(true)
                .build());
        try {
            SynapseResponse response = hub.sendPrompt("ping", null);
            assertThat(response.getContent()).isEqualTo("TLS OK");
        } finally {
            hub.close();
        }
    }
}
