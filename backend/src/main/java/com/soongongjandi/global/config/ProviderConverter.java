package com.soongongjandi.global.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.soongongjandi.domain.member.entity.Provider;

@Component
public class ProviderConverter implements Converter<String, Provider> {

    @Override
    public Provider convert(String source) {
        try {
            return Provider.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown provider: " + source);
        }
    }
}
