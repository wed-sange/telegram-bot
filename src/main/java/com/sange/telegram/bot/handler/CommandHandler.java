package com.sange.telegram.bot.handler;

/**
 * 通用命令接口
 */
public interface CommandHandler {
    void handle(long chatId, String args);
}