# CodeGraph 同类工具调研报告

> 调研日期：2026-07-05
>
> 调研对象：https://github.com/colbymchenry/codegraph（⭐ 57,662）

---

## 一、CodeGraph 核心能力总结

**一句话**：预构建代码知识图谱（符号、调用边、依赖），通过 MCP Server 为 AI 编程 Agent 提供精确上下文，减少 58% 工具调用、22% 延迟，100% 本地运行。

**关键技术**：tree-sitter AST 解析 → 符号/调用图/依赖图 → 本地索引 → MCP 工具暴露

**暴露的 MCP 工具**：

| 工具 | 作用 |
|------|------|
| `codegraph_search` | 按名称搜索符号 |
| `codegraph_context` | 为任务构建相关代码上下文 |
| `codegraph_callers` | 查找某个函数被谁调用 |
| `codegraph_callees` | 查找某个函数调用了谁 |
| `codegraph_impact` | 分析修改某个符号的影响范围 |
| `codegraph_node` | 获取单个符号详情 |
| `codegraph_files` | 获取索引中的文件结构 |
| `codegraph_status` | 检查索引健康状态 |

**支持的 Agent**：Claude Code、Cursor、Codex CLI、OpenCode、Hermes Agent、Gemini CLI、Antigravity IDE、Kiro

**Benchmark（7 个开源仓库，中位数）**：58% 更少工具调用 · 22% 更快 · 文件读取降至近零

| 代码库 | 语言 | 文件数 | 工具调用减少 | 时间加速 | Token 减少 |
|--------|------|--------|-------------|---------|-----------|
| VS Code | TypeScript | ~10k | 81% | 11% | 64% |
| Excalidraw | TypeScript | ~640 | 40% | 27% | 25% |
| Django | Python | ~3k | 77% | 13% | 60% |
| Tokio | Rust | ~790 | 57% | 18% | 38% |
| OkHttp | Java | ~645 | 50% | 31% | 54% |
| Gin | Go | ~110 | 44% | 24% | 23% |
| Alamofire | Swift | ~110 | 58% | 33% | 64% |

---

## 二、同类工具详细对比

### 2.1 codebase-memory-mcp

| 项目 | 信息 |
|------|------|
| GitHub | `DeusData/codebase-memory-mcp` |
| Stars | ~5,400+ |
| 语言 | **纯 C（零依赖、单二进制）** |
| License | MIT |
| 语言支持 | **158 种语言** |

**与 CodeGraph 对比**：

| 维度 | CodeGraph | codebase-memory-mcp |
|------|-----------|---------------------|
| 实现语言 | TypeScript | 纯 C |
| 语言支持 | 多语言 | 158 种语言 |
| 索引速度 | 快 | **毫秒级（Linux 内核 2800 万行仅 3 分钟）** |
| 查询速度 | 快 | **亚毫秒级** |
| 图数据库 | 自建索引 | 持久化知识图谱 |
| MCP 工具 | 8 个 | 11+ 个 |
| Agent 支持 | 8 个 | 11 个 |

**独有特色**：
- Leiden 社区检测算法自动识别代码模块边界
- 跨仓库分析（CROSS_* 边类型链接多个已索引仓库）
- 死代码检测（找到无调用链的孤立函数）
- 支持 Cypher-like 图查询语言
- arXiv 论文：arXiv:2603.27277

**定位**：高性能代码图数据库引擎，适合超大仓库和复杂图查询场景。

---

### 2.2 Understand-Anything

| 项目 | 信息 |
|------|------|
| GitHub | `Egonex-AI/Understand-Anything` |
| Stars | ~6 万+ |
| 定位 | **给人看的交互式代码知识图谱** |

**与 CodeGraph 对比**：

| 维度 | CodeGraph | Understand-Anything |
|------|-----------|---------------------|
| 核心目标 | 给 Agent 查代码上下文 | 给人看项目结构 |
| 输出形态 | MCP 工具返回 JSON | 交互式 Dashboard + knowledge-graph.json |
| 交互方式 | 通过 AI 查询 | 拖拽探索、点击展开、搜索节点 |
| 视角切换 | ❌ | ✅ 初级/架构师两种视角 |
| 中文支持 | ❌ | ✅ `--language zh` |
| 安装方式 | CLI + MCP Server | Claude Code Plugin + CLI |

**独有特色**：
- 多智能体流水线分析项目（文件→函数→类→依赖）
- 生成 `.understand-anything/knowledge-graph.json`
- `/understand-dashboard` 交互式可视化
- 支持按架构层筛选节点

**定位**：新人接手项目、架构师做全局导览、理解业务流程。与 CodeGraph 互补使用效果最佳。

---

### 2.3 Graphify

| 项目 | 信息 |
|------|------|
| GitHub | `safishamsi/graphify` |
| 定位 | AI 编码助手的 Skill（轻量级） |

**与 CodeGraph 对比**：

| 维度 | CodeGraph | Graphify |
|------|-----------|----------|
| 形态 | CLI + MCP Server | AI Skill + CLI |
| 依赖 | 需要安装 CLI | 无额外依赖 |
| 查询方式 | MCP 工具调用 | CLI 命令 + Claude Code hook |
| 输出 | 结构化 JSON | GRAPH_REPORT.md |

