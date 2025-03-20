package com.sange.telegram.bot.util;

public class MarkdownUtil {
    /**
     * 转义 MarkdownV2 特殊字符
     */
    public static String escapeMarkdownV2(String text) {
        if (text == null) {
            return "";
        }
        // 需要转义的字符: _ * [ ] ( ) ~ ` > # + - = | { } . !
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}
