package com.mlx.opcuademo.service;

import com.mlx.opcuademo.configuration.OpcUAConfig;
import com.mlx.opcuademo.entity.PostWriteValue;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class OpcUaClientServiceImpl implements OpcUaClientService {
    private final OpcUaClient client;
    private final OpcUAService opcUAService;
    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    public OpcUaClientServiceImpl(OpcUAConfig config, OpcUAService opcUAService) {
        this.client = config.createOpcUaClient();
        this.opcUAService = opcUAService;
        log.info("OpcUaClientService initialized");
    }

    @Override
    public Object readNode(Integer namespaceIndex, String identifier) {
        try {
            client.connect().get();
            var value = opcUAService.readNode(client, new NodeId(namespaceIndex, identifier), future);
            Object result = value.getValue().getValue();
            future.complete(client);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean writeNode(PostWriteValue postWriteValue) {
        try {
            client.connect().get();
            var result = opcUAService.writeNode(client,
                    new NodeId(postWriteValue.getNamespaceIndex(),
                            postWriteValue.getIdentifier()),
                    postWriteValue.getValue());
            future.complete(client);
            return result.isGood();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
