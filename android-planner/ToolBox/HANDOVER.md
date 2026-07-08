# ToolBox 项目交接文档

> 最后更新：2026-07-05 23:45 CST
> 编写目的：让任何 AI 或开发者能无缝接手本项目
> 项目状态：✅ 全部 6 个 Phase 开发完成

---

## 一、项目概况

**项目名称**：ToolBox（Android 多功能工具箱应用）
**设计原则**：轻便 · 强大 · 可靠
**目标用户**：个人自用
**当前阶段**：全部开发完成，待本机编译测试

### 核心功能（5 个独立模块）

| 模块 | 功能 | 状态 |
|---|---|---|
| 备忘录 | CRUD + 富文本 + 搜索 + 置顶 + 滑动删除 + 桌面小组件 | ✅ 完成 |
| 日程表 | 月历视图 + 事件管理 + 重复规则 + 5 级强度系统闹钟提醒 | ✅ 完成 |
| 任务打卡 | 周期任务 + 强制提醒（5 级自动升级）+ 打卡证明 + 统计 + 桌面小组件 | ✅ 完成 |
| 密码本 | 三层加密（Keystore + SQLCipher + 字段级 AES-256-GCM）+ 分组 + 分层展示 + 导入导出 .vault | ✅ 完成 |
| 灵感笔记 | 4 种模板 + 涂鸦 + 网页快照 + AI 自动归类 + 标签系统 | ✅ 完成 |

---

## 二、技术栈（已锁定 🔒）

```
语言:     Kotlin 2.0+
UI:       Jetpack Compose + Material 3 + Jetpack Glance (Widget)
架构:     简化 MVVM（无 UseCase 层）+ 模块化单体
DI:       Hilt
数据库:   Room + SQLCipher（全库加密）+ 版本化 Migration
安全:     Android Keystore + Argon2id + AES-256-GCM（三层加密）
网络:     Retrofit + OkHttp + kotlinx.serialization
AI:       多 Provider 抽象层（DeepSeek / OpenAI 兼容 / Claude / 自定义）
异步:     Coroutines + Flow
提醒:     AlarmManager + WorkManager + ForegroundService（5 级强度引擎）
网页:     WebView 单 HTML 快照
Widget:   Jetpack Glance
```

---

## 三、项目结构

```
ToolBox/                                    # 129 文件 | 10,024 行 Kotlin | 18 模块
├── app/                                    # 壳工程 (7 Kotlin + 8 XML)
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml             # 权限 + 组件注册 + Widget
│       ├── java/com/toolbox/app/
│       │   ├── ToolBoxApp.kt              # Application
│       │   ├── MainActivity.kt            # 主 Activity + Setup/Main 两态
│       │   ├── navigation/AppNavHost.kt   # 完整导航 (15 个 Screen)
│       │   ├── settings/SettingsScreen.kt # 设置页
│       │   └── ui/SetupScreen.kt         # 首次启动引导
│       └── res/
│           ├── layout/                    # Widget 布局
│           ├── values/                    # strings + themes
│           └── xml/                       # Widget 元数据
│
├── buildSrc/                               # 构建辅助
├── gradle/libs.versions.toml               # 统一依赖版本
│
├── core/                                   # 核心共享层 (10 模块)
│   ├── core-common/                        # 工具类 + 扩展 + 常量 + Resource
│   ├── core-database/                      # 13 实体 + 9 DAO + SQLCipher + Migration
│   ├── core-datastore/                     # DataStore 偏好管理
│   ├── core-security/                      # Keystore + Argon2 + CryptoEngine + SecurityManager
│   │                                       # + LockManager + VaultExporter + EmergencyDestroyer
│   │                                       # + SyncAdapter (预留)
│   ├── core-network/                       # Retrofit + OkHttp + 拦截器
│   ├── core-ai/                            # AiProvider + DeepSeek + Registry + CommandExecutor
│   ├── core-notification/                  # 4 个通知渠道
│   ├── core-alarm/                         # AlarmScheduler + ReminderEngine (5 级)
│   ├── core-websnapshot/                   # WebView 单 HTML 快照
│   └── core-ui/                            # Material 3 主题 + 背景 + 共享组件
│
└── feature/                                # 功能模块 (5 个)
    ├── feature-memo/                       # Repository + ViewModel + ListScreen + EditScreen + Widget
    ├── feature-schedule/                   # Repository + ViewModel + CalendarScreen + EventEditScreen
    ├── feature-checkin/                    # Repository + ViewModel + CheckinScreen + CheckinDialog + Widget
    ├── feature-password/                   # Repository (分层加密) + ViewModel + ListScreen + EditScreen
    └── feature-inspiration/                # Repository + ViewModel + ListScreen + EditScreen
                                            # + TemplatePickerScreen + DrawingCanvas + WebCaptureScreen
                                            # + AiNoteProcessor
```

---

## 四、架构详解

### 4.1 分层架构

