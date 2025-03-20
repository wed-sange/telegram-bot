package com.sange.telegram.bot.handler.example.command.handler;

import com.sange.telegram.bot.BotManager;
import com.sange.telegram.bot.handler.CommandHandler;
import com.sange.telegram.bot.handler.HandlerName;

public class ExampleCommandHandler implements CommandHandler {

    @Override
    public void handle(long chatId, String args) {
        BotManager.sendToHandler(HandlerName.EXAMPLE, chatId, "这是一个示例！😊");
    }
}
