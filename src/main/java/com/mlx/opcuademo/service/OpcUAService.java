package com.mlx.opcuademo.service;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface OpcUAService {
    default Predicate<EndpointDescription> endpointFilter() {
        return e -> getSecurityPolicy().getUri().equals(e.getSecurityPolicyUri());
    }

    default SecurityPolicy getSecurityPolicy() {
        return SecurityPolicy.Basic256Sha256;
    }

    IdentityProvider getIdentityProvider(String username, String password);

    CompletableFuture<List<NodeId>> findAllNode(OpcUaClient client);

    BrowseResult browseNode(OpcUaClient client, NodeId nodeId, CompletableFuture<OpcUaClient> future);

    DataValue readNode(OpcUaClient client, NodeId nodeId, CompletableFuture<OpcUaClient> future);

    StatusCode writeNode(OpcUaClient client, NodeId nodeId, Object value);

    CompletableFuture<List<NodeId>> getTagProviderNodes(OpcUaClient client, @Nullable String... identifiers);
}
