package com.grpc.gabrielcampos.laboratorio4.services;

import com.example.grpc.ChatMessage;
import com.example.grpc.ChatServiceGrpc.ChatServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;


@GrpcService
public class ChatService extends ChatServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
    private final ConcurrentLinkedQueue<StreamObserver<ChatMessage>> clients = new ConcurrentLinkedQueue<>();

    @Override
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
        clients.add(responseObserver);
        return new StreamObserver<ChatMessage>() {
            @Override
            public void onNext(ChatMessage message) {
                LOGGER.info("Received message: {} from user: {}", message.getText(), message.getUser());
                // Echo the message to all connected clients
                for (StreamObserver<ChatMessage> client : clients) {
                    client.onNext(message);
                }
            }

            @Override
            public void onError(Throwable t) {
                LOGGER.error("Chat failed: ", t);
                clients.remove(responseObserver);
            }

            @Override
            public void onCompleted() {
                clients.remove(responseObserver);
                responseObserver.onCompleted();
                LOGGER.info("Chat completed.");
            }
        };
    }
}