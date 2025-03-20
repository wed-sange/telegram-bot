# telegram bot 接入

该项目主要以策略模式来处理机器人的各种功能，支持多机器人处理

## 文件目录
```html
bot
├── BotManager.java
├── TelegramBotMain.java
├── handler
│   ├── CommandHandler.java
│   ├── HandlerName.java
│   └── example
│       ├── ExampleHandlerService.java
│       └── command
│           ├── ExampleCommandContext.java
│           └── handler
│               ├── ExampleCommandHandler.java
│               └── StartCommandHandler.java
└── util
    ├── MarkdownUtil.java
    ├── MoneyUtil.java
    ├── TimeUtil.java
    ├── http
    │   ├── HttpClient.java
    │   ├── HttpClientFactory.java
    │   └── exception
    │       ├── HttpIOException.java
    │       └── HttpResponseException.java
    └── properties
        ├── ConfigUtils.java
        ├── GameType.java
        ├── IConfig.java
        └── PropertiesLoader.java

```

## 初始化

### 配置文件

```properties
# 需要代理则添加
#proxy.http.host=
#proxy.http.port=
#proxy.http.account=
#proxy.http.password=
```

#### 如果有新的 properties 配置文件，则在 createPath 中添加或删除 支持热加载

```java
/**
 * 配置 properties文件名称
 * @return
 */
public static IConfig loadProperties() {
    List<String> propertiesFilePath = createPath("application.properties", "example.properties");
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
```
### 机器人配置
#### 因为整个项目是以策略模式来处理的，建议不同的机器人逻辑单独配置，示例：[example.properties](src%2Fmain%2Fresources%2Fexample.properties)

## 多机器人
继承 [BotManager.java](src%2Fmain%2Fjava%2Fcom%2Fsange%2Ftelegram%2Fbot%2FBotManager.java) 类，示例：
```java
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
```
