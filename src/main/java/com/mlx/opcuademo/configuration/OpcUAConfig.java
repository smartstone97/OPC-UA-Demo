package com.mlx.opcuademo.configuration;

import com.mlx.opcuademo.service.OpcUAService;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "opc.ua")
public class OpcUAConfig {
    private final String username;
    private final String password;
    private final String endpointUrl;
    @Autowired
    private OpcUAService opcUaService;
    private Integer timeout = 10000;
    private String keystorePath = "./keystore";

    public OpcUAConfig(@Nullable Integer timeout, @Nullable String keystorePath, String username, String password, String endpointUrl) {
        if (timeout != null) {
            this.timeout = timeout;
        }
        if (keystorePath != null) {
            if (!keystorePath.isBlank()) {
                this.keystorePath = keystorePath;
            }
        }
        this.username = username;
        this.password = password;
        this.endpointUrl = endpointUrl;
    }

    public OpcUaClient createOpcUaClient() {
        try {
            Path securityTempDir = Paths.get(keystorePath, "client", "security");
            Files.createDirectories(securityTempDir);
            if (!Files.exists(securityTempDir)) {
                throw new Exception("unable to create security dir: " + securityTempDir);
            }
            KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);
            return OpcUaClient.create(endpointUrl,
                    endpoints ->
                            endpoints.stream()
                                    .filter(opcUaService.endpointFilter())
                                    .findFirst()
                    , configBuilder -> configBuilder.setApplicationName(LocalizedText.english("opc-ua-client"))
                            .setApplicationUri("urn:mlx:opcuaservice:client")
                            .setCertificate(loader.getClientCertificate())
                            .setKeyPair(loader.getClientKeyPair())
                            .setCertificateChain(loader.getClientCertificateChain())
                            .setIdentityProvider(opcUaService.getIdentityProvider(username, password))
                            .setRequestTimeout(UInteger.valueOf(timeout))
                            .setConnectTimeout(UInteger.valueOf(timeout))
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
