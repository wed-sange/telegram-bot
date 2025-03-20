package com.sange.telegram.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.sange.telegram.bot.util.http.HttpClientFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 机器人管理器
 * 支持多机器人处理
 */
@Slf4j
public class BotManager {
    private static final Map<String, List<TelegramBot>> BOT_HANDLERS = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> ROUND_ROBIN_INDEX = new ConcurrentHashMap<>();

    public static void init(String handlerName, String[] tokens, Consumer<Update> messageHandler) {
        List<TelegramBot> bots = new ArrayList<>();
        for (String token : tokens) {
            TelegramBot bot = new TelegramBot.Builder(token).okHttpClient(HttpClientFactory.createOkHttpClient(true)).build();

            // 设置消息监听
            bot.setUpdatesListener(updates -> {
                for (Update update : updates) {
                    if (update.message() != null) {
                        try {
                            long chatId = update.message().chat().id();
                            switch (update.message().text()) {
                                case "/dialog":
                                    bot.execute(new SendMessage(chatId, String.valueOf(chatId)));
                                    break;
                                default:
                                    messageHandler.accept(update);
                                    break;
                            }
                        } catch (Exception e) {
                            log.error("收到无法解析的消息:{}", update.message(), e);
                        }
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            });
            log.info("{}机器人:{} ==> 初始化成功", handlerName, bot);
            bots.add(bot);
        }

        BOT_HANDLERS.put(handlerName, bots);
        ROUND_ROBIN_INDEX.put(handlerName, new AtomicInteger(0));
    }

    /**
     * 群发
     * @param handlerName
     * @param chatIds
     * @param message
     */
    public static void sendToHandler(String handlerName, Long[] chatIds, String message) {
        for (int i = 0; i < chatIds.length; i++) {
            sendToHandler(handlerName, chatIds[i], message);
        }
    }

    /**
     * 发送富文本消息
     * @param handlerName
     * @param chatId
     * @param message 消息内容
     */
    public static void sendToHandler(String handlerName, Long chatId, String message) {
        List<TelegramBot> bots = BOT_HANDLERS.get(handlerName);
        if (bots == null || bots.isEmpty()) return;

        int index = ROUND_ROBIN_INDEX.get(handlerName)
                .getAndUpdate(i -> (i + 1) % bots.size());
        TelegramBot bot = bots.get(index);
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.parseMode(ParseMode.Markdown);
        bot.execute(sendMessage);
    }

    /**
     * 群发文件
     * @param handlerName
     * @param chatIds
     * @param file
     * @param caption
     */
    public static void sendFileToHandler(String handlerName, Long[] chatIds, File file, String caption) {
        for (int i = 0; i < chatIds.length; i++) {
            sendFileToHandler(handlerName, chatIds[i], file, caption);
        }
    }

    /**
     * 发送文件
     * @param handlerName 处理程序名称
     * @param chatId
     * @param file 文件
     * @param caption 标题
     */
    public static void sendFileToHandler(String handlerName, Long chatId, File file, String caption) {
        List<TelegramBot> bots = BOT_HANDLERS.get(handlerName);
        if (bots == null || bots.isEmpty()) return;

        int index = ROUND_ROBIN_INDEX.get(handlerName).getAndUpdate(i -> (i + 1) % bots.size());
        TelegramBot bot = bots.get(index);

        if (file != null && file.exists()) {
            SendDocument sendDocument = new SendDocument(chatId, file);
            if (caption != null && !caption.isEmpty()) {
                sendDocument.caption(caption).parseMode(ParseMode.Markdown);
            }

            bot.execute(sendDocument);
            log.info("发送文件成功: chatId={} file={} caption={}", chatId, file.getName(), caption);
        } else {
            log.warn("文件不存在或无效: {}", file);
        }
    }
}
