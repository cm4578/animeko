# Ani 风格：已批准的功能与行为变更

> 状态：**已获维护者批准**，待实现。日期：2026-10-31。
> 本文只列「会改到既有逻辑或新增功能」的项目。纯 UI（版式、颜色、字级）的改动见 02-components.md、03-mobile-pages.md、04-macos.md。
> 视觉参考：Claude Design 画布「Ani 配色重选」，「定案画面」页（手机）与「macOS」页。文中用画板标题指代，例如「Ani macOS · 快取」。

---

## 0. 给执行 Agent 的说明

- **只有本文列出的项目可以改逻辑或加功能。** ViewModel、Repository、设置数据结构、导航逻辑的其他改动，一律先问维护者。
- **§8 是 2026-10-31 第二、三轮决议，优先于前文。** 特别是平台范围：本期只做 iOS 与 macOS（§8.1）；新增数据一律用冷流接入（§8.1）；新文案一律新增字串 key（§8.3）。
- 每个 PR 在描述里写明实现的是哪几项（A、B1、C2…），一个 PR 最好只做一组。
- **不改储存格式。** 除非本文明确写「新增字段」，否则不加、不删、不改任何持久化设置字段。新增字段必须有默认值，旧数据能正常反序列化。
- 复用现有函数。本文「现有」一栏列出的函数直接调用，不另写一套。
- 遵守 `AGENTS.md`：用 import，不写全限定名；注释直接描述当前设计；UI 截图按「UI Verification」一节上传为 GitHub 附件，不进仓库。
- 颜色一律用 token（定义见 01-tokens.md），组件用 C1–C21（定义见 02-components.md）。

## 1. 总表

| 编号 | 项目 | 平台 | 类型 |
|---|---|---|---|
| A | 配色：在 `appColorScheme` 的生成结果上覆写 Ani 色票 | iOS、macOS | 改既有函数 |
| B1 | 新增第 4 个分页「我的」 | iOS（iPhone、iPad） | 新页面、改导航 |
| B2 | 通知入口移到「我的」页；macOS 放在侧边栏底部铃铛 | iOS、macOS | 改入口 |
| B3 | macOS 侧边栏：新增一级入口与底部帐号区 | macOS | 改导航 |
| B4 | macOS 独立设定窗口（⌘,） | macOS | 新窗口 |
| C1 | 追番「列表／网格」切换 | iOS、macOS | 新功能 |
| C2 | macOS 键盘快捷键 | macOS | 新功能 |
| C3 | macOS 右键菜单与快取检阅器（⌘I） | macOS | 新功能 |
| C4 | 互动通知详情（只显示通知本身） | iOS、macOS | 新功能 |
| D1 | BT 限速改为「开关＋滑杆」 | iOS、macOS | 改既有设置 UI 的映射 |
| D2 | 列上的「⋮」菜单改为「选择」模式 | iOS、macOS | 改既有入口 |
| D3 | macOS 资料源表格加「启用」开关栏 | macOS | 改既有入口 |
| D4 | zh-TW 字串改为台湾用语（含第 3 分页改名「快取」） | 所有平台（zh-TW 字串资源共用，维护者已同意） | 改翻译 |
| G32 | macOS 扫码登录：Mac 显示 QR Code，手机扫描确认（原 §6 E） | macOS | 新功能 |
| G | 第二、三轮决议的其他项目（G1–G31） | 见 §8 | 见 §8 |

## 2. A 配色接入

**现状**

- `appColorScheme` 是 expect/actual：`app/shared/ui-foundation/src/{androidMain,iosMain,desktopMain}/kotlin/ui/foundation/theme/AppTheme.*.kt`。用 materialkolor 的 `dynamicColorScheme` 由种子色生成 M3 配色。
- 默认种子色 `DefaultSeedColor = Color(0xFF4F378B)`，在 `app/shared/app-platform/src/commonMain/kotlin/ui/theme/DefaultSeedColor.kt`。
- `useBlackBackground` 由 `modifyColorSchemeForBlackBackground` 处理，在 `AppTheme.kt`。

**要改成**

保留 materialkolor 的生成流程，在生成结果上用 `copy(...)` 覆写角色，得到 Ani 色票。覆写表、强调色推导、`AniColors` 的定义、测试都在 **01-tokens.md**，本文不重复。

**范围与边界**

- `useDynamicTheme`（Android 动态取色）开启时，**维持原行为，不覆写**。
- 「高对比度深色主题」（`useBlackBackground`）**保留**，继续在覆写之后套用 `modifyColorSchemeForBlackBackground`。
- TV（`AniTvTheme`、`TvColorMapping`）**不在范围内**。
- `AniTheme`、`AniThemeDefaults` 的现有成员不改。

**验收**：01-tokens.md 的单元测试通过；Android 开启动态取色时外观与改动前一致。

## 3. B 导航结构

### 3.1 B1 手机第 4 分页「我的」

**现状**

- `MainScreenPage`（`app/shared/app-platform/src/commonMain/kotlin/navigation/NavRoutes.kt`）只有 `Exploration`、`Collection`、`CacheManagement`。
- 个人入口是顶栏头像（`SelfAvatar`，在 `AniTopAppBar`）点开的 `ProfilePopup`（`app/shared/ui-settings/src/commonMain/kotlin/ui/settings/account/ProfilePopup.kt`）。弹窗由 `ProfilePopupLayout` 绘制，功能如下：

| 弹窗功能 | 回调 | 「我的」页对应位置 |
|---|---|---|
| 头像（可编辑） | `onClickEditAvatar` → 帐号设置 | 个人资料卡 |
| 昵称、编辑个人资料 | `onClickEditProfile` → 帐号设置 | 个人资料卡「编辑个人资料」 |
| 未登录时：登录／注册 | `onClickLogin` | 个人资料卡改为登录卡 |
| 通知（带未读点） | `onClickNotifications`，`showNotificationDot` | 通知列（B2） |
| 播放记录 | `onClickPlaybackHistory` | 设置分组里的一列，或个人资料卡下方 |
| 设置 | `onClickSettings` | 下方各设置分类入口 |
| 登出（带确认对话框 `AccountLogoutDialog`） | `onClickLogout` | 页面最底部 |

**要改成**：画板「Ani 風格 · 我的頁」。「我的」页必须承接上表**全部**功能，一项都不能少。

**改动**

