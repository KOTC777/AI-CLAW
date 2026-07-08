# Android 应用架构设计文档

> 项目代号：ToolBox
> 设计原则：轻便 · 强大 · 可靠

## 文档索引

| 版本 | 文件 | 内容 | 状态 |
|---|---|---|---|
| v0.1 | [v0.1-需求分析与技术选型.md](v0.1-需求分析与技术选型.md) | 需求拆解、初步技术选型、Q1-Q8 | ✅ 完成 |
| v0.2 | [v0.2-需求深化与追问.md](v0.2-需求深化与追问.md) | 需求深化分析、Q9-Q13 | ✅ 完成 |
| v0.3 | [v0.3-最终架构方案.md](v0.3-最终架构方案.md) | 最终技术栈、模块设计、数据库、核心子系统、开发计划 | ✅ 完成 |
| v0.4 | [v0.4-技术栈优劣分析与替代方案.md](v0.4-技术栈优劣分析与替代方案.md) | 各层技术选型优劣分析、替代方案对比、调整建议 | ✅ 完成 |
| v0.5 | [v0.5-最终技术栈锁定.md](v0.5-最终技术栈锁定.md) | 🔒 技术栈锁定（安全方案）、密钥派生、加密架构、兼容性策略 | ✅ 锁定 |

## 最终技术栈 🔒

- **语言**: Kotlin 2.0+
- **UI**: Jetpack Compose + Material 3
- **架构**: 简化 MVVM（无 UseCase）+ 模块化单体
- **数据库**: Room + SQLCipher（全库加密）
- **安全**: Android Keystore + Argon2id + AES-256-GCM + 三层加密
- **提醒**: AlarmManager + WorkManager + ForegroundService（5 级强度）
- **AI**: 多 Provider 抽象层（DeepSeek / OpenAI / Claude / 自定义）
- **网页**: WebView 单 HTML 快照
- **DI**: Hilt
- **序列化**: kotlinx.serialization
- **异步**: Coroutines + Flow

## 下一步

技术栈已锁定，进入 Phase 1 开发：项目脚手架搭建。
