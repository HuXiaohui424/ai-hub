package com.hui.ai.repository.impl;

import lombok.Getter;
import org.springframework.core.io.ByteArrayResource;

@Getter
public class NameWithByteArrayResource extends ByteArrayResource {
    private final String filename;

    public NameWithByteArrayResource(byte[] byteArray, String filename) {
        super(byteArray);
        this.filename = filename;
    }


}