- `MainScreenPage` 末尾新增 `Profile`。新增枚举值不影响已存 `UISettings.mainSceneInitialPage` 的反序列化。
- `MainScreenPage.visibleEntries`（现为 `entries`）改为按平台返回：iOS（iPhone、iPad）包含 `Profile`；macOS、Android、Windows、Linux 不包含（macOS 的「我的」由侧边栏帐号行与设定窗口承担，见 B3、B4）。平台用 `currentPlatform().isIos()` 判断。
- iPhone：C1 FloatingTabBar 显示 4 项。iPad：Apple 版 rail 也显示「我的」（G12）。
- 初始页面：「设置 › 界面 › 初始页面」的选项来自 `visibleEntries`（`AppSettingsTab.kt`），所以 iOS 可选「我的」，其他平台不出现（G12）。
- 现有对 `MainScreenPage` 的 `when` 都要补 `Profile` 分支，否则无法编译：`NavRoutes.kt` 的图标与名称各一处，`MainScreen.kt` 两处。名称用新增的字串 key（§8.3），图标用线条人像图标。`MainScreen.kt` 是 Android、Windows、Linux 仍在用的既有画面，其中的 `Profile` 分支只为通过编译，内容同 `Exploration`。这是 B1 对既有 Composable 唯一允许的改动。
- 读到的值不在当前平台的 `visibleEntries` 中时（例如 iOS 的设置备份导入到 macOS 或 Android），按 `Exploration` 处理。只在两处做这个映射：`AniApp.kt` 读取 `mainSceneInitialPage` 处，以及 `SettingsViewModel.restoreSettingsBackup` 导入时。不改存储格式。
- 页面 ViewModel 复用 `ProfileViewModel`，追番统计复用 `SubjectCollectionRepository.subjectCollectionCountsFlow()`（`UserCollectionsViewModel` 已在用）。
- 「我的」页的设置分类入口调用现有的 `navigateSettings(tab)`，分组按 G16。

**不要改**：`ProfilePopup` 本身保留（Android、Windows、Linux 继续使用）；`ProfileViewModel` 的逻辑。

**风险**

- 旧版 App 读取含 `Profile` 的设置会反序列化失败（降级安装，或把新版 iOS 的设置备份导入旧版）。这属于降级场景，在 PR 中说明，不另做处理。
- iPad 旋转时会在 rail 与底部导航之间切换，两种导航都要有「我的」。

**验收**：iPhone 底部导航 4 项，iPad rail 含「我的」；macOS、Android、Windows、Linux 的导航项与改动前一致；弹窗的每个功能都能在「我的」页找到并正常运作；未登录时显示登录入口；初始页面为「我的」的备份导入 macOS 或 Android 后，以「探索」打开。

### 3.2 B2 通知入口

**现状**

- 通知入口只在 `ProfilePopup`。路由已有 `NavRoutes.Notifications`、`NotificationDetail`、`Announcements`、`AnnouncementDetail`、`NotificationSettings`。
- 红点是布尔值：`MainScreenSharedViewModel.showNotificationDot`，由 `NotificationBadgeState.showsEntryDot(settings)` 算出（有未读通知；或开启公告提示点 `NotificationSettings.showAnnouncementDot` 且有未读公告）。`MainScreen` 再和登录状态合并成 `notificationDotVisible`，经 `LocalSelfAvatarUnreadDot` 给头像。入口只显示红点，不显示数字。
- 未读数在 `NotificationBadgeState.unreadCount`（`NotificationBadgeManager.state`），通知中心已在用；字串 `notification_unread_count`（zh-TW「%1$d 則未讀」）已存在。

**要改成**

- 手机：「我的」页有通知列，红点用 C12 AlertDot，后接「N 則未讀」。画板「Ani 風格 · 我的頁」。
- macOS：侧边栏底部放铃铛按钮，只显示红点，不显示数字；在通知中心时铃铛为选中态。设定窗口帐号窗格的通知列同手机，显示红点和「N 則未讀」。画板「Ani macOS · 通知中心與公告」「Ani macOS · 我的（設定視窗）」。

**改动**

- 红点：读取现有的 `showNotificationDot`，并照 `MainScreen` 的 `notificationDotVisible` 与登录状态合并，不另写判断。
- 数字：在 `MainScreenSharedViewModel` 新增只读的 `notificationUnreadCount: StateFlow<Int>`，取 `NotificationBadgeManager.state` 的 `unreadCount`；写法同 `showNotificationDot`（`by lazy` + `WhileSubscribed(5000)`），只有 Apple 版画面订阅（§8.1）。文字用现有的 `notification_unread_count`。`unreadCount` 为 0 时不显示数字（例如只有未读公告时只显示红点）。
- 点击调用 `navigator.navigateNotifications()`。

**验收**：「我的」通知列、macOS 铃铛与 Android 等平台的头像红点，显示规则一致；「N 則未讀」的数字与通知中心一致；未登录时都不显示红点和数字。

### 3.3 B3 macOS 侧边栏

**现状**

- 宽屏时 `MainScreen` 用 `AniNavigationSuite` 显示 `NavigationRail`：
  - 顶部 header 是搜索 FAB；
  - 中间是 3 个 `MainScreenPage`；
  - 底部 footer 是「设置」。
- 相关文件：`app/shared/src/commonMain/kotlin/ui/main/MainScreen.kt`，`app/shared/ui-adaptive/src/commonMain/kotlin/ui/adaptive/navigation/` 下的 `NavigationSuite.kt`、`AniNavigationSuiteScaffold.kt`。
- 布局类型由 `AniNavigationSuiteDefaults.calculateLayoutType` 决定。

**要改成**：C17 MacSidebar，画板「Ani macOS · 追番」等。

| 区块 | 内容 | 调用 |
|---|---|---|
| 顶部 | 搜索框，可直接输入；⌘K 聚焦 | G11：搜索词状态提升到主窗口层，复用 `SearchViewModel` |
| 「浏览」分组 | 探索、新番时间表、热度排行 | 探索为 `MainScreenPage`；后两者为 `navigateSchedule()`、`navigateTrendingRanking()` |
| 「我的片库」分组 | 追番、播放记录、快取 | 追番、快取为 `MainScreenPage`；播放记录为 `navigatePlaybackHistory()` |
| 底部 | 帐号（头像、昵称、待同步数）、通知铃铛（B2）、设置（B4） | 帐号行打开设定窗口的「帐号」窗格（B4） |

**改动**

- 新增 C17 MacSidebar；macOS 的主窗口按 `isMacOS()` 选用它。原 `AniNavigationSuite` / `NavigationRail` 不改，Windows、Linux 照旧。
- 只改「宽屏时用什么导航组件」。各页面路由、`AniNavigator` 的函数一律复用。
- 新番时间表、热度排行、播放记录目前是推入的路由，不是 `MainScreenPage`。侧边栏把它们显示为一级入口时，选中态以当前路由判断。**不要**把它们改成 `MainScreenPage`。

**风险**

- 侧边栏只用于 macOS（G21）。
- 搜索框本身就是输入框（G11），会改到搜索状态的归属，按 G11 的范围实现。

**验收**：每个入口都能到达对应页面，选中态正确；返回导航不受影响。

### 3.4 B4 macOS 独立设定窗口（⌘,）

**现状**

- 设置是主窗口里的一页：路由 `NavRoutes.Settings(tab)`，画面 `SettingsScreen`，内部已用 `AniListDetailPaneScaffold` 做列表加详情。
- 分类由 `SettingsTab` 枚举定义：PROFILE、APPEARANCE、THEME、UPDATE、PLAYER、MEDIA_SOURCE、MEDIA_SELECTOR、SERVER、PROXY、BT、STORAGE、SETTINGS_BACKUP、ABOUT、LOG、DEBUG。
- 主窗口在 `app/desktop/src/main/kotlin/AniDesktop.kt` 用 `Window(...)` 建立。
- 已有二级窗口的先例：图片查看器 `ImageViewer.desktop.kt`，通过 `LocalSecondaryWindowFrame` 沿用主窗口外观。

