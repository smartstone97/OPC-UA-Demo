package com.mlx.opcuademo.entity;

import com.mlx.opcuademo.service.OpcUaClientService;
import lombok.Setter;


public class UdtNode {
    private String identifier;
    @Setter
    private String value;
    private OpcUaClientService opcUaClientService;

    public static UdtNode initialization(String identifier, OpcUaClientService opcUaClientService) {
        UdtNode udtNode = new UdtNode();
        udtNode.identifier = identifier;
        udtNode.opcUaClientService = opcUaClientService;
        return udtNode;
    }

    public Object getValue() {
        return opcUaClientService.readNode(2, identifier);
    }

    public UdtNode initialize(String identifier, OpcUaClientService service) {
        this.identifier = identifier;
        opcUaClientService = service;
        return this;
    }

    public void writeValue() {
        if (value != null) {
            boolean result = opcUaClientService.writeNode(PostWriteValue.builder()
                    .namespaceIndex(2)
                    .identifier(identifier)
                    .value(value)
                    .build());
            if (!result) {
                throw new RuntimeException("Failed to write value to node " + identifier);
            }
        }
    }
}
