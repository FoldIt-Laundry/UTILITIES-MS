package com.foldit.utilites.dao;

import com.foldit.utilites.negotiationconfigholder.model.Configuration;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IConfigDetails extends MongoRepository<Configuration,String> {

    @Aggregation({
            "{ $match: { 'configKey': ?0 }},",
            "{ $project: { 'configValue': 1}}"
    })
    Configuration getConfigValue(String configKey);

}
