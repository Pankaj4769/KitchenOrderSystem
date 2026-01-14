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

        SseEmitter emitter = new SseEmitter(0L); // no timeout
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    // Called internally when order changes
    public void sendOrderUpdate(Order order) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(order);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
