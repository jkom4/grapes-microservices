package grapes.microservices.frontendchat.models.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import grapes.microservices.frontendchat.models.Message;
import grapes.microservices.frontendchat.models.Topic;
import grapes.microservices.frontendchat.models.dto.MessageDTO;
import grapes.microservices.frontendchat.models.dto.MessageMapper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mock implementation of MulticastService for testing without actual network.
 */
public class MulticastService implements IMulticastService {
    private Topic currentTopic;
    MulticastSocket multicastSocket;
    InetAddress adresseGroupe;
    private static Gson gson;
    private ExecutorService listenerExecutor;
    private volatile boolean running = false;
    private final ObjectProperty<Message> messageReveiceObserver;

    @Override
    public ObjectProperty<Message> getMessageReveiceObserver() {
        return messageReveiceObserver;
    }

    private final byte[] buf = new byte[30000];

    private final int port;

    public MulticastService(int port) {
        this.port = port;
        this.messageReveiceObserver = new SimpleObjectProperty<>();
        gson = new Gson();
    }

    @Override
    public void init() throws IOException {
        System.out.println("[MulticastService] Start Multicast service");
        multicastSocket = new MulticastSocket(port);

        // Use a single-threaded executor for the listener task
        listenerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "MulticastListenerThread");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void close() {
        System.out.println("[MulticastService] Stop Multicast service...");
        running = false;
        multicastSocket.close();
    }

    @Override
    public void joinTopic(Topic topic) throws IOException {
        System.out.println("[MulticastService] Joining topic: " + topic);

        this.currentTopic = topic;

        NetworkInterface ni;
        try (MulticastSocket s = new MulticastSocket()) {
            ni = s.getNetworkInterface();
        }
        adresseGroupe = InetAddress.getByName(topic.getMulticastGroup());
        InetSocketAddress socketAddress = new InetSocketAddress(adresseGroupe, port);
        multicastSocket.joinGroup(socketAddress, ni);
        running = true;
        System.out.println("[MulticastService] Listening to group: " + adresseGroupe + ".");
        listenerExecutor.submit(this::listenInLoop);
    }

    @Override
    public void leaveTopic(Topic topic) throws IOException {
        System.out.println("[MulticastService] Leaving topic: " + topic);
        if (this.currentTopic != null && this.currentTopic.equals(topic)) {
            this.currentTopic = null;
        }
        adresseGroupe = InetAddress.getByName(topic.getMulticastGroup());
        multicastSocket.leaveGroup(adresseGroupe);
        running = false; // stop the thread task
    }

    @Override
    public void sendMessage(Topic topic, Message message) throws IOException {
        MessageDTO dto =MessageMapper.toDTO(message); // convert message into messageDTO
        String data = gson.toJson(dto); // convert messageDTO into json
        DatagramPacket dtg = new DatagramPacket(data.getBytes(), data.length(), adresseGroupe, port);
        // send
        multicastSocket.send(dtg);
        System.out.println("[MulticastService] Message sent to topic " + topic.id() + ": " + data);
    }

    // task executed by the thread listener
    private void listenInLoop() {
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (running) {
            try {
                multicastSocket.receive(packet);
            } catch (Exception e) {
                // Catch any other unexpected errors
                if (running) {
                    System.err.println("Unexpected error in listener loop: " + e.getMessage());
                    e.printStackTrace();
                    running = false; // Stop on unexpected errors
                }
            }

            if (!running) {
                break;
            }

            // Process the received packet
            String jsonPayload = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
            MessageDTO dto;
            try {
                dto = gson.fromJson(jsonPayload, MessageDTO.class);
            } catch (JsonSyntaxException e) {
                System.err.println("Not valid Json: " + jsonPayload);
                continue;
            }
            Message message = MessageMapper.toEntity(dto);

            // notify listeners of new message
            messageReveiceObserver.set(message);
        }
    }
}