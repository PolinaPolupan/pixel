package com.example.pixel.node.scheduler;

import com.example.pixel.common.integration.NodeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class NodeLoader {

    private final NodeClient nodeClient;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        int maxRetries = 10;
        int delayMs = 2000;

        for (int i = 0; i < maxRetries; i++) {
            try {
                nodeClient.loadNodes();
                return;
            } catch (Exception e) {
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
            }
        }

        log.error("Failed to load nodes");
    }
}