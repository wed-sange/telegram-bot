package com.sange.telegram.bot.util.properties;

import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置文件加载器，支持热加载
 */
@SuppressWarnings("SameParameterValue")
@Slf4j
public class PropertiesLoader {
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> {
        /* 备忘：此处不能使用 Virtual Thread：
            ScheduledThreadPoolExecutor 需使用尚未启动的线程（state = NEW） */
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("properties-loader-daemon-executor");
        return thread;
    });

    public static IConfig load(String... paths) {
        Map<String, String> config = load_files(paths);
        return config::get;
    }

    /*
     * 热加载需求：
     *      1. 热加载时机：每整分钟（行为更易预测）
     *      2. 配置有更新时，输出更新内容（方便检查）
     *      3. 即使没有被用到，也要正常更新
     */
    public static IConfig loadLive(String... paths) {
        AtomicReference<LinkedHashMap<String, String>> holder = new AtomicReference<>(load_files(paths));
        executor.scheduleAtFixedRate(() -> {
            LinkedHashMap<String, String> new_config = load_files(paths);
            print_updates(holder.get(), new_config);
            holder.set(new_config);
        }, 1, 1, TimeUnit.MINUTES);
        return key -> holder.get().get(key);
    }

    /* ------------------------- properties ------------------------- */

    // 加载 properties 文件中的配置
    // 如同一配置多次出现，优先使用前面的值
    @SneakyThrows
    private static LinkedHashMap<String, String> load_files(String[] paths) {
        LinkedHashMap<String, String> cache = new LinkedHashMap<>();
        if (paths != null) {
            for (String path : paths) {
                load_file(path, cache, false);
            }
        }
        return cache;
    }

    private static void load_file(String path, Map<String, String> store, boolean override_old_value) throws Exception {
        Pair<String, InputStream> input = get_input_stream(path);
        if (input.getValue() == null)
            return;
        try (InputStream in = input.getValue()) {
            new Properties() {
                public Object put(Object key, Object value) {
                    if (override_old_value) {
                        store.put((String) key, (String) value);
                    } else {
                        store.putIfAbsent((String) key, (String) value);
                    }
                    return null;
                }
            }.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private static Pair<String, InputStream> get_input_stream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resource = path.replaceFirst(".*?:", "");
            InputStream stream = PropertiesLoader.class.getResourceAsStream(resource);
            return Pair.of(path, stream);
        } else {
            File file = new File(path);
            InputStream stream = file.isFile() ? Files.newInputStream(file.toPath()) : null;
            return Pair.of(file.getAbsolutePath(), stream);
        }
    }

    private static final Pattern CONFIG_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+=[a-zA-Z0-9._\\s-]+$");

    // 更新配置
    public static synchronized void updateConfig(String path, String key, String value) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("配置文件不存在: " + path);
            }

            // 读取已有配置
            Properties properties = new Properties();
            try (InputStream input = new FileInputStream(file)) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }

            // 格式校验
            String keyValue = key + "=" + value;
            Matcher matcher = CONFIG_PATTERN.matcher(keyValue);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid format: " + keyValue);
            }

            properties.setProperty(key, value);
            try (OutputStream out = Files.newOutputStream(Paths.get(path))) {
                properties.store(out, "Updated by PropertiesLoader");
                log.info("Config updated: {} = {}", key, value);
            } catch (IOException e) {
                log.error("Failed to update config: {}", e.getMessage(), e);
            }
            log.info("配置更新成功: {} = {}", key, value);
        } catch (IOException e) {
            log.error("更新配置失败: {}", e.getMessage(), e);
            throw new IllegalArgumentException("更新配置失败: " + e.getMessage());
        }
    }


    /* ------------------------- print ------------------------- */

    private static void print_updates(Map<String, String> old, Map<String, String> latest) {
        Preconditions.checkNotNull(old);
        Preconditions.checkNotNull(latest);
        latest.forEach((k, v) -> {
            String old_v = old.get(k);
            if (old_v == null) {
                log.info("config added: {} = {}", k, v);
            } else if (!old_v.equals(v)) {
                log.info("config updated: {} = {} -> {}", k, old_v, v);
            }
        });
        old.forEach((k, v) -> {
            if (!latest.containsKey(k))
                log.info("config removed: {} = {}", k, v);
        });
    }

}
