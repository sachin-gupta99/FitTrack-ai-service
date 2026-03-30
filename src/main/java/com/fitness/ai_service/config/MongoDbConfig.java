package com.fitness.ai_service.config;

import com.fitness.ai_service.service.ParameterStoreService;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@RequiredArgsConstructor
public class MongoDbConfig extends AbstractMongoClientConfiguration {

    private final MongoDbProperties mongoDbProperties;
    private final ParameterStoreService parameterStoreService;

    @Override
    protected String getDatabaseName() {
        return parameterStoreService.getParameterValue(mongoDbProperties.getDatabase());
    }

    @Override
    public MongoClient mongoClient() {
        String uri = parameterStoreService.getParameterValue(mongoDbProperties.getUri());
        String database = parameterStoreService.getParameterValue(mongoDbProperties.getDatabase());

        System.out.println("Fetched uri: " + uri);
        System.out.println("Fetched database: " + database);

        ConnectionString connectionString = new ConnectionString(
                uri + database + "?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        return MongoClients.create(settings);
    }
}
