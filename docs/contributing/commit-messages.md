# Commit 信息规范

本文规定提交信息的写法,与仓库近期的提交保持一致。人工提交和 Agent 提交都适用。

## 1. 格式

```
<type>(<scope>): <标题>

<正文>
```

- 第一行是标题,必填。正文可选,和标题之间空一行。
- `(<scope>)` 只在改动横跨多个模块、找不到合适 scope 时省略,例如 `build: 升级 mediamp 到 0.5.3`。
- 标题末尾的 ` (#1234)` 由 GitHub squash 合并时自动加上,本地提交不要自己写。

## 2. type

只用下表的 type。

| type | 用于 | 例子 |
|---|---|---|
| `feat` | 新功能;用户可见的行为或界面变化,包括移除入口 | `feat(settings): 移除 QQ 群入口` |
| `fix` | 修复缺陷 | `fix(media): 合集种子按 scene 命名里的 S02E02 选对剧集文件` |
| `perf` | 性能优化,行为不变 | `perf(media-selector): 过滤资源列表时条目名只做一次特殊字符处理` |
| `refactor` | 重构,行为不变 | `refactor(tv): 复用共享进度条与 ViewModel` |
| `test` | 只改测试 | `test(search): 覆盖本地过滤清空整页时的搜索分页` |
| `build` | 构建脚本、依赖升级、打包 | `build: 升级 Compose Multiplatform 到 1.12.1` |
| `ci` | CI 工作流 | `ci(release): release notes 生成失败时让 step 失败` |
| `docs` | 文档,包括 `AGENTS.md` 与 `docs/` | `docs(testing): 说明反引号测试名只能使用的字符` |
| `chore` | 其他杂项:开发工具、Agent 技能脚本 | `chore(skill): desktop-ui-verify 的 input agent 支持注入外部文件拖放` |

- 界面改版、配色、版式调整用 `feat`;修正界面错误用 `fix`。不用 `ui`、`style`、`minor`、`opt` 等其他 type。
- 一个提交同时包含功能和测试时,按功能选 type。

## 3. scope

- 写模块或功能区的名字,全小写,多个词用 `-` 连接,例如 `media-selector`、`datasource-test-mcp`。
- 沿用已有的 scope,不另造同义词。常用 scope:

| 类别 | scope |
|---|---|
| 平台 | `android`、`ios`、`desktop`、`tv` |
| 页面与功能 | `player`、`subject`、`exploration`、`collection`、`schedule`、`search`、`settings`、`cache`、`download`、`danmaku`、`comment`、`notification`、`user`、`login`、`person`、`theme`、`update` |
| 数据与选源 | `datasource`、`media`、`media-selector`、`selector`、`mediasource`、`torrent`、`client` |
| 工程 | `release`、`analytics`、`codex-agent`、`datasource-test-mcp`、`skill`、`agents`、`testing`、`lang` |

- 平台专属的实现(打包、系统集成、只在该平台存在的功能)用平台 scope:`fix(desktop): macOS zip 发布包重新 ad-hoc 签名`。
- 跨平台功能里的平台差异用功能 scope,平台写进标题:`fix(player): decode compressed Matroska subtitles on Android`。

## 4. 标题

- 用简体中文写。代码标识符、库名、协议名照原文,不翻译:`用 WebSessionManager 自动处理数据源验证码`。
- 写改动带来的结果或行为,不写操作过程。
  - 好:`播放页选集放在详情分页下方`
  - 不好:`修改播放页`、`update`、`Fix test.`
- `fix` 写修好后的正确行为,或「修复 + 现象」:`续播时直接在记忆进度处打开媒体`、`修复验证码浏览器拦截`。
- 中文与英文、数字之间加一个空格:`升级 mediamp 到 0.5.3`。
- 多个要点用半角逗号加空格 `, ` 分隔,不用全角逗号。
- 引用界面文字用「」:`点「重启更新」后只剩交换目录`。
- 结尾不加句号。
- 尽量不超过 70 个字符,squash 合并后还会加上 ` (#1234)`。

## 5. 正文

- 标题能说清的小改动可以不写正文。
- 正文先用一段说明原因:原来的问题或需求,为什么这样改。
- 再用 `- ` 列出具体改动,每条以改到的类、函数或模块开头:

  ```
  - LabelFirstRawTitleParser: S\d+E\d+ 在 word 内按边界 find, 并先于 x264 过滤
  - selectVideoFileEntry: 兜底数字匹配要求集数独立成段
  ```

- 标点用半角(`, `、`; `、`.`),并列的词可以用 `、`。
- 不强制按固定列宽换行。一段写成一行,或在合适处换行,都可以。
- 写代码和行为。不写对话过程、Agent 的推理过程,也不写「按要求修改」之类的话。
- 关联 issue 写在正文最后一行:`Fixes #1234` 或 `Closes #1234`。

## 6. 署名与 trailer

- 不加 AI 工具的署名或标记,包括:
  - `Co-Authored-By: Claude …`,以及其他 AI 模型的 `Co-authored-by`
  - `Claude-Session: …`
  - `🤖 Generated with [Claude Code](…)`
- PR 描述同样不加上述内容。
- 工具默认会追加这些行时,提交前删掉。提交后用 `git log -1 --format=%B` 检查;还没 push 的提交可以用 `git commit --amend` 修改。
- 真人合作者照常写 `Co-authored-by: 名字 <邮箱>`。

## 7. 拆分提交

- 一个提交只做一件事。文档、格式化、依赖升级和功能改动分开提交。
- 每个提交都能编译。

## 8. 示例

只有标题:

```
docs(design): 新增 Ani 风格界面实现方案
```

带正文:

```
feat(theme): iOS 与 macOS 使用 Ani 配色

iOS 与 macOS 的 appColorScheme 使用 aniColorScheme: 背景与卡片为中性石板色, 强调色由主题色推导.
Android、Windows、Linux 的配色不变.

- Platform.kt: 新增 isApple()
- AniColorScheme.kt: 新增 aniColorScheme 与强调色推导
- AniColors.kt、AniTextStyles.kt: 新增 M3 没有的颜色与字级
```

修复:

```
fix(desktop): macOS 自动更新整包替换 .app, 修复更新后因签名无效无法启动

Fixes #1234
```