**要改成**：C20 MacSettingsWindow，画板「Ani macOS · 我的（設定視窗）」与第三层各设定窗格。

**改动**

- 在 `AniDesktop.kt` 新增一个可开关的设定 `Window`：
  - 照图片查看器的做法取用 `LocalSecondaryWindowFrame`；
  - 内容复用 `SettingsScreen` 的各 Tab 内容；
  - 左侧分类栏换成 C20 的样式。
- 打开方式：⌘,（macOS 菜单列「设置…」，用 Compose Desktop 的 `MenuBar`），以及侧边栏的设置按钮与帐号行。`MenuBar` 与其中的快捷键只在 `isMacOS()` 时加入，Windows、Linux 不加。
- 「通知」在 App 里没有对应的 `SettingsTab`。通知设定是 `NavRoutes.NotificationSettings`，从通知中心进入。设定窗口里的「通知」分类直接嵌入现有的通知设定画面，**不新增 `SettingsTab` 枚举值**。
- 「Bangumi 同步」在设定窗口里是帐号窗格的子页，复用 `BangumiSyncTab`。
- 「除错」只在除错模式下出现，沿用现有的显示条件。

**不要改**：各设置 Tab 的内容逻辑、`SettingsTab` 的枚举。

**风险**

- 同一时间只能有一个设定窗口。再次按 ⌘, 时，应把已有窗口带到前面。
- 设定窗口里改主题，主窗口要立即生效（两个窗口共用同一个 `SettingsRepository`，应该自然成立，需验证）。

**验收**：⌘, 打开、再按一次聚焦既有窗口；所有设置项在新窗口可用；关闭窗口不影响主窗口。

## 4. C 新交互

### 4.1 C1 追番「列表／网格」切换

**现状**：`SubjectCollectionsColumn`（`app/shared/ui-subject/src/commonMain/kotlin/ui/subject/collection/SubjectCollectionsColumn.kt`）用 `LazyVerticalGrid(GridCells.Adaptive(360.dp))`。窄屏是一栏大卡，宽屏是多栏大卡，没有切换。

**要改成**：画板「Ani 風格 · 追番首頁」（手机：清单／海报网格），「Ani macOS · 追番」（macOS：7 栏海报网格／表格式清单）。

**改动**

- 「我的追番」标题行加 C4 切换按钮（手机）；macOS 放在工具栏。
- 新增 Apple 版追番列表（列表、网格两种呈现），调用处按 `isApple()` 选择；原 `SubjectCollectionsColumn` 不改，Android、Windows、Linux 照旧。资料与分页（`LazyPagingItems`）复用。
- 记住选择：在 `MyCollectionsSettings`（`UISettings.kt`）**新增字段** `layout: CollectionLayout = CollectionLayout.LIST`，并新增 `enum class CollectionLayout { LIST, GRID }`。默认值保证旧数据可读；iOS 与 macOS 共用这个字段。

**验收**：切换后滚动位置合理（回到顶部或保持当前项）；重启后保留选择；旧设置文件读取后为列表模式。

### 4.2 C2 macOS 键盘快捷键

**现状**

- 搜索结果已经支持方向键选取：`keyboardDirectionToSelectItem`、`keyboardPageToScroll`，在 `ScrollHotKeys.kt`，用于 `SearchPageResultColumn`。
- 播放页已有快捷键（`PlayerKeyboardShortcuts.kt`）：
  - 空格：播放／暂停；F：全屏；B：弹幕；I：播放统计；
  - A、D、S：倍速；方向键：音量与快进。
- Esc 返回由 `BackNavigationInput.kt` 与 `AniDesktop.kt` 的 `BackKeyEventHandler` 处理。

**要改成（新增）**

| 快捷键 | 动作 | 作用范围 | 调用 |
|---|---|---|---|
| ⌘K | 聚焦侧边栏搜索框，或打开搜索页 | 主窗口 | `navigateSubjectSearch()` |
| ⌘, | 打开设定窗口 | 全局 | B4 |
| ⌘R | 重新整理当前页 | 探索、热度排行 | 各页现有的 refresh 或 retry |
| ⌘I | 打开或关闭快取检阅器 | 单番快取页 | C3 |
| ⌘T | 时间表回到今天 | 新番时间表 | 时间表现有状态 |
| ⌘← / ⌘→ | 上一周／下一周 | 新番时间表 | 时间表现有状态 |
| ↩ | 打开选中的搜索结果 | 搜索页 | 现有的选中逻辑 |

**改动**：快捷键注册在 `MenuBar` 的菜单项（macOS 惯例，菜单里能看到快捷键），或用 `onPreviewKeyEvent` 并判断 `isMetaPressed`；两种做法都只在 `isMacOS()` 时生效。播放页现有的单键快捷键不变。

**不做**：上表以外的快捷键维护者没有批准，包括「在 Finder 中显示」⇧⌘R、删除 ⌘⌫、资料源编辑器「重新测试」⌘R、按住 ⌘ 点选多选。菜单里也不要标这些快捷键。多选只走 D2 的「选择」模式。

**风险**

- ⌘I 与播放页的 I（播放统计）同在 I 键，但一个有 ⌘、一个没有，且作用页面不同，不冲突。
- ⌘R、⌘T 要确认目标页的 ViewModel 已有对应动作。没有就先问，不要新写资料逻辑。

**验收**：每个快捷键在对应页面生效，焦点在输入框时不误触发。

### 4.3 C3 macOS 右键菜单与快取检阅器

**现状**

- 右键已有基础设施：`Modifier.onRightClickIfSupported`（`ui-foundation/.../interaction/OnClick.kt`）。评论已有右键菜单 `CommentContextMenu`。
- 快取单集行 `DownloadRow`（`app/shared/ui-download/src/commonMain/kotlin/ui/download/components/DownloadRow.kt`）：
  - 点击行打开「⋮」下拉菜单 `DownloadActionDropdown`，项目是播放、继续、暂停、查看详情、删除（带确认）；
  - 长按进入选择模式。
- 单集下载详情是独立页：`MediaCacheDetailsScreen`，路由 `NavRoutes.CacheDetail(cacheId)`，带 `navigationIcon` 参数。

**要改成**：画板「Ani macOS · 快取」（右键菜单）、「Ani macOS · 快取詳情」（右侧检阅器）。

**改动**

- 右键菜单包含 `DownloadActionDropdown` 的全部项目（播放／继续／暂停、查看详情、删除…），复用同一组回调；另加「前往条目详情」（`navigateSubjectDetails`）与「在 Finder 中显示」（G9）。菜单里只有「查看下载详情」标快捷键 ⌘I。
- 「在 Finder 中显示」单个文件按 G9 实现。
- 检阅器：新增 macOS 版单番快取页，右侧以 C21 Inspector（320 宽）显示单集详情。内容是新增的 Apple 版单集详情（字段同 `MediaCacheDetailsScreen`，按钮见 G28），数据来自 `MediaCacheDetailsScreen` 现有的 ViewModel，顶部是关闭按钮。原 `SubjectDownloadsScreen`、`MediaCacheDetailsScreen` 不改；iOS 仍走原路由推入。
- 热度排行的右键菜单：打开条目详情、追番／取消追番（G2）、复制 Bangumi 链接与在浏览器中打开（G6）。

