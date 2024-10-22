package com.gucardev.backend.socket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.gucardev.backend.constants.Constants;
import com.gucardev.backend.model.Message;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SocketModule {


    private final SocketIOServer server;
    private final SocketService socketService;
    
//    private final Map<String, SocketIOClient> clients = new HashMap<>();
    
//    private final Map<String, SocketIOClient> clients = new ConcurrentHashMap<>();

    public SocketModule(SocketIOServer server, SocketService socketService) {
        this.server = server;
        this.socketService = socketService;
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
//        server.addEventListener("register", String.class, (client, userId, ackRequest) -> onRegister(client, userId));
      
        server.addEventListener("send_message", Message.class, onChatReceived());
        
//        server.addNamespace("/chat").addListeners(new DataListener<Message>() {
//            @Override
//            public void onData(SocketIOClient client, Message message, AckRequest ackRequest) {
//                System.out.println("Message from " + client.getSessionId() + ": " + message.getContent());
//                // Find the recipient client and send the message
//                SocketIOClient recipient = clients.get(message.getUsername());
//                if (recipient != null) {
//                    recipient.sendEvent("private-message", message);
//                }
//            }
//        });
//
//        // Handle client connection and registration
//        server.addEventListener("register", String.class, (client, username, ackRequest) -> {
//            clients.put(username, client);
//            client.sendEvent("registered", "You are registered as " + username);
//        });
        
//        server.addEventListener("send_private_message", String.class, (client, data, ackRequest) -> {
//            String[] parts = data.split(":", 2);
//            onSendPrivateMessage(client, parts[0], parts[1]);
//        });

    }


    private DataListener<Message> onChatReceived() {
        return (senderClient, data, ackSender) -> {
//            log.info(data.toString());
            socketService.saveMessage(senderClient, data);
        };
    }


    private ConnectListener onConnected() {
        return (client) -> {
//            String room = client.getHandshakeData().getSingleUrlParam("room");
//            String username = client.getHandshakeData().getSingleUrlParam("room");
            var params = client.getHandshakeData().getUrlParams();
            String room = params.get("room").stream().collect(Collectors.joining());
         
            String username = params.get("username").stream().collect(Collectors.joining());
            server.addNamespace("/chat");
            client.joinRoom(room);
            socketService.saveInfoMessage(client, String.format(Constants.WELCOME_MESSAGE, username), room);
//            log.info("Socket ID[{}] - room[{}] - username [{}]  Connected to chat module through", client.getSessionId().toString(), room, username);
        };

    }

    private DisconnectListener onDisconnected() {
        return client -> {
            var params = client.getHandshakeData().getUrlParams();
            String room = params.get("room").stream().collect(Collectors.joining());
            String username = params.get("username").stream().collect(Collectors.joining());
            socketService.saveInfoMessage(client, String.format(Constants.DISCONNECT_MESSAGE, username), room);
//            log.info("Socket ID[{}] - room[{}] - username [{}]  discnnected to chat module through", client.getSessionId().toString(), room, username);
        };}
    
    
//    private void onSendPrivateMessage(SocketIOClient client, String recipientId, String message) {
//       if(recipientId != null) {
//    	SocketIOClient recipient = clients.get(recipientId);
//    	 if (recipient != null) {
//             recipient.sendEvent("receive_private_message", message);
//         }
//    	
//       }
//       
//       
//
//    }
//    
//    private void onRegister(SocketIOClient client, String userId) {
//        clients.put(userId, client);
//    }
}
