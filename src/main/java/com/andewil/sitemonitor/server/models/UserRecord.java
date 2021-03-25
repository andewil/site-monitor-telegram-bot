package com.andewil.sitemonitor.server.models;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRecord {
    public enum ChatMode {MENU, INPUT}

    private UUID userId;
    private long chatId;
    private ChatMode chatMode = ChatMode.MENU;
    private String command;

    public static ChatMode parseChatMode(String s) {
        String lowerStr = s.toLowerCase();
        switch (lowerStr) {
            case "menu":
                return ChatMode.MENU;
            case "input":
                return ChatMode.INPUT;
            default:
                return ChatMode.MENU;
        }
    }
}
