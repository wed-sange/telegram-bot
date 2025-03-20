package com.sange.telegram.bot.util.properties;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Slf4j
public class ConfigUtils {
    private static IConfig globalConfig;
    private static IConfig extraConfig;

    private static Gson gson = new Gson();


    public static IConfig extraConfig() {
        if (extraConfig == null) {
            return key -> null;
        } else {
            return extraConfig;
        }
    }

    public static IConfig loadExtraConfig(String... paths) {
        extraConfig = PropertiesLoader.loadLive(paths);
        return extraConfig();
    }

    public static IConfig globalConfig() {
        if (globalConfig == null) {
            synchronized (ConfigUtils.class) {
                if (globalConfig == null) {
                    loadGlobalConfig();
                }
            }
        }
        return globalConfig;
    }

    private static void loadGlobalConfig() {
        IConfig propertiesConfig = loadProperties();
        IConfig yamlConfig = loadYaml();
        globalConfig = key -> {
            String val = propertiesConfig.get(key);
            if (val == null) {
                val = yamlConfig.get(key);
            }
            return val;
        };
    }

    /**
     * 配置 properties文件名称
     * @return
     */
    public static IConfig loadProperties() {
        List<String> propertiesFilePath = createPath( "application.properties", "example.properties");
        return PropertiesLoader.loadLive(propertiesFilePath.toArray(new String[0]));
    }

    /**
     * 配置文件扫描路径
     * @param fileNames
     * @return
     */
    private static List<String> createPath(String... fileNames) {
        List<String> pathList = new ArrayList<>();
        for (String fileName : fileNames) {
            pathList.add("config/" + fileName);
            pathList.add(fileName);
            pathList.add("classpath:/" + fileName);
            pathList.add("/WEB-INF/classes/" + fileName);
        }
        return pathList;
    }

    /**
     * 配置 yml文件名称
     * @return
     */
    private static IConfig loadYaml() {
        List<String> paths = createPath("application.yml");
        Map<String, Object> configs = new HashMap<>();
        for (String path : paths) {
            InputStream stream = null;
            if (path.startsWith("classpath:")) {
                String resource = path.replaceFirst(".*?:", "");
                stream = PropertiesLoader.class.getResourceAsStream(resource);
            } else {
                File file = new File(path);
                if (file.isFile()) {
                    try {
                        stream = new FileInputStream(path);
                    } catch (FileNotFoundException e) {
                    }
                }
            }

            if (stream != null) {
                Yaml yaml = new Yaml();
                try {
                    Map<String, Object> data = yaml.load(stream);
                    configs.putAll(data);
                } catch (Throwable e) {
                    log.error("read file failed, file = {}", path);
                } finally {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        return key -> {
            Object val = readYamlValue(configs, key);
            if (val != null) {
                return val.toString();
            } else {
                return null;
            }
        };
    }

    private static Object readYamlValue(Map<String, Object> data, String key) {
        int index = key.indexOf('.');
        if (index < 0) {
            return data.get(key);
        } else if (index == 0 || index == key.length() - 1) {
            return null;
        } else {
            String parentKey = key.substring(0, index);
            String childKey = key.substring(index + 1);
            Object val = data.get(parentKey);
            if (val != null && val instanceof Map) {
                return readYamlValue(((Map) val), childKey);
            } else {
                return null;
            }
        }
    }

    public static String get(String key) {
        String val = globalConfig().get(key);
        if (val != null) {
            return val;
        } else {
            return extraConfig().get(key);
        }
    }

    public static String get(String key, String defaultVal) {
        String val = get(key);
        return val != null ? val : defaultVal;
    }

    public static <T> T get(String key, Class<T> classOfT) {
        return gson.fromJson(get(key), classOfT);
    }

    public static <T> T get(String key, Class<T> classOfT, T defaultVal) {
        String val = get(key);
        if (StringUtils.isNumeric(val)) {
            return gson.fromJson(get(key), classOfT);
        }
        return defaultVal;
    }

    public static Integer getInt(String key) {
        String val = get(key);
        return StringUtils.isNumeric(val) ? Integer.valueOf(val) : null;
    }

    public static Integer getInt(String key, Integer defaultVal) {
        String val = get(key);
        return StringUtils.isNumeric(val) ? Integer.valueOf(val) : defaultVal;
    }

    public static Long getLong(String key) {
        String val = get(key);
        return StringUtils.isNumeric(val) ? Long.valueOf(val) : null;
    }

    public static Long getLong(String key, Long defaultVal) {
        String val = get(key);
        return StringUtils.isNumeric(val) ? Long.valueOf(val) : defaultVal;
    }
}
