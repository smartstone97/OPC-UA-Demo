package com.mlx.opcuaservice.service;

import com.mlx.opcuaservice.entity.PostWriteValue;
import org.springframework.web.bind.annotation.RequestBody;

public interface OpcUaClientService {
    Object readNode(Integer namespaceIndex, String identifier);

    Boolean writeNode(@RequestBody PostWriteValue postWriteValue);
}
