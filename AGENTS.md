# Depression-TianTian (DTT)

> Minecraft 1.20.1 Fabric 模组 —— 对 [Depression](https://www.mcmod.cn/class/8769.html) 模组的扩展与增强。

## 面向玩家

DTT 的核心体验围绕"日记"展开。原版 Depression 模组的日记内容较少、文案感重，DTT 对此进行了"报复性"扩充：

- **增强日记生成系统**：根据玩家当日行为数据（击杀怪物、驯服动物、宠物死亡、死亡次数等）和心理健康状态（健康 / 轻度 / 中度 / 重度抑郁 / 躁狂相）动态生成个性化日记，包含天气、话题、感受等多维度内容，且支持变体避免重复。
- **提示消息系统**：在特定情境下向玩家发送沉浸式提示——身处黑暗、淋雨、唱片机旁、重置重生点、PTSD 形成/消散/缓解等，都配有精心编写的文案。
- **躯体化症状强化**：新增"厌食"状态效果，抑郁严重时隐藏饥饿值栏，让心理问题的躯体表现更加直观。
- **游戏性保护机制**：双脚离地时自动阻止闭眼；战斗状态下可配置阻止闭眼/打盹；紧张性木僵可配置为不锁键盘（改为给予负面效果）。
- **更多恢复途径**：附近方块、宠物、唱片机的情绪恢复机制均可配置（间隔、模式），击杀怪物也可恢复情绪。
- **高度可配置**：心理医生商品价格/库存/解锁等级、PTSD 触发距离/恢复速率、日记温柔模式、精神特质选择界面开关等，均通过 JSON 配置文件调整。

**前置依赖**：Fabric API、Architectury API、Depression（>=0.2.2）。

## 面向开发者

DTT 同时是一个库模组，为 Depression 模组提供了更易用的 API 层：

### 事件系统 (`api/event/`)

通过 Architectury Event 封装了 Depression 的核心生命周期事件，可被其他模组监听：

| 事件                                               | 说明             |
|--------------------------------------------------|----------------|
| `MentalIllnessEvent.MENTAL_HEALTH_CHANGED_EVENT` | 患病状态变化         |
| `MentalIllnessEvent.MOOD_POLARITY_CHANGED_EVENT` | 双相情感障碍极性切换     |
| `PTSDEvent.PTSD_ONSET_EVENT`                     | PTSD 发作（可取消）   |
| `PTSDEvent.PTSD_FORM_EVENT`                      | PTSD 形成（可取消）   |
| `PTSDEvent.PTSD_DISPERSE_EVENT`                  | PTSD 消散        |
| `PTSDEvent.PTSD_REMISSION_EVENT`                 | PTSD 缓解        |
| `PTSDEvent.PTSD_LEVEL_CHANGED_EVENT`             | PTSD 等级变化      |
| `PTSDEvent.PTSD_PHOTISM_EVENT`                   | PTSD 幻视触发（可取消） |
| `SymptomEvent.ANOREXIA_TRIGGERED`                | 厌食触发（可取消）      |
| `SymptomEvent.INTERRUPT_EATING`                  | 打断进食（可取消）      |
| `SymptomEvent.CLOSE_EYES_EVENT`                  | 闭眼（可取消）        |
| `SymptomEvent.SIMPLE_CLOSE_EYES_EVENT`           | 简化闭眼（不区分原因，可取消） |
| `SymptomEvent.OPEN_EYES_EVENT`                   | 睁眼             |
| `SymptomEvent.MENTAL_FATIGUE_EVENT`              | 精神疲劳（可取消）      |
| `SymptomEvent.INSOMNIA_EVENT`                    | 失眠             |
| `EmotionEvent.ENTER_COMBAT_STATE_EVENT`          | 进入战斗状态         |
| `EmotionEvent.EXIT_COMBAT_STATE_EVENT`           | 退出战斗状态         |
| `EmotionEvent.ENTER_ZEN_STATE_EVENT`             | 进入禅定状态         |
| `EmotionEvent.EXIT_ZEN_STATE_EVENT`              | 退出禅定状态         |
| `EmotionEvent.ZEN_STATE_ADDITION_EVENT`          | 进入 BossBar 通知范围（可取消） |
| `EmotionEvent.ZEN_STATE_REMOVAL_EVENT`           | 离开 BossBar 通知范围（可取消） |

此外，`api/event/PTSDContext` 为 PTSD 事件的上下文包装（携带 PTSD 名称、源实体/伤害信息等）；`api/internal/` 下的 duck 接口（`AnorexiaController`、`BlockBreakMentalHealCooldownController`、`EyesStatusFlagController`）由 Mixin 注入，用于内部状态读写，仅供 DTT 自身使用。

### 包装类 (`api/wrapper/`)

将 Depression 原版的魔法数值封装为语义化枚举：

- `MentalHealthStatus` — 精神健康状态（健康/轻度/中度/重度抑郁/躁狂相），支持从 `MentalStatus`、`ClientMentalStatus`、数值等多种来源转换。
- `MentalIllnessStatus` — 患病情况（健康/轻度抑郁/中度抑郁/重度抑郁障碍/双相情感障碍）。
- `PTSDLevel` — PTSD 等级（0-4），含潜伏期、有症状、极端等语义判断。
- `Severity` — 严重程度枚举，统一描述各维度的严重等级。
- `EmotionLevel` — 情绪水平。

### 辅助类 (`api/helper/`)

- `MentalStatusHelper` — 查询精神状态、是否处于紧张性木僵等。
- `PTSDHelper` — PTSD 相关查询。
- `EmotionHelper` — 情绪值相关操作。

### 日记系统 (`diary/`)

`DiaryUpdatePacketMixin` 在 `enhanced_diary_generator`（默认 false）开启时接管 `DiaryUpdatePacket.sendToPlayer`，调用根包 `DiaryContentProducer` 生成并发送日记翻译文本。

- `DiaryContentProducer`（`diary/` 根包）— 组装日记段落（开头→天气→话题→通用→话题→结尾），支持变体查找和温柔模式。
- `DiaryParagraph`（`diary/` 根包）— 段落枚举（WEATHER/NO_DIARY/MANIC_GENERAL/OPENING/CLOSING/CURED/WORSENED/GENERAL/TOPIC）。
- `diary/topic/v2/` — 注册表化话题（`Topic`/`EssentialTopic`/`StatTopic`，具体实现于 `diary/topic/v2/topics/`），经 `DTTRegistries` 的 essential/stat 注册表注册；`ContextAttribute`（权重、感受符合度、极端负面标记、来源话题）、`TopicManager`（历史话题去重与持久化）、`TopicProducer`（从注册表提取并计算话题属性）。
- `diary/topic/TopicWeightCalculator` — 基于今日/昨日/EMA 三层统计计算权重（对数归一化与 Sigmod 平滑）。
- `diary/feeling/v2/` — `FeelingProducer` 根据话题 `ContextAttribute` 符合度与心理健康状态计算当日感受关键词。
- `diary/dailystat/v2/` — `DailyStatKey`（注册表化统计键）、`DailyStat`、`DailyStatManager`（今日/昨日/EMA 三层，持久化到 NBT）。
- `diary/wrapper/BakedTranslationKey` — 预烘焙 translation key 包装（日记本地化拼接辅助）。

### 提示消息系统 (`hint/`)

基于计时器的消息调度框架（v2）：

- `HintMessage` — 抽象基类，定义冷却/积累计时器（`timer/` 下的 `Timer`、`RandomizedTimer`、`TimeUnit`）、自动发送开关、全局条件、translation key 与变体数。
- `SimpleHintMessage` — 自动发送型消息（如 `DarknessMessage`、`PetMessage`、`JukeboxMessage`）。
- `SubscriptionHintMessage` — 订阅触发型消息（如 `AnorexiaMessage`、PTSD 系列），通过 `trigger()` 触发。
- `HintMessageInstance` — 每玩家、每消息的运行时实例（携带计时器状态，可序列化到 NBT）。
- `HintMessageManager` — 管理每玩家的消息实例、待发送任务队列与轮询计时器，控制发送节奏（含队列阻塞）。
- `HintMessageSender` — 触发/发送/立即发送消息并处理重要性与队列路由。
- `HintMessageTask` — 待发送队列中的消息任务（含生命周期与过期）。
- 具体实现（`hint/messages/`）：`DarknessMessage`（黑暗）、`InRainMessage`（淋雨）、`PetMessage`（宠物附近）、`JukeboxMessage`（唱片机附近）、`ResetSpawnPointMessage`（重置重生点）、`AnorexiaMessage`/`AnorexiaWorsenMessage`（厌食及加重）、`LatentPTSDMessage`/`FormPTSDMessage`/`ExtremePTSDMessage`/`DispersePTSDMessage`/`RemissionPTSDMessage`（PTSD 各阶段）。

### 技术要点

- **Mixin**：大量使用 Mixin（含 MixinSquared）注入 Depression 原版逻辑，分为 `event/`（事件提取）、`modification/`（行为修改）、`client/`（客户端）子包，另有根包下通用 Mixin 与 `accessor/`（如 `BoredomMapAccessor`、`PTSDMapAccessor`）。
- **网络同步**：通过 Architectury `NetworkChannel` 同步服务端配置到客户端（`ServerConfigPacket`、`OpenEyesEventPacket`）。
- **配置系统**：服务端/客户端分离的 JSON 配置（`ServerConfig`/`ClientConfig`），首次运行自动生成默认配置文件。
- **注册表**：`DTTRegistries` 定义了话题（essential/stat）、统计键（boolean/number）、提示消息等自定义注册表。
- **初始化入口**：`init/` 旗下的 `DTTNetwork`、`DTTStat`、`DTTDailyStat`、`DTTTopic`、`DTTHintMessage`、`DTTStatusEffect`、`DTTListener`（Architectury/DTT 内部/Fabric 事件）与 `DTTCommand` 调试命令。
- **其他模块**：`config/`（配置类）、`network/`（网络包）、`registry/`（注册表）、`client/`（客户端入口与键位）、`util/`（工具类如 `MathUtil`、`StringUtil`）、`effect/`（`AnorexiaEffect`）、`mock/`（`MockMentalStatus`，模拟精神状态用于测试），`test_diary/` 存放日记生成测试产物。
- **构建**：Fabric Loom（`net.fabricmc.fabric-loom-remap`）+ Gradle，Java 17，许可证 GPL-3.0。


<!-- open-mem-context -->
## Project Activity (auto-generated by open-mem)

### ./
| ID | Type | Title | Date |
|----|------|-------|------|
| 8c840493-2492-4918-94be-210aa9e82291 | 🔵 discovery | Conventional commit changelog pattern across 5 versions | 2026-06-08 |
| e1b77e69-39c4-4f95-bed0-9fcc4dcfb579 | 🔄 refactor | Hint v2 system migrated to root, v1 deleted | 2026-06-08 |
| 09ac33a3-3b27-464d-800f-944a8b6da62f | 🔵 discovery | Git CRLF line ending warnings on checkout | 2026-06-08 |
| fb6d06f5-6710-4791-b42d-34d8739dcdaa | 🔵 discovery | DTT version history and evolution (v0.1.1-v0.1.5) | 2026-06-08 |
| 8f0f8ab3-128d-417a-afe2-23baa1fe1737 | 🔄 refactor | Hint system v2 refactoring stats - net -554 lines | 2026-06-08 |
| 2502af91-e0fa-4b61-b8fb-fd8fb927a89c | 🔄 refactor | Hint message system v2 refactoring | 2026-06-08 |

**Key concepts:** conventional-commits, changelog-pattern, breaking-change-notation, breaking-change, hint-system-v2, package-consolidation, PTSD-system, mixin-accessor, project-memory, git-line-endings

💡 *Use `mem-find` to search full details. Use `mem-create` to save important decisions.*
<!-- /open-mem-context -->
