# Android Library 转 KMP Library 实战文档（JdcrLogCommon）

## 1. 目标与结论

本次改造目标是把 `jdcrlog` 从纯 Android Library 转成 Kotlin Multiplatform Library（KMP），并满足：

- Android 端逻辑保持不变；
- iOS / Web(JS) / Desktop(JVM) 端先提供空实现（占位，保证可编译）；
- 项目可完成 `:jdcrlog:assemble` 构建。

当前结果：已完成改造并构建成功。

---

## 2. 本次遇到的核心问题与处理

## 2.1 `androidTarget` DSL 无法识别

现象：`build.gradle.kts` 报 `Unresolved reference: androidTarget`。

原因：项目 Kotlin 版本为 `1.7.20`，使用了新版 DSL 写法。

处理：改为 Kotlin 1.7.20 兼容写法：

- 使用 `kotlin { android { ... } }`；
- 并将 `kotlinOptions.jvmTarget = "11"` 放在 `compilations.all` 中。

## 2.2 Kotlin/Native 依赖下载失败（`kotlin-native-prebuilt-macos-aarch64:1.7.20`）

现象：配置 iOS target 后，提示无法解析 Kotlin/Native prebuilt。

根因：需要允许 Kotlin 插件动态注入用于下载 Native 编译器的仓库（包括 ivy 机制），且要配置 Native 相关下载源。

处理：

1. `settings.gradle.kts` 中 `repositoriesMode` 改为 `PREFER_PROJECT`；
2. 在 `dependencyResolutionManagement.repositories` 中补充：
   - `maven("https://download.jetbrains.com/kotlin/native/builds")`

## 2.3 Gradle Wrapper 下载超时

现象：`services.gradle.org` 下载 `gradle-8.5-bin.zip` 超时。

处理：

1. `gradle-wrapper.properties` 调大 `networkTimeout`：`10000 -> 120000`；
2. `distributionUrl` 切换到可达镜像：
   - `https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip`

---

## 3. 结构改造步骤（通用模板）

如果你要把一个普通 Android Library 改成 KMP，可按下面流程执行：

1. **改插件**
   - 从 `com.android.library + org.jetbrains.kotlin.android`
   - 改为 `com.android.library + org.jetbrains.kotlin.multiplatform`

2. **改 sourceSets 结构**
   - 新建：
     - `src/commonMain/kotlin`
     - `src/androidMain/kotlin`
     - `src/iosMain/kotlin`（或拆 `iosX64Main / iosArm64Main / iosSimulatorArm64Main`）
   - Android 代码迁移到 `androidMain`；
   - 公共接口和可跨平台代码迁移到 `commonMain`。

3. **抽象 expect/actual**
   - `commonMain` 定义 `expect`；
   - `androidMain` 保留原逻辑并提供 `actual`；
   - 其他平台（如 iOS）先用空 `actual` 占位。

4. **补充 iOS targets**
   - `iosX64()`
   - `iosArm64()`
   - `iosSimulatorArm64()`

5. **修仓库与下载源**
   - 允许项目级仓库（`PREFER_PROJECT`）；
   - 增加 JetBrains Kotlin/Native 下载源；
   - 确保 Gradle Wrapper 下载地址在当前网络环境可达。

6. **验证构建**
   - 先跑 `./gradlew :module:assemble`
   - 再看 iOS metadata / native task 是否可正常完成。

---

## 4. 本次实际改动清单（JdcrLogCommon）

## 4.1 构建脚本与仓库配置

- 修改：`settings.gradle.kts`
  - `repositoriesMode` -> `PREFER_PROJECT`
  - 增加 `maven("https://download.jetbrains.com/kotlin/native/builds")`

- 修改：`gradle/wrapper/gradle-wrapper.properties`
  - `distributionUrl` 切换到腾讯镜像
  - `networkTimeout` 从 `10000` 改为 `120000`

- 修改：`jdcrlog/build.gradle.kts`
  - 切换为 Kotlin Multiplatform 配置
  - 配置 Android + iOS + Web(JS) + Desktop targets
  - 配置 `sourceSets`（`commonMain/androidMain/iosMain/jsMain/desktopMain`）

## 4.2 目录结构迁移

- 删除（从 `src/main` 迁出）：
  - `jdcrlog/src/main/AndroidManifest.xml`
  - `jdcrlog/src/main/java/...` 下原有代码

- 新增并迁移到：
  - `jdcrlog/src/androidMain/...`（Android 逻辑）
  - `jdcrlog/src/commonMain/...`（公共 API/接口）
  - `jdcrlog/src/iosMain/...`（iOS 空实现）
  - `jdcrlog/src/jsMain/...`（Web(JS) 空实现）
  - `jdcrlog/src/desktopMain/...`（Desktop(JVM) 空实现）

## 4.3 关键代码抽象

- `commonMain` 中新增 `expect`：
  - `JdcrLogBase`
  - `JdcrLog`
