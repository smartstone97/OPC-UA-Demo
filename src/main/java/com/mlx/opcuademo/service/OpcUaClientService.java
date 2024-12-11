package com.mlx.opcuademo.service;

import com.mlx.opcuademo.entity.PostWriteValue;
import org.springframework.web.bind.annotation.RequestBody;

public interface OpcUaClientService {
    Object readNode(Integer namespaceIndex, String identifier);

    Boolean writeNode(@RequestBody PostWriteValue postWriteValue);
}
