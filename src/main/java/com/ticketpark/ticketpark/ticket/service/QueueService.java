package com.ticketpark.ticketpark.ticket.service;

import com.ticketpark.ticketpark.common.redis.QueueRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final QueueRedisService queueRedisService;
    private static final String WAITING_KEY = "ticket:waiting:";
    private static final String WORKING_KEY = "ticket:working:";

    public Long enterQueue(String userId, String ticketIdx) {
        long score = System.currentTimeMillis();
        queueRedisService.addQueue(WAITING_KEY + ticketIdx, userId, score);
        return queueRedisService.getRank(WAITING_KEY + ticketIdx, userId);
    }

    public Long getWaitCount(String userId, String ticketIdx) {
        Long rank = queueRedisService.getRank(WAITING_KEY + ticketIdx, userId);
        return (rank != null) ? rank + 1 : -1L; // 0번부터 시작하므로 +1
    }

    public boolean isAllowed(String userId, String ticketIdx) {
        Object data = queueRedisService.getValues(WORKING_KEY + ticketIdx + ":" + userId);
        return data != null;
    }

    public void allowUsers(String ticketIdx, long count) {
        Set<Object> users = queueRedisService.getTopOrder(WAITING_KEY + ticketIdx, count);

        for (Object user : users) {
            String userId = (String) user;

            queueRedisService.setValues(WORKING_KEY + ticketIdx + ":" + userId, "allowed", Duration.ofMinutes(5));
            queueRedisService.removeUser(WAITING_KEY + ticketIdx, userId);
        }
    }

    public void finishTicketing(String ticketIdx, String userId) {
        queueRedisService.deleteValues(WORKING_KEY + ticketIdx + ":" + userId);
    }

}