```
UI 层 (Compose Screen + ViewModel)
  ↓ 观察 StateFlow
ViewModel 层
  ↓ 调用 suspend 函数
Repository 层
  ↓ 操作 DAO / 加密 / 网络 / AI
数据层 (Room + SQLCipher / DataStore / Retrofit)
```

**注意**：没有 UseCase 层。ViewModel 直接调 Repository。

### 4.2 数据库设计

**13 个实体**：

| 域 | 实体 | 关键设计 |
|---|---|---|
| 备忘录 | `MemoEntity` | 软删除（deleted_at） |
| 日程 | `ScheduleEventEntity` | repeat_rule / reminder_config 存 JSON |
| 打卡 | `CheckinTaskEntity` + `CheckinRecordEntity` | 级联删除；intensity_config 存 JSON |
| 密码 | `PasswordGroupEntity` + `PasswordEntryEntity` | 字段级 AES-256-GCM 加密（password/hints/ext 各有 encrypted + iv + tag） |
| 灵感 | `InspirationNoteEntity` + `InspirationAttachmentEntity` + `InspirationTemplateEntity` | 附件级联删除 |
| AI | `AiProviderConfigEntity` + `AiProcessingLogEntity` + `AiScheduledTaskEntity` | API Key 字段级加密 |
| 安全 | `AppLockConfigEntity` | app/feature 两种锁类型 |

### 4.3 安全架构（三层加密）

```
Layer 1: Android Keystore (硬件级)
  └── 保护 master_key，密钥不可导出

Layer 2: SQLCipher (数据库级)
  └── AES-256 加密整个 .db 文件
  └── passphrase 由 master_key 加密后存在 DataStore

Layer 3: 字段级加密 (应用层)
  └── 密码本的 password/hints/ext 字段独立 AES-256-GCM 加密
  └── 字段密钥由 master_key 通过 HKDF 派生

密钥派生链:
  用户主密码 → Argon2id(64MB, 3轮, 1并行) → master_key
    → HKDF(master_key, "field_encryption") → 字段加密密钥
    → AES-GCM 加密 DB passphrase → 存入 DataStore
```

### 4.4 AI 集成架构

```
用户输入笔记
  → AiNoteProcessor.processNote(noteId)
  → 读取笔记 + 模板 system prompt
  → 发送到 AI Provider（DeepSeek 等）
  → AI 返回结构化 JSON {"category":"技术","tags":["Android"]}
  → 更新笔记分类和标签
  → 记录处理日志到 ai_processing_log 表
```

### 4.5 提醒引擎（5 级强度）

```
Level 1: 通知栏提醒
Level 2: 每 N 分钟重复通知（不可滑动关闭）
Level 3: 高优先级通知（全屏意图）
Level 4: 锁屏通知 + 铃声 + 振动
Level 5: 前台服务持续提醒

支持时间触发自动升级:
  截止前 2h  → L1
  截止前 30m → L2
  截止前 10m → L3
  截止时间到  → L4
  截止后 30m → L5

打卡验证通过 → 取消所有待触发提醒
```

### 4.6 Widget 架构

```
Jetpack Glance:
  MemoWidget      → 显示最近备忘录列表
  CheckinWidget   → 显示今日任务 + 连续天数 + 打卡状态

更新频率: 30 分钟
支持: 调整大小、深色模式
```

---

## 五、编码规范

### 5.1 命名约定

```
包名:     com.toolbox.{core|feature}.{module}.{layer}
类名:     PascalCase (MemoEntity, MemoDao, MemoRepository)
函数:     camelCase (observeAll, softDelete)
常量:     SCREAMING_SNAKE_CASE (ARGON2_MEMORY_KB)
资源:     snake_case (ic_launcher, app_name)
```

### 5.2 文件组织

```
每个 feature 模块:
  ui/         → Screen + ViewModel
  data/       → Repository + Domain Model
  di/         → Hilt Module
  widget/     → Glance Widget (如有)

每个 core 模块:
  {功能}/     → 按功能分组
  di/         → Hilt Module
```

### 5.3 依赖注入

- `@Module` + `@InstallIn(SingletonComponent::class)`
- ViewModel: `@HiltViewModel` + `@Inject constructor`
- Repository 通过构造函数注入 DAO
- 单例: `@Singleton`

### 5.4 数据流

```
DAO → Flow<Entity>
  → Repository → Flow<DomainModel>
  → ViewModel → stateIn() → StateFlow
  → Screen → collectAsState()
```

### 5.5 加密约定

- 密码本字段: `SecurityManager.encryptField()` / `decryptField()`
- API Key: 同上
- 加密操作: `Dispatchers.IO`
- 敏感数据用完: `ByteArray.fill(0)`

---

## 六、开发进度

### ✅ Phase 1: 基础框架
- Gradle 多模块脚手架 + libs.versions.toml
- 10 个 core 模块完整实现
- 所有 Hilt DI Module
- 主题系统 + 应用锁框架

