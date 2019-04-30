package client;

import client.TextMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class MessagePatterns {

    public static final String AUTH_PATTERN = "/auth %s %s";
    public static final String AUTH_SUCCESS_RESPONSE = "/auth successful";
    public static final String AUTH_FAIL_RESPONSE = "/auth fail";
    public static final String USER_ALREADY_AUTHORIZED = "/auth already authorized";


    public static final String DISCONNECT = "/disconnect";
    public static final String CONNECTED = "/connected";
    public static final String CONNECTED_SEND = CONNECTED + " %s";
    public static final String DISCONNECT_SEND = DISCONNECT + " %s";

    public static final String MESSAGE_PREFIX = "/w";
    public static final String MESSAGE_SEND_PATTERN = MESSAGE_PREFIX + " %s %s";

    public static final Pattern MESSAGE_REC_PATTERN = Pattern.compile("^/w (\\w+) (.+)", Pattern.MULTILINE);

    public static TextMessage parseTextMessageRegx(String text, String userTo) {
        Matcher matcher = MESSAGE_REC_PATTERN.matcher(text);
        if (matcher.matches()) {
            return new TextMessage(matcher.group(1), userTo,
                    matcher.group(2));
        } else {
            System.out.println("Unknown message pattern: " + text);
            return null;
        }
    }

    public static TextMessage parseTextMessage(String text, String userTo) {
        String[] parts = text.split(" ", 3);
        if (parts.length == 3 && parts[0].equals(MESSAGE_PREFIX)) {
            return new TextMessage(parts[1], userTo, parts[2]);
        } else {
            System.out.println("Unknown message pattern: " + text);
            return null;
        }
    }

    public static String parseConnectedMessage(String text) {
        String[] parts = text.split(" ");
        if (parts.length == 2 && parts[0].equals(CONNECTED)) {
            return parts[1];
        } else {
            System.out.println("Unknown message pattern: " + text);
            return null;
        }
    }

    public static String parseDisconnectedMessage(String text) {
        String[] parts = text.split(" ");
        if (parts.length == 2 && parts[0].equals(DISCONNECT)) {
            return parts[1];
        } else {
            System.out.println("Unknown message pattern: " + text);
            return null;
        }
    }
}