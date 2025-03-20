package com.sange.telegram.bot.handler.example;

import com.pengrad.telegrambot.model.Update;
import com.sange.telegram.bot.BotManager;
import com.sange.telegram.bot.handler.HandlerName;
import com.sange.telegram.bot.handler.example.command.ExampleCommandContext;
import com.sange.telegram.bot.util.properties.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleHandlerService extends BotManager {
    /**
     * 初始化 Example 机器人
     */
    public static void init() {
        init(HandlerName.EXAMPLE, ConfigUtils.get("example.bots", String[].class), ExampleHandlerService::update);
    }

    private static void update(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        String[] parts = messageText.split(" ", 2);
        String command = parts[0];
        String args = parts.length > 1 ? parts[1] : null;

        try {
            ExampleCommandContext.execute(command, chatId, args);
        } catch (IllegalArgumentException e) {
             log.error(e.getMessage(), e);
             BotManager.sendToHandler(HandlerName.EXAMPLE, chatId, "❌ 未知命令: `" + command + "`");
        }
    }
}
