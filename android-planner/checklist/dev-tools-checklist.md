# 开发工具清单

> 日期：2026-07-05
> 项目：ToolBox Android 应用
> 用途：明确开发过程中需要的所有工具，区分"AI 工作区可执行"与"用户本机需要"

---

## 一、环境总览

| 类别 | 状态 | 说明 |
|---|---|---|
| JDK 17+ | ❌ 未安装 | Gradle 编译必须 |
| Android SDK | ❌ 未安装 | Android 编译必须 |
| Gradle 8.x | ❌ 未安装 | 构建系统 |
| Kotlin 2.0+ | ❌ 随 Gradle 自动下载 | 无需单独安装 |
| Git | ✅ 已安装 | 版本控制 |
| Python 3 | ✅ 已安装 | 辅助脚本 |
| Node.js | ✅ 已安装 | 辅助工具 |

> ⚠️ 当前服务器环境缺少 JDK 和 Android SDK，无法直接编译 Android 项目。
> 代码生成、文件组织、脚本编写可在服务器完成；编译和运行需要用户本机环境。

---

## 二、必备工具清单

### A. 构建环境（用户本机或 CI 服务器）

| 工具 | 版本 | 用途 | 安装方式 |
|---|---|---|---|
| **JDK** | 17 或 21 (LTS) | Kotlin 编译、Gradle 运行 | [Adoptium](https://adoptium.net/) / SDKMAN |
| **Android Studio** | Ladybug (2024.2+) | IDE、模拟器、调试 | [developer.android.com](https://developer.android.com/studio) |
| **Android SDK** | API 34 (target) | 编译目标 | Android Studio SDK Manager |
| **Android SDK** | API 26 (min) | 最低兼容 | Android Studio SDK Manager |
| **Gradle** | 8.7+ | 构建系统 | 项目内 gradle wrapper（自动下载） |
| **Kotlin Compiler** | 2.0+ | 编译 Kotlin | Gradle 自动管理 |

**Android SDK 组件（通过 SDK Manager 安装）：**

```
SDK Platforms:
  ✅ Android 14 (API 34)    — targetSdk
  ✅ Android 8.0 (API 26)   — minSdk

SDK Tools:
  ✅ Android SDK Build-Tools 34.0.0
  ✅ Android SDK Platform-Tools
  ✅ Android SDK Command-line Tools
  ✅ Android Emulator
  ✅ Android SDK Platform-Images (API 34, for emulator)
  ✅ Intel HAXM / Android Emulator Hypervisor Driver
```

### B. 版本控制

| 工具 | 版本 | 用途 | 状态 |
|---|---|---|---|
| **Git** | 2.43+ | 版本控制 | ✅ 已安装 |
| **GitHub CLI** (gh) | latest | PR 管理、Issue 追踪 | 可选 |
| **Git LFS** | latest | 大文件存储（多媒体附件） | 推荐 |

### C. 测试工具

| 工具 | 用途 | 集成方式 |
|---|---|---|
| **JUnit 5** | 单元测试 | Gradle 依赖 |
| **Mockk** | Kotlin mock 框架 | Gradle 依赖 |
| **Turbine** | Flow 测试 | Gradle 依赖 |
| **Robolectric** | Android 单元测试（无模拟器） | Gradle 依赖 |
| **Espresso** | UI 测试 | Gradle 依赖 |
| **Compose Test** | Compose UI 测试 | Gradle 依赖 |
| **Android Emulator** | 集成测试 | Android Studio |
| **真实设备** | 真机测试 | USB 调试 |

### D. 代码质量

| 工具 | 用途 | 集成方式 |
|---|---|---|
| **ktlint** | Kotlin 代码风格检查 | Gradle 插件 |
| **detekt** | 静态分析（复杂度/坏味道） | Gradle 插件 |
| **Android Lint** | Android 特定检查 | Gradle 内置 |
| **Dependency Analysis** | 依赖分析（未使用/重复依赖） | Gradle 插件 |

### E. 安全工具

| 工具 | 用途 | 说明 |
|---|---|---|
| **BouncyCastle** | Argon2id 实现 | Gradle 依赖 |
| **SQLCipher for Android** | 数据库加密 | Gradle 依赖 |
| **Android Keystore API** | 硬件密钥管理 | SDK 内置 |
| **OWASP Dependency-Check** | 依赖漏洞扫描 | Gradle 插件（可选） |

### F. 网络与 API 调试

| 工具 | 用途 | 说明 |
|---|---|---|
| **OkHttp Logging Interceptor** | HTTP 请求日志 | Gradle 依赖，仅 debug |
| **Charles Proxy / mitmproxy** | 网络抓包调试 | 独立工具 |
| **Postman / Insomnia** | AI API 接口测试 | 独立工具 |

### G. 设计与资源

| 工具 | 用途 | 说明 |
|---|---|---|
| **Figma / Sketch** | UI 设计稿 | 可选，也可直接 Compose 实现 |
| **Android Asset Studio** | 图标/启动图生成 | 在线工具 |
| **Material Theme Builder** | Material 3 主题生成 | [material-foundation](https://material-foundation.github.io/) |
| **App Icon Generator** | 多分辨率图标 | 在线工具 |

### H. CI/CD（可选，后期）

| 工具 | 用途 | 说明 |
|---|---|---|
| **GitHub Actions** | 自动构建/测试 | 免费额度够用 |
| **Fastlane** | 自动签名/发布 | Ruby 环境 |
| **Gradle Play Publisher** | 自动发布到 Play Store | Gradle 插件 |

### I. 数据库管理

| 工具 | 用途 | 说明 |
|---|---|---|
| **DB Browser for SQLite** | 查看/调试数据库 | 注意：SQLCipher 加密库需用支持的版本 |
| **Android Studio Database Inspector** | 运行时查看 Room 数据 | Android Studio 内置 |
| **Room Gradle Plugin** | Schema 导出（迁移验证） | Gradle 插件 |

---

## 三、Gradle 插件清单

```kotlin
// buildSrc/Dependencies.kt 或 libs.versions.toml

plugins {
    // 核心
    id("com.android.application") version "8.4.0"
    id("com.android.library") version "8.4.0"
    id("org.jetbrains.kotlin.android") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"

    // DI
    id("com.google.dagger.hilt.android") version "2.51"
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"  // Hilt 用 KSP

    // 导航
    id("androidx.navigation.safeargs.kotlin") version "2.8.0"

    // 代码质量
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"

    // 依赖分析
    id("com.autonomousapps.dependency-analysis") version "1.31.0"

    // Room Schema 导出
    id("androidx.room") version "2.6.1"

    // Glance (Widget, 后期)
    // id("androidx.glance") version "1.1.0"
}
```

---

## 四、核心依赖清单

```toml
# gradle/libs.versions.toml

[versions]
kotlin = "2.0.0"
compose-bom = "2024.06.00"
hilt = "2.51"
room = "2.6.1"
navigation = "2.8.0"
lifecycle = "2.8.3"
coroutines = "1.8.1"
retrofit = "2.11.0"
okhttp = "4.12.0"
serialization = "1.7.1"
datastore = "1.1.1"
camerax = "1.3.4"
sqlcipher = "4.5.6"
bouncycastle = "1.78.1"
glance = "1.1.1"

[libraries]
# Compose BOM
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }

# Lifecycle
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room + SQLCipher
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
sqlcipher = { group = "net.zetetic", name = "android-database-sqlcipher", version.ref = "sqlcipher" }

# Security
bouncycastle = { group = "org.bouncycastle", name = "bcprov-jdk18on", version.ref = "bouncycastle" }

# Network
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Serialization
serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

# DataStore
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Coroutines
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

# CameraX
camerax-core = { group = "androidx.camera", name = "camera-core", version.ref = "camerax" }
camerax-camera2 = { group = "androidx.camera", name = "camera-camera2", version.ref = "camerax" }
camerax-lifecycle = { group = "androidx.camera", name = "camera-lifecycle", version.ref = "camerax" }
camerax-view = { group = "androidx.camera", name : "camera-view", version.ref = "camerax" }

# Glance (Widget)
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version.ref = "glance" }

# Testing
junit5 = { group = "org.junit.jupiter", name = "junit-jupiter", version = "5.10.2" }
mockk = { group = "io.mockk", name = "mockk", version = "1.13.10" }
turbine = { group = "app.cash.turbine", name = "turbine", version = "1.1.0" }
robolectric = { group = "org.robolectric", name = "robolectric", version = "4.12.2" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
```

---

## 五、用户本机环境搭建步骤

```bash
# 1. 安装 JDK 17 (Adoptium)
# macOS:
brew install --cask temurin@17
# Linux:
sudo apt install openjdk-17-jdk
# Windows:
# 下载 https://adoptium.net/temurin/releases/?version=17

# 2. 配置 JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS
# 或
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  # Linux

# 3. 安装 Android Studio
# 下载: https://developer.android.com/studio

# 4. Android Studio 首次启动会自动安装:
#    - Android SDK (API 34)
#    - Build Tools
#    - Platform Tools
#    - Emulator

# 5. 配置 ANDROID_HOME
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
export ANDROID_HOME=$HOME/Android/Sdk           # Linux
# Windows: 默认 C:\Users\<user>\AppData\Local\Android\Sdk

# 6. 验证
java -version
adb version
```

---

## 六、AI 工作区能力边界

| 任务 | 能否在服务器完成 | 说明 |
|---|---|---|
| 生成项目结构 | ✅ | 创建所有 Gradle 配置和源码文件 |
| 编写业务代码 | ✅ | 所有 Kotlin 源码 |
| 编写测试代码 | ✅ | 单元测试、UI 测试代码 |
| 编译项目 | ❌ | 需要 JDK + Android SDK |
| 运行模拟器 | ❌ | 需要本机 + KVM/HAXM |
| 运行单元测试 | ⚠️ | 纯 JVM 测试可通过 Gradle，Android 测试需模拟器 |
| 生成资源文件 | ✅ | XML 布局、drawable、values 等 |
| 签名打包 | ❌ | 需要 keystore 文件 |

**推荐工作流：**
1. AI 在服务器生成全部代码文件
2. 用户通过 Git 拉取到本机
3. Android Studio 打开、编译、运行、调试
4. 反馈问题 → AI 修改代码 → Git push → 用户拉取

---

*清单版本：v1.0 | 状态：✅ 完成*
