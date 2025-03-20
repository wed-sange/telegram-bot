package com.sange.telegram.bot.util.properties;


public enum GameType {

    G80_新视讯(80, "G80_新视讯"), // SX
    G81_视讯经典百家乐(81, "G81_视讯经典百家乐"), // SXJDBJL
    G82_视讯极速百家乐(82, "G82_视讯极速百家乐"), // SXJSBJL
    G83_视讯共咪百家乐(83, "G83_视讯共咪百家乐"), // SXGMBJL
    G84_视讯竞咪百家乐(84, "G84_视讯竞咪百家乐"), // SXJMBJL
    G85_视讯斗牛(85, "G85_视讯斗牛"), // SXDN
    G86_视讯炸金花(86, "G86_视讯炸金花"), // SXZJH
    G87_视讯牛牛(87, "G87_视讯牛牛"), // SXNN
    G88_视讯龙虎(88, "G88_视讯龙虎"), // SXLH
    G89_视讯骰宝(89, "G89_视讯骰宝"), // SXSB
    G90_视讯轮盘(90, "G90_视讯轮盘"), // SXLP
    G91_视讯色碟(91, "G91_视讯色碟"), // SXSD
    G92_视讯保险百家乐(92, "G92_视讯保险百家乐"), // SXBXBJL
    G93_视讯包桌百家乐(93, "G93_视讯包桌百家乐"), // SXBZBJL
    ;

    private int code;
    private String name;

    GameType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static GameType getByCode(int code) {
        for (GameType gameType : GameType.values()) {
            if (gameType.getCode() == code) {
                return gameType;
            }
        }
        return null;
    }
}
