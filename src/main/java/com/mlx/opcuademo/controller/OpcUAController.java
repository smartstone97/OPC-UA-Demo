package com.mlx.opcuaservice.controller;

import com.mlx.entity.Result;
import com.mlx.opcuaservice.configuration.OpcUAConfig;
import com.mlx.opcuaservice.entity.PostWriteValue;
import com.mlx.opcuaservice.service.OpcUAService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/opc-ua-client")
public class OpcUAController {
    private final OpcUAService opcUAService;
    private final OpcUaClient client;
    private final CompletableFuture<OpcUaClient> future = new CompletableFuture<>();

    public OpcUAController(OpcUAService opcUAService, OpcUAConfig config) {
        this.opcUAService = opcUAService;
        this.client = config.createOpcUaClient();
    }

    @GetMapping("/find-all-nodes")
    public Result<?> findAllNodes() {
        try {
            client.connect().get();
            var result = opcUAService.findAllNode(client).get();
            client.disconnect().get();
            return Result.success(result);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/browse-node")
    public Result<?> browseNode(Integer namespaceIndex, String identifier) {
        try {
            client.connect().get();
            var result = opcUAService.browseNode(client, new NodeId(namespaceIndex, identifier), future);
            client.disconnect().get();
            return Result.success(Map.of("result", result));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/read-node")
    public Result<?> readNode(Integer namespaceIndex, String identifier) {
        try {
            client.connect().get();
            var value = opcUAService.readNode(client, new NodeId(namespaceIndex, identifier), future);
            Object result = value.getValue().getValue();
            client.disconnect().get();
            return Result.success(Map.of("value", result != null ? result : "null"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/write-node")
    public Result<?> writeNode(@RequestBody PostWriteValue postWriteValue) {
        try {
            client.connect().get();
            var result = opcUAService.writeNode(client,
                    new NodeId(postWriteValue.getNamespaceIndex(),
                            postWriteValue.getIdentifier()),
                    postWriteValue.getValue());
            future.complete(client);
            client.disconnect().get();
            return Result.success(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
