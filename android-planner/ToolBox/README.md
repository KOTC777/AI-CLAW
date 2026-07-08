# ToolBox Android 应用

> 轻便 · 强大 · 可靠

## 项目状态

✅ 全部 6 个 Phase 开发完成，待本机编译测试。

## 统计

| 维度 | 数量 |
|---|---|
| 文件总数 | 129 |
| Kotlin 代码 | 10,024 行 |
| Gradle 模块 | 18 |
| Room 实体 | 13 |
| Compose Screen | 15 |
| Widget | 2 |

## 功能模块

| 模块 | 功能 |
|---|---|
| 📝 备忘录 | CRUD + 搜索 + 置顶 + 滑动删除 + 桌面小组件 |
| 📅 日程表 | 月历视图 + 事件管理 + 重复规则 + 5 级提醒 |
| ✅ 任务打卡 | 周期任务 + 强制提醒 + 打卡证明 + 统计 + 桌面小组件 |
| 🔐 密码本 | 三层加密 + 分组 + 分层展示 + 导入导出 |
| ✨ 灵感笔记 | 4 种模板 + 涂鸦 + 网页快照 + AI 自动归类 |

## 技术栈

- **语言**: Kotlin 2.0+
- **UI**: Jetpack Compose + Material 3 + Glance
- **架构**: 简化 MVVM + 模块化单体
- **数据库**: Room + SQLCipher（全库加密）
- **安全**: Android Keystore + Argon2id + AES-256-GCM（三层加密）
- **AI**: 多 Provider 抽象层（DeepSeek / OpenAI 兼容）
- **提醒**: AlarmManager + WorkManager + ForegroundService（5 级强度）

## 快速开始

```bash
# 1. 安装 JDK 17
brew install --cask temurin@17  # macOS

# 2. 安装 Android Studio
# https://developer.android.com/studio

# 3. 打开项目
# Android Studio → Open → 选择 ToolBox/ 目录

# 4. 运行
# 连接设备或启动模拟器 → Run
```

## 文档

- `HANDOVER.md` — 完整交接文档
- `AGENTS.md` — AI 接手指南
- `MEMORY.md` — 项目决策记忆
- `android-planner/docs/` — 架构设计文档 (v0.1-v0.5)
