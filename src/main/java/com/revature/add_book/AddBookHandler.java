package com.revature.add_book;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddBookHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Gson mapper = new GsonBuilder().setPrettyPrinting().create();
    private final DynamoDBMapper ddb = new DynamoDBMapper(AmazonDynamoDBClientBuilder.defaultClient());

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {

        LambdaLogger logger = context.getLogger();
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        try {
            Book newBook = mapper.fromJson(requestEvent.getBody(), Book.class);
            String newId = addBook(logger, newBook);
            Map<String, String> respBody = new HashMap<>();
            respBody.put("id", newId);
            responseEvent.setBody(mapper.toJson(respBody));
            responseEvent.setStatusCode(201);
        } catch (Exception e) {
            logger.log("ERROR: " + e);
            responseEvent.setStatusCode(500);
        }

        return responseEvent;

    }

    public String addBook(LambdaLogger logger, Book newBook) {
        logger.log("Attempting to persist object: " + newBook);
        ddb.save(newBook);
        logger.log("Object successfully persisted with id: " + newBook.getId());
        return newBook.getId();
    }

    @Data
    @DynamoDBTable(tableName = "books")
    public static class Book {

        @DynamoDBHashKey
        @DynamoDBAutoGeneratedKey
        private String id;

        @DynamoDBAttribute
        private String isbn;

        @DynamoDBAttribute
        private String title;

        @DynamoDBAttribute
        private String publisher;

        @DynamoDBAttribute
        private List<String> authors;

        @DynamoDBAttribute
        private List<String> genres;

        @DynamoDBAttribute
        private String imageKey;

    }

}
