package com.sange.telegram.bot.handler.example.command;

import com.sange.telegram.bot.handler.CommandHandler;
import com.sange.telegram.bot.handler.example.command.handler.ExampleCommandHandler;
import com.sange.telegram.bot.handler.example.command.handler.StartCommandHandler;

import java.util.HashMap;
import java.util.Map;

public class ExampleCommandContext {

    private static final Map<String, CommandHandler> COMMAND_HANDLERS = new HashMap<>();

    static {
        COMMAND_HANDLERS.put("/example", new ExampleCommandHandler());
        COMMAND_HANDLERS.put("/start", new StartCommandHandler());
    }

    public static void execute(String command, long chatId, String args) {
        CommandHandler handler = COMMAND_HANDLERS.get(command);
        if (handler != null) {
            handler.handle(chatId, args);
        } else {
            //这里可以抛异常 也可以单独处理
            throw new IllegalArgumentException("Unknown command: " + command);
        }
    }
}