### ✅ Phase 2: 备忘录 + 密码本
- MemoRepository + MemoViewModel + MemoListScreen + MemoEditScreen
- PasswordRepository (分层加密) + PasswordViewModel + PasswordListScreen (解锁门) + PasswordEditScreen
- SetupScreen (首次启动主密码设置)

### ✅ Phase 3: 日程表 + 提醒引擎
- ScheduleRepository + ScheduleViewModel + CalendarScreen + EventEditScreen
- ReminderEngine (5 级强度 + 时间触发升级)

### ✅ Phase 4: 任务打卡
- CheckinRepository + CheckinViewModel + CheckinScreen + CheckinDialog
- 自动调度升级提醒 + 连续天数统计

### ✅ Phase 5: 灵感笔记 + AI
- InspirationRepository + InspirationViewModel + NoteListScreen + NoteEditScreen
- TemplatePickerScreen (4 种内置模板)
- DrawingCanvas (涂鸦) + WebCaptureScreen (网页快照)
- AiNoteProcessor (AI 自动归类 + CommandExecutor)

### ✅ Phase 6: 打磨 + 扩展
- MemoWidget + CheckinWidget (桌面小组件)
- EmergencyDestroyer (紧急销毁)
- SyncAdapter (同步接口预留)
- SettingsScreen (设置页面)
- DatabaseMigrations (迁移策略)
- AndroidManifest 更新

---

## 七、如何继续开发

### 步骤 1：拉取到本机

```bash
# 将 android-planner/ToolBox/ 目录拷贝到本机
```

### 步骤 2：环境准备

```bash
# JDK 17
brew install --cask temurin@17  # macOS
sudo apt install openjdk-17-jdk  # Linux

# Android Studio (自带 SDK)
# https://developer.android.com/studio
```

### 步骤 3：打开项目

1. Android Studio → Open → 选择 `ToolBox/` 目录
2. 等待 Gradle Sync（首次约 5-10 分钟）
3. 连接设备或启动模拟器
4. Run → 运行 app 模块

### 步骤 4：预期编译问题

可能需要修复的问题：

1. **Hilt 依赖注入**：`DatabaseModule` 中 `AppDatabase` 需要动态提供（带 passphrase）
2. **Widget XML 布局**：可能需要调整 Glance 版本兼容
3. **SQLCipher NDK**：首次编译可能需要安装 NDK
4. **Compose Compiler 版本**：确保与 Kotlin 2.0.0 匹配

---

## 八、关键设计决策记录

| 决策 | 选择 | 原因 | 文档 |
|---|---|---|---|
| 原生 vs 跨平台 | Kotlin 原生 | 40% 需求是系统级交互 | v0.1 |
| 架构模式 | 简化 MVVM | 自用项目不需要 UseCase | v0.4 |
| 加密方案 | Argon2id + SQLCipher + 字段级 | 三层防护 | v0.5 |
| DI 框架 | Hilt | 编译期安全 | v0.4 |
| 序列化 | kotlinx.serialization | KMP 预留 | v0.4 |
| 网页存储 | 单 HTML 内联 | 轻便管理 | v0.2 |
| AI 架构 | Provider + CommandExecutor | AI 只下命令不碰数据 | v0.3 |
| Widget | Jetpack Glance | 官方推荐 | v0.3 |

---

## 九、文件索引

```
android-planner/
├── docs/                              # 架构设计文档 (v0.1-v0.5)
│   ├── README.md
│   ├── v0.1-需求分析与技术选型.md
│   ├── v0.2-需求深化与追问.md
│   ├── v0.3-最终架构方案.md
│   ├── v0.4-技术栈优劣分析与替代方案.md
│   └── v0.5-最终技术栈锁定.md
├── checklist/                         # 工具清单 + 安装追踪
│   ├── dev-tools-checklist.md
│   └── tool-install-tracker.md
└── ToolBox/                           # 项目源码 (129 文件)
    ├── AGENTS.md                      # AI 接手指南
    ├── HANDOVER.md                    # 本文件
    ├── MEMORY.md                      # 项目决策记忆
    ├── README.md                      # 项目说明
    └── ... (详见第三节项目结构)
```

---

## 十、注意事项

1. **SQLCipher NDK**：可能需要在 Android Studio 安装 NDK
2. **BouncyCastle 体积**：约 +2MB APK
3. **Android 12+ 闹钟权限**：需用户授权 `SCHEDULE_EXACT_ALARM`
4. **Android 13+ 通知权限**：需运行时申请 `POST_NOTIFICATIONS`
5. **Android 14+ 前台服务**：需声明 `foregroundServiceType="specialUse"`
6. **Compose Compiler**：与 Kotlin 版本强绑定
7. **Hilt AppDatabase**：需要运行时注入 passphrase，不能直接 `@Provides`
8. **Widget 更新**：Glance Widget 更新有延迟，非实时

---

*交接文档版本：v2.0 | 状态：✅ 全部开发完成，待编译测试*
