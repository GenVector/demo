package com.richinfoai.evillesspipelinemergehiai.repo.mongoDB;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.richinfoai.evillesspipelinemergehiai.entity.DocContainerInfo;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MongoDBRepo {

    private MongoTemplate mongoTemplate;
    private MongoClient mongoClient;


    public MongoDBRepo(String dbName, String host, Integer port) {
        mongoClient = new MongoClient(new ServerAddress(host, port));
        SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongoClient, dbName);
        this.mongoTemplate = new MongoTemplate(factory);
        log.info(String.format("连接mongo数据库成功host:%s-----port:%s----dbName:%s", host, port.toString(), dbName));
    }

    public MongoDBRepo(String dbName, String host, Integer port, String userName, String password) {
        try {
            mongoClient = new MongoClient(new ServerAddress(host, port),
                    MongoCredential.createScramSha1Credential(userName, dbName,
                            IOUtils.toCharArray(new StringReader(password))),
                    MongoClientOptions.builder().build());
            SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongoClient, dbName);
            this.mongoTemplate = new MongoTemplate(factory);
        } catch (IOException e) {
            log.info(String.format("连接mongo数据库失败host:%s-----port:%s----dbName:%s", host, port.toString(), dbName));
            throw new RuntimeException(String.format("连接mongo数据库%s/%s失败", host + port, dbName), e);
        }
        log.info(String.format("连接mongo数据库成功host:%s-----port:%s----dbName:%s", host, port.toString(), dbName));
    }

    public void closeClient() {
        this.mongoClient.close();
    }

    public List<DocContainerInfo> getCollections(String collectionName) {

        return mongoTemplate.findAll(DocContainerInfo.class, collectionName);
    }

    public DocContainerInfo getInstance(String documentId, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("documentId").is(documentId));
        return mongoTemplate.findOne(query, DocContainerInfo.class, collectionName);
    }

    public List<DocContainerInfo> getCollection(String collectionName, long start, int size) {
        Query query = new Query();
        query.skip(start);
        query.limit(size);
        //query.with(new Sort(Sort.Direction.ASC, "documentId"));
        return mongoTemplate.find(query, DocContainerInfo.class, collectionName);
    }

    public void save(DocContainerInfo docContainerInfo, String collectionName) {
        mongoTemplate.save(docContainerInfo, collectionName);
    }

    public DocContainerInfo update(DocContainerInfo docContainerInfo, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(docContainerInfo.get_id()));
        mongoTemplate.findAndRemove(query, DocContainerInfo.class, collectionName);
        save(docContainerInfo, collectionName);
        return docContainerInfo;
    }

    public void removeAll(String collectionName) {
        Query query = new Query();
        query.addCriteria(new Criteria());
        mongoTemplate.remove(query, collectionName);
        //mongoTemplate.findAllAndRemove(query, collectionName);
    }

    public void saveAll(List<DocContainerInfo> docContainerInfoList, String collectionName) {
        mongoTemplate.insert(docContainerInfoList, collectionName);
    }

    public void remove(String key, String collectionName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(key));
        mongoTemplate.remove(query, DocContainerInfo.class, collectionName);
    }
}
