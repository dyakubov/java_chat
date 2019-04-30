package server;

import client.Network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ServerWindow extends JFrame implements EventsReciever{
    private final JList<String> connectedUsersList;
    ChatServer currentChatServer = null;
    private final JPanel connectionButtons;
    private final JTextArea chatLogArea;
    private final JButton startServerButton;
    private final JButton stopServerButton;
    private final DefaultListModel<String> connectedUsersModel;



    public ServerWindow (){

        setBounds(200, 200, 500, 500);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        connectedUsersList = new JList<>();
        connectedUsersModel = new DefaultListModel<String>();
        connectedUsersList.setModel(connectedUsersModel);
        add(connectedUsersList, BorderLayout.WEST);

        chatLogArea = new JTextArea();
        chatLogArea.setPreferredSize(new Dimension(this.getWidth(), 50));
        add(chatLogArea, BorderLayout.SOUTH);

        connectionButtons = new JPanel();
        connectionButtons.setLayout(new GridBagLayout());
        startServerButton = new JButton("Старт");
        stopServerButton = new JButton("Стоп");
        connectionButtons.add(startServerButton);
        connectionButtons.add(stopServerButton);
        add(connectionButtons);

        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentChatServer == null) {
                    Thread startThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ChatServer chatServer = new ChatServer();
                            currentChatServer = chatServer;
                            currentChatServer.start(this);


                        }

                    });
                    startThread.start();

                }
                else {
                    JOptionPane.showMessageDialog(ServerWindow.this,
                            "Сервер уже работает",
                            "Запуск сервера",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        stopServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    currentChatServer.stop();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });

        setTitle("Чат-сервер");


        setVisible(true);

    }

    @Override
    public void log(String event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                chatLogArea.setText(event);
            }
        });
    }

    @Override
    public void setNewTitle(String title) {

    }
}
