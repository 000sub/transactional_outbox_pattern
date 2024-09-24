package com.example.producer.outbox.scheduler;


import com.example.producer.lock.DistributedLock;
import com.example.producer.outbox.domain.MessagePublishResult;
import com.example.producer.outbox.domain.OutboxMessage;
import com.example.producer.outbox.repository.OutboxMessageRepository;
import com.example.producer.outbox.repository.ProcessedMessageCustomRepositoryImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@EnableScheduling
@Slf4j
public class OutboxMessageScheduler {

    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ProcessedMessageCustomRepositoryImpl processedMessageCustomRepository;
    private static final int BATCH_SIZE = 1000;

    @Value("${topic.name}")
    private String topicName;


    @DistributedLock(key = "OutboxMessageScheduler.relayMessages")
    @Scheduled(cron = "0/10 * * * * *")
    public void relayMessages() {
        Page<OutboxMessage> messages = outboxMessageRepository.findMessagesToPublish(
                Pageable.ofSize(BATCH_SIZE));

        log.info("foundMessage nums = {}, size = {}", messages.getTotalElements(), messages.getSize());

        if (messages.isEmpty()) {
            return;
        }

        MessagePublishResult result = publishMessages(messages);
        Set<Long> successMessageIds = result.getSuccessMessageIds();
        log.info("success result = {}", successMessageIds.toString());
        processedMessageCustomRepository.batchInsert(BATCH_SIZE, successMessageIds);
    }

    private MessagePublishResult publishMessages(Page<OutboxMessage> messages) {
        Set<Long> successMessageIds = new HashSet<>();
        Set<Long> failMessageIds = new HashSet<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (OutboxMessage msg : messages) {
            CompletableFuture<SendResult<String, String>> send = kafkaTemplate.send(topicName, msg.getPayload());

            CompletableFuture<Void> future = send.whenComplete((result, ex) -> {
                if (ex != null) {
                    failMessageIds.add(msg.getId());
                } else {
                    successMessageIds.add(msg.getId());
                }
            }).thenApply(voidResult -> null);

            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return new MessagePublishResult(successMessageIds, failMessageIds);
    }


}