**验收**：右键与「⋮」菜单行为一致；检阅器与原详情页显示同样内容；⌘I 开关检阅器。

### 4.4 C4 互动通知详情（只显示通知本身）

**现状**

- 通知模型 `AppNotification`（`app/shared/app-data/src/commonMain/kotlin/data/models/notification/AppNotification.kt`）的 `content` 只有 `ReportResult`、`AccountSecurity`、`System`、`Unknown` 四种。
- 互动类（`NotificationGroup.INTERACTION`）只带 `NotificationDisplay`：`title`、`body`、`overline`、`icon`、`imageUrl`、`target`（subjectId、episodeId、personId、characterId、url）。**没有评论 ID，也没有评论串内容**。

**要改成**：画板「Ani macOS · 通知中心與公告」（右栏详情）、「Ani 風格 · 通知與公告詳情」（iOS 推入的详情页）。设计稿画的是「原评论加回复」的评论串，按下面第 1 步改为只显示通知本身。

**改动（分两步）**

1. **要做（仅 UI，iOS 与 macOS 相同）**：互动通知的详情显示 `display.title`、`display.body`（作为引用块）、目标条目或剧集，加「前往评论区」按钮，用 `target.destination` 跳转。iOS 点通知后照现有行为进入通知详情页（`DetailScreens.kt`），macOS 显示在右栏。
2. **不做**：完整评论串（原评论加回复）。维护者决定只显示通知本身（§8 G20）；不要动 `AniNotificationService` 和模型。

**验收**：第 1 步在无新数据的情况下可用；没有 `target` 的互动通知不显示跳转按钮。

## 5. D 既有行为调整

### 5.1 D1 BT 限速「开关＋滑杆」

**现状**

- `AnitorrentConfig`（`app/shared/app-data/src/commonMain/kotlin/data/models/preference/AnitorrentConfig.kt`）：
  - `downloadRateLimit: FileSize`，`FileSize.Unspecified` 表示不限；
  - `uploadRateLimit: FileSize`，`Unspecified` 表示不限，`FileSize.Zero` 表示不允许上传。
- 设置 UI `tabs/media/TorrentEngineGroup.kt` 的 `RateSliderItem`：滑杆 1–10 MB/s（`valueRange = 1f..10f`），**拉到 10 时存为 `Unspecified`**，显示「無限制」。

**要改成**：画板「Ani 風格 · 設定：網路與儲存」、「Ani macOS · 設定：網路與儲存」。「限制下载速度」／「限制上传速度」开关，打开后才出现滑杆。

**实现方式**：新增 Apple 版限速设置项（开关＋滑杆），iOS 与 macOS 的 BitTorrent 设置按 `isApple()` 选用。原 `RateSliderItem` 不改，Android、Windows、Linux 仍是 1–10、拉到 10 表示不限。

**映射（不改储存格式）**

| UI 状态 | 存储值 |
|---|---|
| 开关关闭 | `FileSize.Unspecified` |
| 开关打开，滑杆值 v | `v.megaBytes`（Apple 版滑杆范围 1–10 MB/s，10 就是 10 MB/s，不代表不限） |
| 读取到 `Unspecified` | 开关关闭，滑杆隐藏 |
| 读取到其他值 | 开关打开，滑杆显示该值 |
| 上传读取到 `FileSize.Zero` | 开关打开，滑杆在最小值，文字「不允许上传」。0 的语义保持不变 |

**初始值**：开关从关到开时，滑杆初始值为 **5 MB/s**（维护者已确认）。

**验收**：旧数据的三种情况（`Unspecified`、`Zero`、具体值）都能正确显示；切换开关后存储值符合上表。

### 5.2 D2 「⋮」菜单改为「选择」模式

**现状**

- 单番快取：`DownloadRow` 点击行打开「⋮」菜单，长按进入选择模式。选择工具栏 `DownloadSelectionToolbar` 已有继续、暂停、删除（批量）。
- 资料源管理：`MediaSourceGroup.kt` 每列有「⋮」`DropdownMenu`，内容是 `ToggleEnabledMenuItem`（启用／停用）、编辑、删除。选择工具栏 `MediaSourceSelectionToolbar` 已有启用、停用、删除（批量），由 `MediaSourceSelectionState` 管理。

**要改成**：画板「Ani 風格 · 快取詳情」、「Ani 風格 · 設定：播放與資料源」，以及对应的 macOS 画板。行上不再有「⋮」。

| 原「⋮」项目 | 新入口 |
|---|---|
| 快取：播放、继续、暂停 | 行右侧的主要动作按钮（`DownloadPrimaryAction`，已存在） |
| 快取：查看详情 | **点击行**（原本点击行是打开菜单） |
| 快取：删除 | 选择模式工具栏的删除；单集详情页底部的「刪除下載」 |
| 资料源：编辑 | **点击行** |
| 资料源：启用／停用、删除 | 选择模式工具栏 |

- 进入选择模式：iOS 为页面右上角的「選擇」按钮，加上原有的长按；macOS 为工具栏（快取、单番快取）或表格分组标题行右侧（资料源管理）的「選擇」按钮。不支持按住 ⌘ 点选。
- 资料源调整顺序：用行右侧的拖拽把手拖动，排序逻辑沿用现有；长按只用来进入选择模式。

**改动**：新增 Apple 版快取行与资料源行（不显示「⋮」，点击行为按上表），调用处按 `isApple()` 选择；原 `DownloadRow`、`MediaSourceGroup` 不改。选择状态类（`MediaSourceSelectionState` 等）与工具栏原样复用。

**风险**：点击快取行由「打开菜单」改为「打开详情」，是行为变更（已批准）。macOS 另有右键菜单（C3）保留全部项目。

**验收**：原菜单里的每个动作都有新入口可用；选择模式行为不变。

### 5.3 D3 macOS 资料源表格「启用」开关栏

**现状**：启用／停用由 `EditMediaSourceState.toggleMediaSourceEnabled(item, enabled)` 实现（`tabs/media/source/MediaSourceGroupState.kt`），目前只从「⋮」菜单（`ToggleEnabledMenuItem`）和选择工具栏调用。

**要改成**：画板「Ani macOS · 設定：播放與資料源」的资料源表格，每列最后一栏是 C7 AniSwitch。

**改动**：开关的 `onCheckedChange` 直接调用 `toggleMediaSourceEnabled(item, it)`，**不新写逻辑**。停用的列整列文字降透明度（见 04-macos.md）。

**验收**：开关与选择工具栏的启用／停用结果一致。

### 5.4 D4 zh-TW 字串改为台湾用语

