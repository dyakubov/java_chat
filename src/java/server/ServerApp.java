package server;

import javax.swing.*;

public class ServerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ServerWindow serverWindow = new ServerWindow();
            }
        });
    }
}
