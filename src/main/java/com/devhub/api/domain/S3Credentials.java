package com.devhub.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class S3Credentials {

    private String accessKey;
    private String secret;
    private String region;
    private String bucketName;
    private String sessionToken;

    public S3Credentials(String accessKey, String secret, String region, String bucketName) {
        this.accessKey = accessKey;
        this.secret = secret;
        this.region = region;
        this.bucketName = bucketName;
    }
}