**范围**：只改 `app/shared/app-lang/src/androidMain/res/values-zh-rTW/` 下的翻译值。不改 key，不改 `values/`（英文）、`values-zh-rCN/`、`values-zh-rHK/`。Apple 版画面的新文案不属于 D4，按 §8.3 新增 key。

**例外（不改）**

- `settings_app_close_behavior`、`settings_app_close_behavior_exit`、`settings_app_close_behavior_minimize_to_tray`：维护者指定沿用原字串。
- `oauth_bangumi_help_cant_receive_email_content` 里的「QQ 郵箱」「網易郵箱」：产品名，不改。

**替换规则**

| 原词 | 改为 | 备注 |
|---|---|---|
| 數據源 | 資料源 | |
| 數據 | 資料 | 先替换「數據源」再替换「數據」 |
| 緩存 | 快取 | |
| 屏蔽 | 封鎖 | |
| 設置 | 設定 | |
| 視頻 | 影片 | |
| 高級 | 進階 | |
| 客戶端 | 用戶端 | 不要再把「用戶端」的「用戶」换成「使用者」 |
| 計費網絡 | 計量付費網路 | 先于「網絡」替换 |
| 網絡 | 網路 | |
| 剪下板 | 剪貼簿 | |
| 抛棄 | 拋棄 | |
| 默認 | 預設 | |
| 添加 | 新增 | |
| 支持 | 支援 | |
| 發佈 | 發布 | |
| 全屏 | 全螢幕 | |
| 用戶（后面不接「端」） | 使用者 | |

**需人工判断，不做机械替换**：查看（20 处）、打開（15 处）、退出（10 处）、通過（4 处）、禁用。例如「退出登錄」应为「登出」，「審核通過」不改。逐条看上下文，拿不准的列在 PR 里问。

**受影响的字串（共 56 条：按上表规则扫描得出 55 条，加上 G13 的分页名 1 条）**

| 文件 | key | 替换 |
|---|---|---|
| strings.xml | main_screen_page_cache_management | 下載→快取（G13，不属于用语替换规则，单独改） |
| strings.xml | settings_mediasource_selector_cookies_description | 添加→新增 |
| strings.xml | settings_app_not_show_done_and_dropped_subjects | 抛棄→拋棄 |
| strings.xml | settings_media_source_delete_no_config | 添加→新增 |
| strings.xml | settings_media_source_delete_with_config | 添加→新增 |
| strings.xml | settings_media_source_delete_selected_confirmation | 添加→新增 |
| strings.xml | settings_media_source_subscription_add_dialog | 添加→新增 |
| strings.xml | settings_media_source_subscription_add_confirm | 添加→新增 |
| strings.xml | settings_media_source_add_button | 添加→新增 |
| strings.xml | settings_danmaku_add_regex_filter | 添加→新增 |
| strings.xml | settings_danmaku_add_regex | 添加→新增 |
| strings.xml | settings_danmaku_regex_description | 屏蔽→封鎖 ×2 |
| strings.xml | settings_media_torrent_sharing_settings | 設置→設定 |
| strings.xml | settings_media_torrent_sharing_description | 視頻→影片；網絡→網路；用戶→使用者 ×2 |
| strings.xml | settings_media_torrent_limit_upload_on_metered | 計費網絡→計量付費網路 |
| strings.xml | settings_media_torrent_limit_upload_on_metered_description | 計費網絡→計量付費網路 |
| strings.xml | settings_media_torrent_peer_filter | 屏蔽→封鎖；設置→設定 |
| strings.xml | settings_media_torrent_peer_filter_description | 緩存→快取；客戶端→用戶端 |
| strings.xml | settings_media_torrent_extra_trackers_description | 添加→新增；用戶→使用者 |
| strings.xml | settings_media_torrent_extra_trackers_dialog_description | 添加→新增 |
| strings.xml | cache_details_source | 數據源→資料源 |
| strings.xml | cache_details_publish_time | 發佈→發布 |
| strings.xml | subject_details_manage_cache | 緩存→快取 |
| strings.xml | media_selector_web_captcha_unsupported | 數據源→資料源；支持→支援 |
| strings.xml | media_selector_sources | 數據源→資料源 |
| strings.xml | media_selector_manual_no_sources | 數據源→資料源 |
| strings.xml | media_selector_bt_source_all | 數據源→資料源 |
| strings.xml | comment_add_emoji | 添加→新增 |
| strings.xml | comment_image_limit_reached | 添加→新增 |
| strings.xml | subject_episode_regex_filter_placeholder | 屏蔽→封鎖 |
| strings.xml | subject_episode_regex_filter_example | 屏蔽→封鎖 |
| strings.xml | subject_episode_regex_filter_add | 添加→新增 |
| strings.xml | subject_episode_video_settings_display_area_full | 全屏→全螢幕 |
| strings.xml | settings_storage_backup_op_backup_title | 剪下板→剪貼簿 |
| strings.xml | settings_storage_backup_op_backup_description | 剪下板→剪貼簿 |
| strings.xml | settings_storage_backup_op_restore | 剪下板→剪貼簿 |
| strings.xml | settings_storage_backup_op_restore_error | 剪下板→剪貼簿 |
| strings_media_selection.xml | settings_media_preference_description | 緩存→快取；設置→設定；默認→預設 |
| strings_media_selection.xml | settings_media_preference_override_notice | 設置→設定 |
| strings_media_selection.xml | settings_media_alliance_description | 支持→支援 |
| strings_media_selection.xml | settings_media_advanced_settings | 設置→設定；高級→進階 |
| strings_media_selection.xml | settings_media_advanced_settings_description | 數據源→資料源；設置→設定 |
| strings_media_selection.xml | settings_media_prefer_source_type | 數據源→資料源 |
| strings_media_selection.xml | settings_media_prefer_source_type_description | 數據源→資料源 ×2 |
| strings_media_selection.xml | settings_media_fast_select_web | 數據源→資料源 |
| strings_media_selection.xml | settings_media_fast_select_web_description | 數據源→資料源 ×3 |
| strings_media_selection.xml | settings_media_video_link_resolve_timeout_description | 設置→設定 |
| strings_media_selection.xml | settings_media_max_wait_time_description | 數據源→資料源 ×2；設置→設定 ×4 |
| strings_media_selection.xml | settings_media_show_disabled | 數據源→資料源 |
| strings_media_selection.xml | settings_media_show_disabled_description | 數據源→資料源 ×4；設置→設定 |
| strings_media_selection.xml | settings_media_image_captcha_auto_solve_description | 數據源→資料源 |
| strings_media_selection.xml | settings_media_auto_enable_last | 數據源→資料源 |
| strings_media_selection.xml | settings_media_auto_enable_last_description | 數據源→資料源 ×3；設置→設定 |
| strings_media_selection.xml | settings_media_auto_cache_none | 緩存→快取；設置→設定 |
| strings_media_selection.xml | settings_media_auto_cache_all | 緩存→快取；設置→設定 |
| strings_media_selection.xml | settings_media_auto_cache_partial | 緩存→快取；設置→設定 |

`strings_settings.xml`、`strings_notification.xml`、`tv.xml` 按上表规则扫描无命中。

**复查用指令**（在仓库根目录执行，列出仍含大陆用语的字串）：

