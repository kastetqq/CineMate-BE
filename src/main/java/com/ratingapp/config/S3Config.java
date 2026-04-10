package com.ratingapp.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class S3Config {

    @Value("${timeweb.s3.access-key}")
    private String accessKey;

    @Value("${timeweb.s3.secret-key}")
    private String secretKey;

    @Value("${timeweb.s3.endpoint}")
    private String endpoint;

    @Value("${timeweb.s3.region}")
    private String region;

    @Value("${timeweb.s3.bucket-name}")
    private String bucketName;

    @Value("${timeweb.s3.public-url}")
    private String publicUrl;

    @Bean
    public AmazonS3 amazonS3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        
        return AmazonS3ClientBuilder
                .standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .withPathStyleAccessEnabled(true)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}