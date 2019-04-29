import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;


public class Network implements Closeable {

    public Socket socket;
    public DataInputStream in;
    public DataOutputStream out;

    private String hostName;
    private int port;
    private MessageReciever messageReciever;

    private String login;

    private Thread receiverThread;

    public Network(String hostName, int port, MessageReciever messageReciever) {
        this.hostName = hostName;
        this.port = port;
        this.messageReciever = messageReciever;

        this.receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        String text = in.readUTF();

                        TextMessage msg = MessagePatterns.parseTextMessageRegx(text, login);
                        if (msg != null) {
                            System.out.println("New message " + text);
                            messageReciever.submitMessage(msg);
                            continue;
                        }


                        String connectedLogin = MessagePatterns.parseConnectedMessage(text);
                        if (connectedLogin != null) {
                            System.out.println("Connection message " + text);
                            messageReciever.userConnected(connectedLogin);
                            continue;
                        }


                        String disConnectedLogin = MessagePatterns.parseDisconnectedMessage(text);
                        if (disConnectedLogin != null) {
                            System.out.println("Disconnection message " + text);
                            messageReciever.userDisconnected(disConnectedLogin);
                            continue;
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                        if (socket.isClosed()) {
                            break;
                        }
                    }
                }
            }
        });
    }

    public void authorize(String login, String password) throws IOException, AuthException {
        socket = new Socket(hostName, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        sendMessage(String.format(MessagePatterns.AUTH_PATTERN, login, password));
        String response = in.readUTF();
        if (response.equals(MessagePatterns.AUTH_SUCCESS_RESPONSE)) {
            this.login = login;
            receiverThread.start();
        } else {
            throw new AuthException();
        }
    }

    public void sendTextMessage(TextMessage message) {
        sendMessage(String.format(MessagePatterns.MESSAGE_SEND_PATTERN, message.getUserTo(), message.getText()));
    }

    private void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public String getLogin() {
        return login;
    }

    @Override
    public void close() {
        this.receiverThread.interrupt();
        sendMessage(MessagePatterns.DISCONNECT);
    }


}