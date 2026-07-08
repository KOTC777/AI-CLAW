# ToolBox 项目记忆

## 项目启动

- **日期**: 2026-07-05
- **用户需求**: Android 多功能工具箱，包含备忘录、日程表、任务打卡、密码本、灵感笔记
- **技术选型讨论**: 经过 v0.1-v0.5 五轮迭代，锁定安全方案
- **开发周期**: 2026-07-05 一天内完成全部 6 个 Phase

## 架构决策

1. **原生 vs 跨平台** → 原生 Kotlin，理由：40% 需求是系统级交互
2. **Clean MVVM vs 简化 MVVM** → 简化 MVVM（无 UseCase），理由：自用项目
3. **加密方案** → Argon2id + SQLCipher + 字段级加密，理由：安全最高
4. **AI 架构** → Provider 抽象 + CommandExecutor，AI 只返回命令不碰数据
5. **网页存储** → 单 HTML 内联，理由：轻便管理
6. **Widget** → Jetpack Glance，理由：官方推荐

## 开发进度

- Phase 1 ✅ 基础框架（10 core 模块 + app 壳）
- Phase 2 ✅ 备忘录 + 密码本（含分层加密 + 解锁门）
- Phase 3 ✅ 日程表 + 提醒引擎（5 级强度）
- Phase 4 ✅ 任务打卡（升级提醒 + 连续天数 + Widget）
- Phase 5 ✅ 灵感笔记 + AI（4 模板 + 涂鸦 + 网页快照 + AI 分类）
- Phase 6 ✅ Widget + 紧急销毁 + 同步接口 + 设置页

## 用户偏好

- 安全优先（选择安全方案而非轻量方案）
- 功能独立可升级
- 强制打卡可配置强度
- AI 处理结果需要记录和提示
- 密码本多层级设计

## 统计

- 129 文件
- 10,024 行 Kotlin
- 18 Gradle 模块
- 13 Room 实体
- 9 DAO
- 15 Compose Screen
- 6 ViewModel
- 10 Hilt Module
- 2 Widget
