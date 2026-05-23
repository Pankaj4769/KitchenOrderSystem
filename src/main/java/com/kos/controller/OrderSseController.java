package com.kos.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.kos.dto.Order;

@RestController
public class OrderSseController {

    private static final Logger logger = LogManager.getLogger(OrderSseController.class);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Client subscribes here
    @GetMapping("/order-stream")
    public SseEmitter streamOrders() {
        logger.info("Entering streamOrders()");
        try {
            // ✅ FIX: Use 60-second timeout instead of 0L (infinite).
            // 0L never times out — each connection holds a Tomcat thread + DB connection
            // forever, starving HikariPool. The browser auto-reconnects after timeout,
            // so this is transparent to the client.
            SseEmitter emitter = new SseEmitter(60_000L);
            emitters.add(emitter);

            Runnable cleanup = () -> {
                emitters.remove(emitter);
                // ✅ FIX: explicitly complete the emitter on timeout.
                // Without this, the timeout propagates to Spring's async dispatcher
                // which calls GlobalExceptionHandler — but the response Content-Type
                // is already 'text/event-stream' so no JSON converter can write ErrorResponse.
                try { emitter.complete(); } catch (Exception ignored) {}
            };
            emitter.onCompletion(() -> emitters.remove(emitter));
            emitter.onTimeout(cleanup);
            emitter.onError(e -> emitters.remove(emitter));

            // ✅ Send an initial heartbeat comment so the browser knows the connection is live.
            // This also flushes the response buffer immediately (some proxies buffer SSE).
            try {
                emitter.send(SseEmitter.event().comment("connected").build());
            } catch (IOException e) {
                emitters.remove(emitter);
            }

            logger.info("Exiting streamOrders()");
            return emitter;
        } catch (RuntimeException e) {
            logger.error("Error in streamOrders(): {}", e.getMessage(), e);
            throw e;
        }
    }

    // Called internally when order changes
    public void sendOrderUpdate(Order order) {
        logger.info("Entering sendOrderUpdate()");
        try {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(order);
                } catch (Exception e) {
                    // Client disconnected or emitter in bad state — clean up silently
                    emitters.remove(emitter);
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            }
            logger.info("Exiting sendOrderUpdate()");
        } catch (RuntimeException e) {
            logger.error("Error in sendOrderUpdate(): {}", e.getMessage(), e);
            throw e;
        }
    }

    /** Returns number of currently connected SSE clients (for debugging). */
    public int getConnectedClientCount() {
        logger.info("Entering getConnectedClientCount()");
        try {
            int result = emitters.size();
            logger.info("Exiting getConnectedClientCount()");
            return result;
        } catch (RuntimeException e) {
            logger.error("Error in getConnectedClientCount(): {}", e.getMessage(), e);
            throw e;
        }
    }
}
