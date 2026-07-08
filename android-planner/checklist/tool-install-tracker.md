# 工具安装追踪

> 记录开发过程中需要安装的工具，每项标注状态

## 构建环境

| 工具 | 版本 | 状态 | 安装命令/链接 | 备注 |
|---|---|---|---|---|
| JDK 17 | 17 LTS | ❌ 待安装 | `brew install --cask temurin@17` (macOS) / `sudo apt install openjdk-17-jdk` (Linux) | Gradle 编译必须 |
| Android Studio | 2024.2+ | ❌ 待安装 | https://developer.android.com/studio | IDE + SDK + 模拟器 |
| Android SDK (API 34) | 34 | ❌ 待安装 | Android Studio SDK Manager 自动安装 | targetSdk |
| Android SDK (API 26) | 26 | ❌ 待安装 | Android Studio SDK Manager 自动安装 | minSdk |
| Android Build Tools | 34.0.0 | ❌ 待安装 | Android Studio SDK Manager 自动安装 | 编译工具 |
| Android Emulator | latest | ❌ 待安装 | Android Studio SDK Manager 自动安装 | 模拟器测试 |

## Gradle 插件（项目内自动下载，无需手动安装）

| 插件 | 版本 | 状态 | 备注 |
|---|---|---|---|
| AGP (Android Gradle Plugin) | 8.4.0 | ✅ 项目内管理 | build.gradle.kts |
| Kotlin | 2.0.0 | ✅ 项目内管理 | build.gradle.kts |
| Hilt | 2.51 | ✅ 项目内管理 | build.gradle.kts |
| KSP | 2.0.0-1.0.21 | ✅ 项目内管理 | build.gradle.kts |
| Navigation SafeArgs | 2.8.0 | ✅ 项目内管理 | build.gradle.kts |
| ktlint | 12.1.0 | ✅ 项目内管理 | build.gradle.kts |
| detekt | 1.23.6 | ✅ 项目内管理 | build.gradle.kts |

## 依赖库（Gradle 自动下载，无需手动安装）

| 库 | 版本 | 状态 | 用途 |
|---|---|---|---|
| Compose BOM | 2024.06.00 | ✅ 自动 | UI 框架 |
| Room | 2.6.1 | ✅ 自动 | 数据库 ORM |
| SQLCipher | 4.5.6 | ✅ 自动 | 数据库加密 |
| BouncyCastle | 1.78.1 | ✅ 自动 | Argon2id |
| Retrofit | 2.11.0 | ✅ 自动 | 网络请求 |
| OkHttp | 4.12.0 | ✅ 自动 | HTTP 客户端 |
| kotlinx.serialization | 1.7.1 | ✅ 自动 | 序列化 |
| CameraX | 1.3.4 | ✅ 自动 | 相机 |
| Glance | 1.1.1 | ✅ 自动 | Widget (后期) |

## 运行时工具

| 工具 | 用途 | 状态 | 备注 |
|---|---|---|---|
| ADB | 调试、安装 APK | ❌ 随 Android SDK 安装 | Platform Tools |
| Git LFS | 大文件存储 | ❌ 待安装 | `brew install git-lfs` / `sudo apt install git-lfs` |

---

*最后更新：2026-07-05*