- `androidMain` 中使用 `actual` 接回原有实现（逻辑不改）；
- `iosMain/jsMain/desktopMain` 中提供空 `actual` 实现（仅用于编译通过）。

---

## 5. 会产生的影响（请关注）

## 5.1 正向影响

- `jdcrlog` 现在可作为 KMP 依赖被 `commonMain` 引用；
- Android 项目仍可直接使用 Android 产物；
- 后续逐步补全 iOS/Web/Desktop 真实实现有了明确入口。

## 5.2 风险与兼容提示

- 当前 iOS/Web/Desktop 是空实现：调用日志 API 不会输出真实日志；
- Kotlin `1.7.20` + AGP `8.1.0` 存在兼容告警（当前可构建，但建议后续升级）；
- 首次构建会下载 Kotlin/Native 工具链，耗时较长是正常现象；
- `sourcesJar` 目前有重复路径 warning，不影响产物，但后续建议清理测试 sourceSet 打包策略。

---

## 6. 当前验证结果

已执行并通过：

- `./gradlew :jdcrlog:assemble`

结果：`BUILD SUCCESSFUL`。

---

## 7. 后续建议（可选）

1. 将 Kotlin 升级到 `1.9.x` 以上，减少 KMP/AGP 兼容告警；
2. 视需要在 `iosMain/jsMain/desktopMain` 补真实日志实现（如 `println` / `NSLog`）；
3. 规范 `sourcesJar` 打包范围，消除 duplicate path warning；
4. 如需要对外发布，补充 `publishing` 产物命名与坐标策略（含 KMP metadata + Android AAR）。

---

## 8. 下一次自己改库时，直接照做的步骤

1. 在 `build.gradle.kts` 中切到 `org.jetbrains.kotlin.multiplatform`。
2. 添加 targets：`android`、`jvm("desktop")`、`js(IR)`、`iosX64/iosArm64/iosSimulatorArm64`。
3. 新建并迁移目录：
   - `src/commonMain/kotlin`
   - `src/androidMain/kotlin`
   - `src/desktopMain/kotlin`
   - `src/jsMain/kotlin`
   - `src/iosMain/kotlin`
4. 把公共 API 变成 `expect`，Android 原实现改 `actual`（不改业务逻辑）。
5. 在 `desktopMain/jsMain/iosMain` 先写空 `actual` 占位。
6. 在 `settings.gradle.kts` 配仓库策略和 Native 下载源：
   - `repositoriesMode = PREFER_PROJECT`
   - `maven("https://download.jetbrains.com/kotlin/native/builds")`
7. 如果网络慢，调整 `gradle-wrapper.properties`：
   - 增大 `networkTimeout`
   - `distributionUrl` 换可达镜像。
8. 执行 `./gradlew :你的模块:assemble` 验证。

---

## 9. 文档是否足够独立使用

结论：够用。  
如果按本文档操作，已经能独立完成“Android Library -> KMP Library（含 Android/iOS/Web/Desktop）并先用空实现打通编译”的完整流程。  
后续如果你要再做“发布到 Maven/JitPack 的细化策略”或“各平台真实实现替换空实现”，再按第 7 节建议逐步推进即可。

---

## 10. 发版前检查清单（重点保证老 Android 项目无感升级）

每次发布新版本前，按顺序检查以下项：

1. **坐标不变**
   - `groupId/artifactId` 保持与历史版本一致；
   - 避免无意改名导致消费端坐标失效。

2. **公开 API 不变**
   - 对外类名、方法名、参数、返回值保持兼容；
   - 如必须改动，按语义化版本升级（如从 `1.x` 到 `2.x`）。

3. **Android 逻辑不回归**
   - `androidMain` 里的原逻辑未被改坏；
   - 日志初始化、开关行为、文件写入路径等关键路径做一次冒烟验证。

4. **KMP 构建通过**
   - 执行：`./gradlew :jdcrlog:assemble`
   - 确认 Android/iOS/js/desktop 相关任务可正常完成（或预期跳过）。

5. **Android 消费项目回归**
   - 用一个“历史 Android 业务项目”升级到新版本做回归；
   - 至少跑：`assembleDebug` + 应用启动 + 一条核心日志链路。

6. **依赖解析结果正确**
   - 在 Android 消费项目确认选择的是 Android 变体（AAR）；
   - 避免异常选到 metadata 导致编译/运行问题。

7. **包体影响确认**
   - 对比升级前后 APK/AAB 大小；
   - 正常情况下 Android 包体应基本不变（或只有极小波动）。

8. **发布仓库可达性**
   - 确认发布仓库（JitPack/Maven 私服）可正常拉取新版本；
   - 首次拉取成功后，再通知业务方升级。

9. **版本与变更说明**
   - 发布说明中写清：
     - 这是 KMP 化版本；
     - Android 端行为是否有变化；
     - 非 Android 端当前是否为空实现。

10. **回滚预案**
   - 保留上一个稳定版本号；
   - 若发现兼容问题可快速回退依赖版本。
