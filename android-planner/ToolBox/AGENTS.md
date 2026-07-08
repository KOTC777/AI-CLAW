# AGENTS.md — AI 接手指南

> 如果你是 AI 助手，正在接手这个项目，请阅读此文件。

## 你是谁

你现在负责的是 **ToolBox** — 一个 Android 多功能工具箱应用。全部 6 个 Phase 已开发完成。

## 第一步：读这些文件

按顺序阅读：

1. `HANDOVER.md` — 完整交接文档
2. `gradle/libs.versions.toml` — 所有依赖版本
3. `settings.gradle.kts` — 模块结构
4. `core/core-security/src/main/java/com/toolbox/core/security/crypto/SecurityManager.kt` — 安全核心
5. `core/core-alarm/src/main/java/com/toolbox/core/alarm/ReminderEngine.kt` — 提醒引擎

## 当前状态

- ✅ Phase 1-6 全部完成
- ⏳ 待本机编译测试，修复可能的编译错误
- 129 文件 | 10,024 行 Kotlin | 18 模块

## 下一个要做的事

在本机编译运行，预期需要修复：

1. `DatabaseModule.kt` — AppDatabase 需要运行时注入 passphrase（不能直接 @Provides）
2. Widget XML — 可能需要调整 Glance 版本兼容
3. SQLCipher — 可能需要安装 NDK
4. Compose Compiler — 确保与 Kotlin 2.0.0 匹配

## 编码规范

- **架构**：简化 MVVM，ViewModel 直接调 Repository
- **DI**：Hilt，`@HiltViewModel` + `@Inject constructor`
- **数据流**：DAO → Flow<Entity> → Repository → Flow<Model> → ViewModel → StateFlow → Screen
- **加密**：敏感字段用 `SecurityManager.encryptField()` / `decryptField()`
- **异步**：Coroutines + Flow
- **UI**：纯 Compose，不使用 XML（Widget 用 Glance）

## 重要约束

1. 所有文件写在项目目录内
2. 依赖版本统一在 `gradle/libs.versions.toml` 管理
3. 新功能先建 feature 模块，不要往 core 里塞业务逻辑
4. 密码本加密字段必须用 ByteArray + IV + Tag 三元组
5. AI 命令必须通过 CommandExecutor 执行
