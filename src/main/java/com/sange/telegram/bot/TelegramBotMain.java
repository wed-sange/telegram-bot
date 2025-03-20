package com.sange.telegram.bot;

import com.sange.telegram.bot.handler.example.ExampleHandlerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramBotMain {
    public static void main(String[] args) {

        log.info("启动测试机器人成功！");
        ExampleHandlerService.init();
    }
}