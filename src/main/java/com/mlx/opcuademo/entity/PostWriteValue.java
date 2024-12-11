package com.mlx.opcuaservice.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostWriteValue {
    private Integer namespaceIndex;
    private String identifier;
    private String value;
}