```bash
grep -nE '數據|緩存|屏蔽|設置|視頻|高級|客戶端|網絡|剪下板|抛棄|默認|添加|支持|發佈|全屏' \
  app/shared/app-lang/src/androidMain/res/values-zh-rTW/*.xml \
  | grep -v 'settings_app_close_behavior'
```

**验收**：复查指令只剩例外项；`values-zh-rTW` 的 key 集合与改动前完全一致；占位符（`%1$d` 等）未被改动。

## 6. E 已定案，无需再问

| 项目 | 决定 | 实现要点 |
|---|---|---|
| 资料源编辑器 | 沿用 App 的自动储存，没有「取消／儲存」 | 不改 `EditMediaSourceLayout` 的储存逻辑；离开时的「捨棄變更」确认（`ConfimExitDialog.kt`）照旧。设计稿右上角显示「修改會自動儲存」提示 |
| macOS 播放器设置 | 分成「播放」「彈幕與其他」两个分页 | 只改 UI 分组，设置项与逻辑不变 |
| 高对比度深色主题 | 保留 | `useBlackBackground` 与 `modifyColorSchemeForBlackBackground` 不变 |
| 关闭行为字串 | 沿用 App 原字串：「關閉行為」「關閉應用」「最小化到系統托盤」 | D4 已排除 |

macOS 扫码登录已移到 §8（G32，实现要点见 §8.4）。

## 7. 对照设计稿的检查结果（结论见 §8）

> 本表是撰写时的检查记录，保留作为依据。表中「需询问」的项目都已答复，**结论以 §8 为准**，实现时不要再按本表停工。

**规则**：本文没有列出的逻辑或功能改动，一律先问维护者，不要自行实现。下表是对照设计稿与代码后，已知会碰到的地方。

- 「仅 UI」表示现有数据已足够，可以直接按设计稿做。
- 「需询问」表示要新数据、新动作或新的储存，**动手前先问**。

| # | 设计稿内容 | 出处画板 | 现有代码 | 结论 |
|---|---|---|---|---|
| 1 | 热度排行改为列表／表格 | 「Ani 風格 · 熱度排行」「Ani macOS · 熱度排行」 | `TrendingRankingItemPresentation` 有 `rank`、`heat`、`subject` | 仅 UI |
| 2 | 播放记录按日期分组 | 「Ani 風格 · 播放記錄」 | 每项有 `updatedAtMillis` | 仅 UI |
| 3 | macOS 播放记录右栏的同步队列 | 「Ani macOS · 播放記錄」 | `PlaybackHistoryScreen.kt` 已有 `pendingSyncEpisodes`、同步状态项 | 仅 UI |
| 4 | 「我的」页追番统计（在看／想看／看过） | 「Ani 風格 · 我的頁」 | `subjectCollectionCountsFlow()` 已存在 | 仅 UI（随 B1 组装） |
| 5 | 搜索结果、热度排行每行的「追番／已追番」按钮 | 「Ani 風格 · 搜尋頁」「Ani 風格 · 熱度排行」及 macOS 版 | `SubjectPreviewItemInfo` 没有收藏状态，也没有收藏动作 | **需询问** |
| 6 | 条目详情、人物页工具栏的「分享」 | 「Ani macOS · 條目詳情」「Ani 風格 · 人物、角色與關係圖」 | 只有剧集分享（`PlayingEpisodeSummary` 的 `onClickShare`），没有条目或人物分享 | **需询问** |
| 7 | 新番时间表卡片上的追番切换钮 | 「Ani macOS · 新番時間表」 | `AiringScheduleItemPresentation` 有 `subjectCollectionType`（可显示状态），时间表没有收藏动作 | 显示状态：仅 UI；切换：**需询问** |
| 8 | macOS 探索页右栏的「今日新番」小工具 | 「Ani macOS · 探索」 | `ExplorationPageViewModel` 只有热度、推荐、追番数据，没有时间表 | **需询问**（需要把时间表数据接进探索页） |
| 9 | 「我的」页「Bangumi 同步 · N 項待同步」 | 「Ani 風格 · 我的頁」 | 有两个队列：播放进度（`PlaybackHistorySyncer`）与收藏（`BangumiSyncTab`） | **需询问**：数字取哪个队列，或两者相加 |
| 10 | 侧边栏搜索框本身就是输入框（设计稿显示查询字） | 「Ani macOS · 搜尋」 | 搜索输入框在搜索页内 | 点击跳转搜索页：仅 UI；输入框移到侧边栏：**需询问**（要改搜索状态的归属） |
| 11 | 侧边栏、独立设定窗口是否也用在 Windows、Linux | B3、B4 | 桌面三平台共用 `MainScreen` | **需询问** |
| 12 | 「我的」分页是否出现在平板、横屏的 NavigationRail，以及能否设为初始页面 | B1 | `calculateLayoutType` 在横屏手机会用 NavigationRail | **需询问** |
| 13 | 有「我的」分页后，窄屏顶栏是否保留头像按钮 | 「Ani 風格 · 追番首頁」 | 头像在 `AniTopAppBar` | **需询问** |
| 14 | 快取分页的标签「下載」改「快取」 | 「Ani 風格 · 快取頁」 | zh-TW 现为「下載」，不属于 D4 的用语替换 | **需询问** |
| 15 | 单番快取页「進行中／已完成」分区 | 「Ani 風格 · 快取詳情」 | 下载项有状态字段 | 仅 UI |
| 16 | 「在 Finder 中顯示」单个快取文件 | 「Ani macOS · 快取」 | 储存设置有「打开 BT 快取资料夹」；单文件定位未确认 | 打开资料夹：仅 UI；定位单文件：**需询问** |
| 17 | 互动通知的完整评论串 | C4 第 2 步 | 通知没有评论 ID | **需询问** |
| 18 | BT 限速开关从关到开的初始值 | D1 | 无 | **需询问**（建议 5 MB/s） |

## 8. 第二、三轮决议（2026-10-31，优先于前文）

### 8.1 平台范围与实现策略

- **本期只做 iOS 与 macOS。** Android（手机、平板）、Windows、Linux、Android TV **维持现状**，之后另行设计。
- 唯一例外是 D4 字串：zh-TW 资源各平台共用，维护者同意所有平台一起改（只改文字，不改版式）。
- 实现策略（平台闸门）：
  - 在 `utils/platform/src/commonMain/kotlin/Platform.kt` 新增 `fun Platform.isApple(): Boolean = isIos() || isMacOS()`（与现有 `isIos()`、`isMacOS()` 写在一起，纯新增）。
  - 配色：只在 iOS 的 `appColorScheme`，以及 desktop 的 `appColorScheme` 中 `isMacOS()` 分支套用 `aniColorScheme`（见 01 §9）。
  - 版式与组件：**新增** Ani 版的 Composable，由调用处按 `LocalPlatform.current.isApple()` 选择；**既有 Composable 原样保留**给 Android、Windows、Linux 使用。不要在既有 Composable 内部改样式。
  - 导航结构（B1、B3、B4、G10、G11、G12、G22–G24、G31）只在 Apple 平台生效；其他平台走原有导航。macOS 专属的窗口、`MenuBar`、快捷键用 `isMacOS()` 判断，Windows、Linux 不加。
  - 新增的数据接入（B2 未读数、G3、G4、G5、G7、G15、G27、G29、G30）可以放在共用的 ViewModel，但必须是冷流：用 `stateIn(scope, SharingStarted.WhileSubscribed(5000), 初始值)`，需要时外面再包 `by lazy`（写法同 `MainScreenSharedViewModel.showNotificationDot`）。只有 Apple 版画面订阅时才开始请求或计算；其他平台的画面不订阅，就没有额外开销。不要在 ViewModel 的 `init` 里启动收集。

