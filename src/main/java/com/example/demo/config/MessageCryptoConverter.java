package com.example.demo.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class MessageCryptoConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    private static final byte[] SECRET_KEY = "MyChatSecretKey!".getBytes(StandardCharsets.UTF_8);

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if(attribute == null ||attribute.isBlank())return attribute;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(SECRET_KEY, ALGORITHM));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }catch (Exception e){
            throw new RuntimeException("Error encrypting chat message", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isBlank())return dbData;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(SECRET_KEY, ALGORITHM));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        }catch (Exception e){
            throw new RuntimeException("Error decrypting chat message", e);
        }
    }
}
