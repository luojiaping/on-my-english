# On My English

[![Android CI](https://github.com/luojiaping/on-my-english/actions/workflows/ci.yml/badge.svg)](https://github.com/luojiaping/on-my-english/actions/workflows/ci.yml)

原生 Android 背单词应用骨架，使用 Kotlin、Jetpack Compose 和离线优先的数据架构。当前重点是 OpenAI 兼容 AI 接口与图片识词导入；学习和统计页面暂为可运行占位界面。

## 当前能力

- 四栏应用壳：学习、词库、统计、设置
- 内置词库：四级 / 六级 / 考研（数据源 ECDICT，MIT 协议，首启自动导入）
- 胶囊卡片词书画廊与词书详情页（词表搜索、掌握进度、封面位预留）
- OpenAI 兼容供应商配置：Base URL、API Key、对话模型、视觉模型、温度
- API 连通性测试
- 相册选择或系统相机拍照
- 图片降采样与 JPEG 压缩后发送到多模态视觉模型
- 结构化输出三级降级：`json_schema`、`json_object`、普通文本 JSON
- AI 候选词预览、选择、编辑和批量入库
- Room 本地词库、词书、释义、例句、标签、复习状态、日志、导入暂存和 FTS4 搜索结构
- SM-2 复习调度器及单元测试；接口已为后续 FSRS 实现预留
- DataStore 设置持久化；API Key 使用 Android Keystore AES-GCM 加密
- GitHub Actions 自动构建、Lint、单元测试和 APK artifact
- Release 开启 R8/资源压缩，并在 CI 强制校验 APK Signature Scheme v3

## 技术基线

| 项目 | 版本 |
|---|---|
| Android Gradle Plugin | 9.3.2，built-in Kotlin |
| Gradle | 9.7.1 |
| Kotlin / KSP | 2.3.21 / 2.3.11 |
| SDK | compile 37 / target 37 / min 29 |
| Compose BOM | 2026.08.00 |
| Hilt / Room / Ktor | 2.60.1 / 2.8.4 / 3.5.2 |

需要支持 AGP 9.3 的稳定版 Android Studio、JDK 17 或更高版本，以及 Android SDK Platform 37。

本地正式签名密钥位于 ignored 的 `keystore/on-my-english-release.p12`，对应配置模板为 `keystore.properties.example`。私钥和密码不在 Git 中。没有本地配置时，Gradle 会使用 debug key 以便开发者仍可构建；CI 使用每次运行临时生成的 release key，仅用于构建验证。

## 运行

1. 使用 Android Studio 打开仓库，等待 Gradle Sync 完成。
2. 选择 API 29 或更高版本的设备或模拟器。
3. 运行 `app` 配置。
4. 在“设置”中填写兼容接口，再到“词库”选择图片或拍照。

也可以从命令行验证：

```bash
./gradlew :app:assembleDebug :app:lintDebug :app:testDebugUnitTest \
  :core:ai:testDebugUnitTest :core:domain:test
```

CI 成功后可在对应 GitHub Actions run 中下载 `on-my-english-debug` APK。

## OpenAI 兼容接口

Base URL 应指向 API 根路径，例如：

```text
https://api.openai.com/v1
https://openrouter.ai/api/v1
http://127.0.0.1:11434/v1
```

客户端请求 `${baseUrl}/chat/completions`。视觉模型必须支持 OpenAI 风格的 `image_url` 内容。无密钥本地服务可以使用 HTTP；为避免凭证泄漏，应用拒绝保存或测试携带 API Key 的 HTTP 配置。

兼容服务对结构化输出支持不同，因此识图依次尝试：

1. `response_format.type=json_schema`
2. `response_format.type=json_object`
3. 不设置 `response_format`，从普通文本中提取首个平衡 JSON

## 项目结构

```text
app/                  应用入口、主导航、Hilt 根节点
build-logic/          AGP 9 convention plugins
core/model/           纯 Kotlin 领域模型
core/common/          Result、错误和协程调度限定符
core/domain/          Repository 契约、UseCase、SRS
core/database/        Room、DAO、schema
core/datastore/       DataStore、Keystore 加密
core/network/         Ktor/OkHttp 客户端
core/ai/              OpenAI 兼容协议、视觉识词、JSON 解析
core/data/            Repository 实现与实体映射
core/designsystem/    主题、色彩、排版、间距令牌
core/ui/              跨 feature 的共享 UI
feature/study/        学习页面
feature/wordbook/     词库和 AI 图片导入
feature/stats/        统计页面
feature/settings/     AI 设置
```

详细依赖规则、数据流和扩展点见 [架构说明](docs/architecture.md)。

## 安全说明

- API Key 不写入 Room、日志、`BuildConfig` 或仓库。
- DataStore 仅保存 AES-GCM 密文；密钥由不可导出的 Android Keystore 管理。
- `AiProviderSettings.toString()` 永远输出 `<redacted>`。
- 图片在本地压缩后直接发送给用户配置的供应商。应用没有中转服务器。
- 不要把真实凭证写入 Gradle、CI secret 以外的位置或截图。

## 数据库迁移

Room schema 保存在 `core/database/schemas/`。修改 Entity 时必须：

1. 增加数据库版本。
2. 提供显式 Migration 或经过评估的 AutoMigration。
3. 提交新 schema JSON。
4. 增加迁移测试后再合并。

当前 version 2（decks 表新增 category / badge / coverUri）。

## 内置词库数据

`app/src/main/assets/decks/{cet4,cet6,kaoyan}.json.gz` 由 `scripts/ecdict_export.py`
从 [ECDICT](https://github.com/skywind3000/ECDICT)（MIT）导出，共约 14,000 词，
按当代语料库词频排序。重新生成：

```bash
python3 scripts/ecdict_export.py /path/to/ecdict.csv
```
