package com.duplicate_filter.filter.servlet_filter.request_hasher;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestHasher {

    @Value("${encryption.key}")
    private String encryptionKey;

    public String createHash(String content, String ip, String url) throws NoSuchAlgorithmException{
        String combined=content+"-"+ip+"-"+url+"-"+encryptionKey;

        MessageDigest digest=MessageDigest.getInstance("SHA256");
        
        byte[] combinedBytes=combined.getBytes();
        byte[] encodedHash=digest.digest(combinedBytes);
        
        Encoder encoder=Base64.getEncoder();
        String hashed=encoder.encodeToString(encodedHash);

        return  hashed;
    } 

}
