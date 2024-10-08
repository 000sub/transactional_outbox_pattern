package com.example.consumer.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer implements AcknowledgingMessageListener<String, String> {

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @KafkaListener(topics = "test-topic",
            containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> consumerRecord, Acknowledgment acknowledgment) {
        String value = consumerRecord.value();
        try {
            // JSON 파싱 (재파싱 필요)
            JsonNode intermediateNode = objectMapper.readTree(value);

            // 중첩된 JSON을 다시 파싱
            String actualJson = intermediateNode.asText();
            JsonNode root = objectMapper.readTree(actualJson);

            // reservationId 추출
            JsonNode idNode = root.path("id");
            long id = idNode.asLong();
            log.info("Extracted Id: {}", id);

            Query query = Query.query(Criteria.where("id").is(id));
            Document document = Document.parse(actualJson);
            mongoTemplate.replace(query, document, ReplaceOptions.replaceOptions().upsert(),
                    "testCollection");

            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