### 8.2 决议表

§7 中「需询问」的项目与撰写时新发现的问题，结论如下。G1–G21 为第二轮决议，G22–G32 为第三轮决议（同日）。「新增」表示维护者已批准新增功能或数据。

| 编号 | 项目 | 结论 | 实现要点 |
|---|---|---|---|
| G1 | 平台范围 | 只做 iOS、macOS | 见 §8.1 |
| G2 | 列表上的快速追番按钮（探索推荐、新番时间表、搜索结果、热度排行） | **新增** | 复用条目详情页现有的「修改收藏类型」动作（同一 Repository 方法），不新增 API；列表项需要能读到收藏状态，`SubjectPreviewItemInfo` 等列表模型可新增带默认值的字段 |
| G3 | 追番页「继续观看」 | **新增** | 在追番页接入 `FollowedSubjectsRepository`（探索页已在用），不改 Repository；按 §8.1 用冷流。「剩 M 分鐘」与进度见 G27 |
| G4 | macOS 探索页「今日新番」小工具 | **新增** | 在探索页接入 `ScheduleViewModel` 的数据源（只取今天），不改时间表逻辑 |
| G5 | macOS 快取页统计卡 | **新增** | 汇总现有下载引擎的速度与累计流量；快取位置读取现有 `CacheDirectoryGroup` 的设置值，「在 Finder 中显示」打开该资料夹。iPhone 版见 G30 |
| G6 | 条目详情、人物／角色页「分享」 | **新增** | 照剧集分享 `ShareEpisodeDropdown`（`app/shared/src/commonMain/kotlin/ui/subject/episode/details/components/ShareEpisodeDropdown.kt`）的做法，点分享按钮弹出菜单，两项：「复制链接」（`LocalClipboard`）、「在浏览器中打开」（`rememberAsyncBrowserNavigator()`）。链接为 Bangumi 页面：`https://bgm.tv/subject/{id}`（条目详情已在用）、`/person/{id}`、`/character/{id}`。**不接系统分享面板**。热度排行右键菜单（C3）用同样两项 |
| G7 | Bangumi 同步队列显示番名 | **新增** | 按 `BangumiSyncOp` 的条目 ID 查询条目名称（复用现有条目信息查询），查不到时退回显示 ID |
| G8 | 追番「管理」批量操作 | **新增** | 进入多选模式，批量修改收藏类型、取消追番；设计稿没有画出多选状态，**实现前先出交互草图给维护者确认**。G8 合入前，追番页的「管理」按钮不显示 |
| G9 | 「在 Finder 中显示」单个快取文件 | **新增** | macOS 用 `Desktop.getDesktop().browseFileDirectory(file)`（或 `open -R`）定位文件 |
| G10 | 推入页也显示浮动导航栏 | **新增**（改导航结构） | 仅 iOS。实现要点见 §8.5 |
| G11 | macOS 侧边栏搜索框直接输入 | **新增**（改搜索状态归属） | 搜索词状态提升到主窗口层；侧边栏输入后主区域显示搜索结果页，复用现有 `SearchViewModel`；⌘K 聚焦搜索框 |
| G12 | iPad 也显示「我的」 | **新增** | iPad 的 Apple 版 rail 也放「我的」；初始页面设定在 iOS 可选「我的」（macOS 没有「我的」分页，见 B1） |
| G13 | 第 3 分页名称 | 「快取」 | 随 D4 修改 zh-TW 字串，所有平台生效 |
| G14 | 有「我的」分页后的页头头像按钮 | **移除**（Apple 平台） | 追番、探索页头不再显示头像；Android 等平台不变 |
| G15 | 「N 項待同步」的数字 | 三条队列**合计**（维护者 2026-10-31 确认） | 数字 = 播放进度与看过状态（`PlaybackHistoryViewModel.pendingSyncEpisodesFlow` 的元素个数，已按集合并）+ 收藏同步队列（`BangumiSyncCommandRepository` 中待执行的命令数）。这样与点进去的「Bangumi 同步」页内容一致。「我的」页与 macOS 侧边栏底部用同一个数。取用时组一个只读冷流（§8.1），不改原 flow；收藏队列若没有现成的计数，用 `listSyncCommands` 的结果计数，不新增 DAO 查询以外的逻辑 |
| G16 | 「软件更新」所在分组 | 照 App 放「其他」 | 手机「我的」页设定分组与 macOS 设定窗口一致；手机设计稿把它放在「应用与介面」，以本文为准 |
| G17 | 条目页动态主题 | Apple 平台也套用 Ani 配色 | `MaterialThemeFromPaletteAndImage.kt` 在 `isApple()` 时改用 `aniColorScheme(封面色, isDark)`：强调色跟随封面，背景维持中性色；其他平台不变 |
| G18 | BT 限速开关开启时的初始值 | 5 MB/s | 见 D1 |
| G19 | 编辑个人资料「电子邮件」行 | **新增**：合并为一个「›」 | 点击后弹出选单（iOS 为 action sheet，macOS 为菜单），含「更改电子邮件」「解除绑定」，两项各自调用现有动作 |
| G20 | 互动通知详情 | 只显示通知本身 | 见 C4；不做完整评论串 |
| G21 | Windows、Linux 是否用 macOS 版型 | 不用 | 之后另行设计 |
| G22 | 资料源编辑器独立窗口（macOS） | **新增**（新窗口） | 1200×760。照 B4 在 `AniDesktop.kt` 新增可开关的 `Window`，取用 `LocalSecondaryWindowFrame`；内容复用现有三种编辑页与 ViewModel，自动保存与离开确认（`ConfimExitDialog`）照旧。从资料源管理点某一行打开；窗口已开时再点任何一行，只把已开窗口带到前面。关闭窗口等同原本的返回。只在 `isMacOS()`；其他平台照旧在页面内推入 |
| G23 | 合并收藏独立窗口（macOS） | **新增**（新窗口） | 1040×720。做法同 G22；从 Bangumi 同步的「N 項衝突」打开，内容复用 `BangumiMergeScreen`，「取消」或「套用合併」完成后关闭窗口 |
| G24 | 登录、授权、扫码小窗口（macOS） | **新增**（新窗口） | 480×640。做法同 G22；内容复用 `EmailLoginStartScreen`、`EmailLoginVerifyScreen`、`OAuthAuthorizeScreen` 与 G32 的扫码面板。登录成功或取消时关闭窗口：在窗口这一层把原本的返回、完成回调接到「关闭窗口」，不改各 ViewModel。若某个画面在内部直接调用 `AniNavigator`、接不到关闭，先问 |
| G25 | macOS 快取页「全部暂停」 | **新增** | `DownloadManagementViewModel` 新增 `pauseAll()`：取当前所有下载中项目的 id，调用一次 `operationRunner.run(ids, DownloadOperation.Pause)`，与单项暂停走同一路径。没有下载中的项目时按钮停用。暂停后按钮变为「全部继续」（新增 `resumeAll()`，走单项继续的同一路径），行为与单番页一致（维护者已确认） |
| G26 | macOS 追番排序按钮 | **不做**（维护者 2026-10-31 决定） | 现有顺序即「最近更新」，只有一个选项不需要菜单。macOS 追番工具栏拿掉排序按钮，只保留排版切换 |
| G27 | 「继续观看」剩余分钟与进度 | **新增** | 用 `EpisodePlayHistoryRepository.flowByEpisodeIds(ids)` 取 `EpisodeHistory.positionMillis`、`durationMillis`。进度 = position ÷ duration；剩余分钟 = ⌈(duration − position) ÷ 60000⌉。没有记录或 duration 为 0 时，不显示「剩 M 分鐘」，进度为 0。iOS 与 macOS 共用；按 §8.1 用冷流 |
| G28 | 单集下载详情的「播放」「暂停／继续」按钮 | **新增**（新入口） | 播放复用 `DownloadRow` 主要动作里的播放回调；暂停／继续复用 `DownloadManagementViewModel.pauseDownload`、`resumeDownload`（`operationRunner`）。回调由调用处传进单集详情画面，不在详情页的 ViewModel 里另写下载控制。按钮文字随状态切换「暫停下載」「繼續下載」；已完成时只显示「播放」。iOS 详情页与 macOS 检阅器相同 |
| G29 | 追番列表「已快取」状态 | **新增** | 每部条目用 `EpisodeProgressRepository.subjectEpisodeProgressesInfoFlow(subjectId)`：下一集（第一个未看的集）的 `cacheStatus` 是 `EpisodeCacheStatus.Cached` 时，状态行显示「已快取」。状态行优先级：有新集 > 已快取 > 其他提示。按 §8.1 用冷流，只在该行可见时订阅 |
| G30 | iPhone 快取页速度卡 | **新增** | 与 G5 同一组汇总数据（下载、上传速度与累计流量）。iPhone 只显示下载、上传两栏，不显示快取位置 |
| G31 | macOS「隐藏侧边栏」按钮 | **新增** | 侧边栏顶部的按钮切换侧边栏显示；隐藏后，工具栏左侧同一位置出现「显示侧边栏」按钮。状态只存在主窗口的内存里（`rememberSaveable`），不新增设置字段 |
| G32 | macOS 扫码登录 | **新增** | Mac 显示 QR Code，手机扫描确认（原 §6 E）。实现要点见 §8.4 |
| — | 未批准、不要做 | — | 快捷键 ⇧⌘R（在 Finder 中显示）、⌘⌫（删除）、资料源编辑器「重新测试」⌘R；按住 ⌘ 点选多选。见 C2 |
| — | §7 中「仅 UI」的项目（#1–#4、#15） | 照设计稿做 | 仅 Apple 版 UI |

