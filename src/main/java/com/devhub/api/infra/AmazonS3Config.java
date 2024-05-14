package com.devhub.api.infra;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@Getter
@Setter
@NoArgsConstructor
@PropertySource("classpath:s3.properties")
public class AmazonS3Config {

    @Value("${accessKey}")
    private String accessKey;
    @Value("${secret}")
    private String secret;
    @Value("${region}")
    private String region;
    @Value("${bucketName}")
    private String bucketName;
    @Value("${sessionToken}")
    private String sessionToken;

    public AmazonS3Config(String accessKey, String secret, String region, String bucketName, String sessionToken) {
        this.accessKey = accessKey;
        this.secret = secret;
        this.region = region;
        this.bucketName = bucketName;
        this.sessionToken = sessionToken;
    }

    @Bean
    public AmazonS3 s3() {
        AWSCredentials credentials = new BasicSessionCredentials(accessKey, secret, sessionToken);
        return AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }
}