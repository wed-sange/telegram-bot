package com.sange.telegram.bot.util.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@FunctionalInterface
public interface IConfig {

    String get(String key);

    /* ------------------------- getter ------------------------- */

    Logger log = LoggerFactory.getLogger(IConfig.class);

    default String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    default String getTrimmed(String key) { return trimToEmpty(get(key)); }

    default int getInt(String key, int defaultValue) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            log.error("illegal config int {}={}", key, value, e);
            return defaultValue;
        }
    }
    default long getLong(String key, long defaultValue) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            log.error("illegal config long {}={}", key, value, e);
            return defaultValue;
        }
    }
    default double getDouble(String key, double defaultValue) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            log.error("illegal config double {}={}", key, value, e);
            return defaultValue;
        }
    }
    default boolean getBool(String key, boolean defaultValue) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return defaultValue;
        // 布尔型配置应配置为 0 或 1
        switch (value) {
            case "1":
                return true;
            case "0":
                return false;
            default:
                log.error("illegal config bool {}={}", key, value, new IllegalStateException("illegal bool config"));
                return defaultValue;
        }
    }
    default int getInt(String key) { return getInt(key, 0); }
    default long getLong(String key) { return getLong(key, 0); }
    default double getDouble(String key) { return getDouble(key, 0); }
    default boolean getBool(String key) { return getBool(key, false); }

    default List<Integer> getIntList( String key) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return emptyList();
        try {
            // 正常情况下，应使用半角逗号、分号作为分隔符
            return Arrays.stream(value.split("[,;]"))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("illegal config int list {}={}", key, value, e);
            return Arrays.stream(value.split("[^-\\d]+"))
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
    }
    default List<Long> getLongList(String key) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return emptyList();
        try {
            // 正常情况下，应使用半角逗号、分号作为分隔符
            return Arrays.stream(value.split("[,;]"))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("illegal config long list {}={}", key, value, e);
            return Arrays.stream(value.split("[^-\\d]+"))
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }
    default List<Double> getDoubleList(String key) {
        String value = getTrimmed(key);
        if (isEmpty(value)) return emptyList();
        try {
            // 正常情况下，应使用半角逗号、分号作为分隔符
            return Arrays.stream(value.split("[,;]"))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("illegal config double list {}={}", key, value, e);
            return Arrays.stream(value.split("[^-.\\d]+"))
                    .filter(s -> !s.isEmpty())
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        }
    }

    /* ------------------------- misc ------------------------- */

    default IConfig usePrefix(String prefix) {
        return key -> get(prefix + key);
    }

}
