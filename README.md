# 原生 Android 数独应用 (Sudoku.com 风格)

本项目是完全参照 Easybrain 知名产品 **Sudoku.com** 设计打造的高品质、简洁、流畅、优雅的原生 Android 数独游戏。

工程基于 **Kotlin + Jetpack Compose + Material 3** 构建，遵循 MVVM 架构与单向数据流 (UDF)。

---

## 🌟 核心特性一览

1. **五档难度阶梯与严格唯一解引擎**
   - 支持 **简单 (Easy)**、**中等 (Medium)**、**困难 (Hard)**、**专家 (Expert)**、**大师 (Master)** 5 种难度。
   - 内置高性能回溯与位运算（Bitmask）求解器，在后台协程（`Dispatchers.Default`）中严格校验双解（`countSolutions == 1`），保证每道题目**有且仅有唯一解**。

2. **每日挑战 (Daily Challenge) 与日历奖杯**
   - 采用日期哈希确定性伪随机种子，保证同一天题目全网玩家完全一致。
   - 交互式月度日历打卡界面，支持补刷当月历史题目，集齐当月挑战解锁金色月度奖杯。

3. **四级视觉高亮体系 (SudokuBoardCanvas)**
   - **选中格**：雅致天蓝高亮。
   - **关联区域 (Peers)**：同行、同列、同 3×3 宫浅色底纹辅助十字排查。
   - **全盘相同数字**：选中某个已有数字时，全盘相同数字同步高亮，且笔记中的对应小数字同步加粗突出。
   - **冲突与错误**：同行/列/宫冲突或填错时，珊瑚红柔和警示。

4. **区域完成波浪动效 (Completion Wave)**
   - 当某一行、某一列或某一个 3×3 宫全部正确填满时，沿该区域单元格触发顺滑的波浪闪烁动效。

5. **全功能辅助工具栏与智能笔记**
   - **撤销 (Undo)**：多步无损撤销，包含还原被自动清除的关联候选数。
   - **擦除 (Erase)**：快速擦除选中格数字或候选标记。
   - **笔记模式 (Notes)**：3×3 微型候选数网格，带醒目 ON/OFF 胶囊指示。
   - **一键自动笔记 (Auto Notes)**：一键快速计算并填满全盘所有合法候选数。
   - **智能逻辑提示 (Smart Hint)**：优先按人类思维分析“唯一余数法 (Naked Single)”与“行列宫摒除法 (Hidden Single)”，弹出推理说明并自动填入。

6. **硬件级触觉反馈与轻量即时合成音效**
   - 通过 `AudioTrack` 纯代码实时合成清脆柔和的正弦落子音、擦除音与通关和弦，无需外部庞大音频包。
   - 结合系统 `Vibrator` 提供落子微震、波浪动效韵律震动与错误双击震动（可在设置中随时开关）。

7. **断点持久化与详细历史统计**
   - 退出应用或切至后台自动暂停计时，任何时候重新进入均可“继续上次对局”。
   - 详尽统计各难度的总局数、获胜场次、胜率、最快用时、平均用时与连胜纪录。

---

## 📁 工程目录结构

```
sudoku-android/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/antigravity/sudoku/
│       │   │   ├── MainActivity.kt                 # 导航与生命周期管理
│       │   │   ├── SudokuApp.kt                    # Application 入口
│       │   │   ├── domain/
│       │   │   │   ├── model/SudokuModels.kt       # 单元格、难度、历史步、设置数据模型
│       │   │   │   └── engine/
│       │   │   │       ├── SudokuSolver.kt         # 位运算回溯求解器与唯一解校验
│       │   │   │       ├── SudokuGenerator.kt      # 终盘生成与挖空算法
│       │   │   │       └── SudokuHintAnalyzer.kt   # 人类解题技巧逻辑提示分析器
│       │   │   ├── data/
│       │   │   │   ├── local/GameStorage.kt        # 断点存档、统计与每日挑战持久化
│       │   │   │   └── feedback/HapticAndSoundManager.kt # 振动与即时合成音频
│       │   │   └── ui/
│       │   │       ├── theme/                      # Material 3 调色盘与字体排版
│       │   │       ├── components/                 # Canvas棋盘、五键工具栏、1-9智能数字键盘
│       │   │       ├── home/HomeScreen.kt          # 首页、继续游戏卡片、五档难度选择
│       │   │       ├── game/                       # 核心对局界面、ViewModel与结算弹窗
│       │   │       ├── daily/DailyChallengeScreen.kt# 每日挑战日历与月度奖杯
│       │   │       ├── stats/StatsScreen.kt        # 5 档难度历史数据统计面板
│       │   │       └── settings/SettingsScreen.kt  # 声音、振动、高亮与规则偏好设置
│       │   └── res/                                # 图标与主题资源
│       └── test/
│           └── java/com/antigravity/sudoku/
│               └── SudokuEngineTest.kt             # 算法与唯一解单元测试
├── gradle/
│   └── libs.versions.toml                          # 统一依赖版本库 (Compose BOM / Kotlin 2.0)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 如何在 Android Studio 中打开与运行

1. 打开 **Android Studio** (推荐 Hedgehog / Iguana / Ladybug 或更高版本)。
2. 选择 **Open**，并指定本项目根目录 `/home/antigravity/sudoku-android`。
3. Android Studio 将自动同步 Gradle 依赖并下载 Android SDK (compileSdk 35, minSdk 26)。
4. 连接任意 Android 真机或启动 AVD 模拟器，点击 **Run 'app'** 即可编译安装并体验。
