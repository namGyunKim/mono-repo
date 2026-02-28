package com.example.global.config.social;

import com.example.domain.social.google.client.GoogleApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class SocialApiClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient googleApiRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${social.google.apiBaseUrl}") String baseUrl
    ) {
        return restClientBuilder.baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient googleOauthRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${social.google.revokeBaseUrl:https://oauth2.googleapis.com}") String baseUrl
    ) {
        return restClientBuilder.baseUrl(baseUrl).build();
    }

    @Bean
    public GoogleApiClient googleApiClient(RestClient googleApiRestClient) {
        return createClient(googleApiRestClient, GoogleApiClient.class);
    }

    @Bean
    public GoogleOauthClient googleOauthClient(RestClient googleOauthRestClient) {
        return createClient(googleOauthRestClient, GoogleOauthClient.class);
    }

    private <T> T createClient(RestClient restClient, Class<T> clientType) {
        final HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(clientType);
    }
}
