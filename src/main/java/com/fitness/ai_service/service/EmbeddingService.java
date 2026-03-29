package com.fitness.ai_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final VectorStore vectorStore;

    public void storeText(Integer userId, String domain, LocalDateTime eventTime, String referenceId, String text) {

        Map<String, Object> metadata = Map.of(
                "userId", userId,
                "domain", domain,
                "eventTime", eventTime.toString(),
                "referenceId", referenceId
        );

        Document document = new Document(text, metadata);
        vectorStore.add(List.of(document));
    }

    public void deleteByReferenceId(String domain, String referenceId) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filterExpression = b.and(
                b.eq("domain", domain),
                b.eq("referenceId", referenceId)
        ).build();
        vectorStore.delete(filterExpression);
    }

    public List<Document> similaritySearch(Integer userId, String domain, String query, int topK) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filterExpression = b.and(
                b.eq("userId", userId),
                b.eq("domain", domain)
        ).build();
        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .filterExpression(filterExpression)
                        .topK(topK)
                        .build()
        );
    }
}
