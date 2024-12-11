package com.mlx.opcuademo.service;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class OpcUAServiceImpl implements OpcUAService {
    @Override
    public IdentityProvider getIdentityProvider(String username, String password) {
        return new UsernameProvider(username, password);
    }

    @Override
    public CompletableFuture<List<NodeId>> findAllNode(OpcUaClient client) {
        final List<UaNode> nodes = new ArrayList<>();
        var nodeTree2List = browseNodeTree(0, "", client, Identifiers.RootFolder, nodes);
        List<NodeId> nodeIds = new ArrayList<>();
        nodeTree2List.forEach(node -> nodeIds.add(node.getNodeId()));
        return CompletableFuture.completedFuture(nodeIds);
    }

    @Override
    public BrowseResult browseNode(OpcUaClient client, NodeId nodeId, CompletableFuture<OpcUaClient> future) {
        try {
            var result = client.browse(BrowseDescription.builder()
                            .nodeId(nodeId)
                            .browseDirection(BrowseDirection.Both)
                            .build())
                    .get();
            future.complete(client);
            return result;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DataValue readNode(OpcUaClient client, NodeId nodeId, CompletableFuture<OpcUaClient> future) {
        try {
            var value = client.readValue(1000, TimestampsToReturn.Both, nodeId);
            future.complete(client);
            return value.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StatusCode writeNode(OpcUaClient client, NodeId nodeId, Object value) {
        try {
            return client.writeValue(nodeId, DataValue.newValue()
                    .setValue(new Variant(value))
                    .build()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<List<NodeId>> getTagProviderNodes(OpcUaClient client, @Nullable String... identifiers) {
        List<NodeId> nodeIds = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        String identifier = identifiers == null ? "Tag Providers" : combine(str, identifiers);
        var tagProviderNodes = client.browse(BrowseDescription.builder()
                .nodeId(new NodeId(2, identifier))
                .browseDirection(BrowseDirection.Forward)
                .build());
        try {
            Arrays.stream(tagProviderNodes.get().getReferences()).toList().forEach(
                    reference -> {
                        try {
                            nodeIds.add(reference.getNodeId().toNodeIdOrThrow(client.getNamespaceTable()));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
            return CompletableFuture.completedFuture(nodeIds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String combine(StringBuilder str, String... strings) {
        Arrays.stream(strings).toList().forEach(identifier -> str.append(identifier).append("/"));
        return str.toString();
    }

    private List<? extends UaNode> browseNodeTree(final int deep,
                                                  final String indent,
                                                  final OpcUaClient client,
                                                  final NodeId browseRoot,
                                                  final List<UaNode> nodes) {
        try {
            List<? extends UaNode> nodeList = client.getAddressSpace().browseNodes(browseRoot);
            for (final UaNode node : nodeList) {
                //排除系统性节点，这些系统性节点名称一般都是以"_"开头
                if (Objects.requireNonNull(node.getBrowseName().getName()).startsWith("_")) continue;
                // 匹配指定节点的数据(包含对应的子节点)
                log.info("{}--{} Node={}", deep, indent, node.getBrowseName().getName());
                if (deep == 1 && !node.getBrowseName().getName().contains("Tag Providers")) continue;
                if (deep == 2 && !node.getBrowseName().getName().contains("default")) continue;
                nodes.add(node);
                // recursively browse to children
                browseNodeTree(deep + 1, indent + "*", client, node.getNodeId(), nodes);
            }
        } catch (UaException e) {
            log.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
        }
        return nodes;
    }
}
