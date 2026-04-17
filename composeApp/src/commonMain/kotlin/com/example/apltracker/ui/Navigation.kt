package com.example.apltracker.ui

enum class Screen(val title: String, val needsPlayer: Boolean) {
    Home("Apex 战绩助手", false),
    PlayerStats("玩家数据 (Bridge)", true),
    MatchHistory("对战记录", true),
    ServerStatus("服务器状态", false),
    MapRotation("地图轮换", false),
    Store("商店信息", false),
    OriginUID("获取用户UID", true),
}
