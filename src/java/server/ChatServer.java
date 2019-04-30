package server;

import client.MessagePatterns;
import client.TextMessage;
import exeptions.AuthException;
import exeptions.UserExistException;
import exeptions.WrongLoginPasswordException;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class ChatServer {

    private AuthService authService = new AuthServiceImpl();
    private Map<String, ClientHandler> clientHandlerMap = Collections.synchronizedMap(new HashMap<>());
    private ServerSocket currentSocket;
    private EventsReciever eventsReciever;



    public void start(EventsReciever eventsReciever) {
        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            currentSocket = serverSocket;
            this.eventsReciever = eventsReciever;
            eventsReciever.log("Сервер запущен");
            while (!currentSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                DataInputStream inp = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                eventsReciever.log("Новый клиент подключился");

                User user = null;
                try {
                    String authMessage = inp.readUTF();
                    user = checkAuthentication(authMessage);
                }  catch (WrongLoginPasswordException ex) {
                    out.writeUTF(MessagePatterns.AUTH_FAIL_RESPONSE);
                    out.flush();
                    socket.close();
                } catch (UserExistException ex){
                    out.writeUTF(MessagePatterns.USER_ALREADY_AUTHORIZED);
                    out.flush();
                    socket.close();
                } catch (IOException| AuthException ex) {
                    ex.printStackTrace();
                }
                if (user != null && authService.authUser(user)) {
                    System.out.printf("User %s authorized successful!%n", user.getLogin());
                    subscribe(user.getLogin(), socket);
                    out.writeUTF(MessagePatterns.AUTH_SUCCESS_RESPONSE);
                    out.flush();
                    sendConnectedUsers();
                } else {
                    if (user != null) {
                        System.out.printf("Wrong authorization for user %s%n", user.getLogin());
                    }
                    out.writeUTF(MessagePatterns.AUTH_FAIL_RESPONSE);
                    out.flush();
                    socket.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private User checkAuthentication(String authMessage) throws AuthException {
        String[] authParts = authMessage.split(" ");
        if (authParts.length != 3 || !authParts[0].equals("/auth")) {
            System.out.printf("Incorrect authorization message %s%n", authMessage);
            throw new AuthException("Неверное авторизационное сообщение");
        } else if (clientHandlerMap.containsKey(authParts[1])){
            System.out.printf("User %s already authorized %n", authParts[1]);
            throw new UserExistException();
        }
        return new User(authParts[1], authParts[2]);
    }

    private void sendUserConnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending connect notification to %s about %s%n", clientHandler.getLogin(), login);
                clientHandler.sendConnectedMessage(login);
            }
        }
    }

    private void sendUserDisconnectedMessage(String login) throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()) {
            if (!clientHandler.getLogin().equals(login)) {
                System.out.printf("Sending disconnect notification to %s about %s%n", clientHandler.getLogin(), login);
                clientHandler.sendDisconnectedMessage(login);
            }
        }
    }


    public void sendMessage(TextMessage msg) throws IOException {
        ClientHandler userToClientHandler = clientHandlerMap.get(msg.getUserTo());
        if (userToClientHandler != null) {
            userToClientHandler.sendMessage(msg.getUserFrom(), msg.getText());
        } else {
            System.out.printf("User %s not connected%n", msg.getUserTo());
        }
    }

    public void subscribe(String login, Socket socket) throws IOException {
            clientHandlerMap.put(login, new ClientHandler(login, socket, this));
            sendUserConnectedMessage(login);
            eventsReciever.log(login + " подписался");
    }

    public void unsubscribe(String login) throws IOException {
        clientHandlerMap.remove(login);
        sendUserDisconnectedMessage(login);
    }

    public void sendConnectedUsers() throws IOException {
        for (ClientHandler clientHandler : clientHandlerMap.values()){
            sendUserConnectedMessage(clientHandler.getLogin());
        }
        eventsReciever.log("Запрос пользователей");

    }

    public void stop() throws IOException {
        currentSocket.close();
        System.out.println("Сервер остановлен");
        eventsReciever.log("Сервер остановлен");

    }

}