**核心命令**：
- `graphify path "A" "B"` — 找两个节点之间的路径
- `graphify explain "NodeName"` — 解释单个节点的上下文

**独有特色**：
- 支持任意文件夹（代码、笔记、论文、图片、视频）
- 通过 CLAUDE.md 规则 + PreToolUse hook 注入图谱提示
- 不依赖 MCP 协议

**定位**：最轻量的方案，快速上手，不想装 MCP 时的替代选择。

---

### 2.4 CodeGraphContext

| 项目 | 信息 |
|------|------|
| GitHub | `CodeGraphContext/CodeGraphContext` |
| 定位 | MCP Server + CLI 双模式 |

**与 CodeGraph 对比**：

| 维度 | CodeGraph | CodeGraphContext |
|------|-----------|------------------|
| 索引存储 | 内存/本地文件 | **图数据库持久化** |
| 模式 | CLI + MCP | MCP Server + 独立 CLI |
| IDE 支持 | 8 个 Agent | Cursor、Claude、VS Code、Windsurf |

**独有特色**：
- 图数据库持久化（适合长期维护索引）
- 自然语言提问 + 终端直接查询双模式

**定位**：需要持久化图数据库的场景。

---

### 2.5 Aider Repo Map（内置方案）

| 项目 | 信息 |
|------|------|
| 来源 | Aider-AI/aider 内置模块 |
| 定位 | Aider 的内置仓库映射系统 |

**技术原理**：
1. tree-sitter 解析源文件 → 提取符号定义和引用
2. 构建依赖关系图
3. 图排序算法选出与当前任务最相关的代码片段
4. 压缩生成上下文 map

**特点**：
- 动态生成，按任务相关性排序
- 不污染主对话的 context window
- 与 Aider 深度绑定，不可独立使用

**定位**：已在用 Aider 的用户无需额外工具，内置 Repo Map 够用。

---

### 2.6 商业方案

| 工具 | 类型 | 核心特点 | 市场份额 |
|------|------|---------|---------|
| **Sourcegraph Cody** | 商业/自托管 | 全仓库代码搜索 + AI 对话，IDE 集成强 | ~10% |
| **Greptile** | 商业 SaaS | AI 代码审查，每月审核十亿行代码（YC W23） | — |
| **Cursor** | IDE（内置） | 自带项目理解能力，多模型支持 | ~20% |
| **GitHub Copilot** | 插件 | GPT-4/Codex 驱动，最大生态 | ~35% |

---

## 三、全景对比矩阵

| 工具 | 类型 | 语言 | Stars | MCP | 可视化 | 自动同步 | 适合谁用 |
|------|------|------|-------|-----|--------|---------|---------|
| **CodeGraph** | CLI + MCP | TypeScript | 57.6k | ✅ | ❌ | ✅ | AI Agent |
| **codebase-memory-mcp** | CLI + MCP | C | 5.4k | ✅ | ❌ | ✅ | AI Agent（大仓库） |
| **Understand-Anything** | Plugin + CLI | Python | 6 万+ | ❌ | ✅ Dashboard | ❌ | 人（新人/架构师） |
| **Graphify** | Skill + CLI | — | 增长中 | ❌ | ❌ | ❌ | 快速轻量场景 |
| **CodeGraphContext** | MCP + CLI | — | 新兴 | ✅ | ❌ | — | 持久化图数据库 |
| **Aider Repo Map** | 内置模块 | Python | — | ❌ | ❌ | — | Aider 用户 |
| **Sourcegraph Cody** | 商业 | — | — | ❌ | ✅ | ✅ | 企业团队 |

---

## 四、选型建议

| 需求场景 | 推荐工具 | 理由 |
|----------|---------|------|
| 给 AI Agent 减少 token 和工具调用 | **CodeGraph** | 最成熟、生态最广、8 个 Agent 支持 |
| 超大仓库 + 需要复杂图查询 | **codebase-memory-mcp** | C 实现、158 语言、毫秒级索引、Cypher 查询 |
| 给人看项目架构、新人上手 | **Understand-Anything** | 交互式 Dashboard、双视角、中文支持 |
| 轻量快速、不想装 MCP | **Graphify** | CLI + hook，零配置 |
| 需要持久化图数据库 | **CodeGraphContext** | 图数据库存储，长期维护 |
| 已在用 Aider | 内置 Repo Map | 无需额外工具 |
| 企业级、需要团队协作 | **Sourcegraph Cody** | 自托管、权限管理、IDE 集成 |

---

## 五、最佳实践

**推荐组合**：CodeGraph + Understand-Anything 双工具协同

- **CodeGraph** → 给 AI Agent 提供精确的代码上下文（谁调用了谁、修改影响范围）
- **Understand-Anything** → 给开发者提供可视化的全局架构视图

两者覆盖"Agent 查"和"人看"两个维度，互补而非竞争。

---

## 六、参考链接

- CodeGraph: https://github.com/colbymchenry/codegraph
- codebase-memory-mcp: https://github.com/DeusData/codebase-memory-mcp
- Understand-Anything: https://github.com/Egonex-AI/Understand-Anything
- Graphify: https://github.com/safishamsi/graphify
- CodeGraphContext: https://github.com/CodeGraphContext/CodeGraphContext
- Aider Repo Map: https://aider.org.cn/docs/repomap.html
