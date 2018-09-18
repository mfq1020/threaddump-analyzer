package site.hearen.threaddump.service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import site.hearen.threaddump.dao.S3Repository;
import site.hearen.threaddump.entity.S3Info;

@Service
@Slf4j
public class S3Service {
    @Autowired
    private S3Repository s3Repository;

    public void insertNewKey(String accessKey, String secretKey) {
        String encodedSecretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        s3Repository.save(S3Info.builder()
                .accessKey(accessKey)
                .secretKey(encodedSecretKey)
                .build());
    }

    public List<String> listSecretKeys() {
        return s3Repository.findAll().stream()
                .map(s3Info -> decodeSecretKey(s3Info.getSecretKey()))
                .collect(Collectors.toList());
    }

    private String decodeSecretKey(String encodedSecretKey) {
        return new String(Base64.getDecoder().decode(encodedSecretKey));
    }
}
