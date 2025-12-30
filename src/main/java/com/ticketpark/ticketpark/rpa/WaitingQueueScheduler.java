package com.ticketpark.ticketpark.rpa;

import com.ticketpark.ticketpark.common.redis.QueueRedisService;
import com.ticketpark.ticketpark.ticket.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class WaitingQueueScheduler {
    private final QueueService queueService;
    private final QueueRedisService queueRedisService;

    @Scheduled(fixedDelay = 1000)
    public void processQueue() {

        Set<String> waitingKeys = queueRedisService.getAllKeys("ticket:waiting:*");

        if (waitingKeys == null || waitingKeys.isEmpty()) return;

        for (String key : waitingKeys) {
            String eventId = key.split(":")[2];
            queueService.allowUsers(eventId, 60);
        }
    }
}
