package com.kos.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.kos.dto.Order;

@RestController
public class OrderSseController {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Client subscribes here
    @GetMapping("/order-stream")
    public SseEmitter streamOrders() {

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

        return emitter;
    }

    // Called internally when order changes
    public void sendOrderUpdate(Order order) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(order);
            } catch (Exception e) {
                // Client disconnected or emitter in bad state — clean up silently
                emitters.remove(emitter);
                try { emitter.completeWithError(e); } catch (Exception ignored) {}
            }
        }
    }

    /** Returns number of currently connected SSE clients (for debugging). */
    public int getConnectedClientCount() {
        return emitters.size();
    }
}
