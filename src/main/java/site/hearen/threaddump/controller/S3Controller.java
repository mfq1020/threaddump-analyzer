package site.hearen.threaddump.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import site.hearen.threaddump.service.S3Service;

@RestController
@RequestMapping("/s3")
public class S3Controller {
    @Autowired
    private S3Service s3Service;

    @GetMapping("/list")
    public List<String> listAllSecretKeys() {
        return s3Service.listSecretKeys();
    }

    @PostMapping("/{accessKey}/{secretKey:.+}")
    public void insertNewKey(@PathVariable String accessKey, @PathVariable String secretKey) {
        s3Service.insertNewKey(accessKey, secretKey);
    }
}