### 8.3 新文案与字串

- Apple 版画面上新出现的文案（提示、说明、按钮，例如表下提示「按兩下開啟…」、「我的」分页名、G25「全部暫停」、G31「隱藏側邊欄」）一律**新增 key**，在 `app/shared/app-lang/src/androidMain/res/` 下的 `values`（英文）、`values-zh-rCN`、`values-zh-rHK`、`values-zh-rTW` 四处同时加入。能复用现有字串的直接复用，例如「N 則未讀」用 `notification_unread_count`。
- 既有 key 的值只在 D4 范围内修改（只改 zh-TW），其他语言不动。
- 公告设定的说明：现有 `notification_settings_announcement_description`（zh-TW「關閉後新公告不會在頭像上顯示紅點。」）在 Apple 平台已不准确（G14 移除了头像）。新增 Apple 专用 key `notification_settings_announcement_description_apple`，zh-TW 为「關閉後，新公告不會顯示未讀提示點。」，iOS 与 macOS 共用；其他平台继续用原 key。

### 8.4 G32 macOS 扫码登录的实现要点

- 现状：
  - `isQrCodeScannerSupported`（`app/shared/ui-onboarding/src/{commonMain,desktopMain,…}/kotlin/ui/qrlogin/QrCodeScanner*.kt`）在桌面为 `false`，所以 `AniAppContent.kt` 不给桌面扫码入口。
  - 「显示 QR Code 等手机扫」的面板 `TvQrLoginPanel` 与 `TvLoginViewModel` 只在 `androidTv` 源集，只作参考，**不改**。
  - 轮询逻辑 `QrLoginPoller`（`app/shared/app-data/src/commonMain/kotlin/domain/session/auth/QrLoginPoller.kt`）在 `commonMain`，可直接给桌面用。
  - 文案已有 `tv_login_qr_*` 系列字串。
- 改动：
  - 新增桌面可用的 ViewModel：照 `TvLoginViewModel` 的 QR 部分写，用 `QrLoginPoller`。`deviceName` 改用桌面可取得的主机名或型号，不用 Android `Build`。
  - 新增 macOS 面板 UI，画板「Ani macOS · 登入流程」的「等待掃描」「已掃碼」两态，文案用 `tv_login_qr_*`。面板显示在 G24 的登录小窗口里。
  - 登录页加「用手機掃碼登入」入口，只在 macOS 显示。
  - **不改** `QrLoginPoller`、`QrLoginRepository`、TV 端代码。
- 风险：若要把 `TvQrLoginUiState` 和映射函数移到 `commonMain` 共用，会动到 TV 代码，先问。默认做法是桌面另写一份。

### 8.5 G10 推入页导航栏的实现要点

- **位置**：C1 FloatingTabBar 画在 `AniAppContent.kt` 的 `NavDisplay` 外层，盖在所有页面上方，只在 iOS 显示。是否显示按 02 C1 的显示规则，由返回栈顶的路由类型决定：全屏任务隐藏，其余显示。
- **高亮的分页（来源分页）**：返回栈中最近一个 `NavRoutes.Main` 条目当前选中的分页。这个值现在是 `entry<NavRoutes.Main>` 里的 `currentPage`（`rememberSaveable`），要提升到 `NavDisplay` 外层，或由 Main 条目同步出来；分页切换逻辑不变。不要在各页面写死「条目详情 → 追番」这类对应：例如从探索打开条目详情，就高亮「探索」。
- **点击**：
  - 点其他分页：弹回到该 Main 条目，再切到所点的分页。
  - 点当前的来源分页：弹回到该 Main 条目，即回到该分页的根页面。
  - 两者都用 `AniNavigator` 现有函数（`popBackStack(route, inclusive = false)` 等）组合，不新增导航函数。
- 主分页上的 C1 行为不变。Android 与 macOS 不显示推入页导航栏。

