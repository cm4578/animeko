# 01 · 设计 token 与配色实现

> 范围:iOS 与 macOS(Desktop JVM 运行在 macOS 时)。Android、Windows、Linux、Android TV 维持现状(05 §8.1)。
> 本文是色值、字级、间距的唯一来源;02–04 只引用 token 名称。

---

## 0. 给执行 Agent 的说明

- 本文对应维护者批准的变更 **A「覆写生成结果」**与 **G17**:保留 materialkolor 生成流程,在生成结果上覆写角色。改动集中在 iOS 的 `appColorScheme` 与 desktop `appColorScheme` 的 macOS 分支,并新增几个只读的 token 文件(§5、§7)。
- **不改** `AniTheme`、`AniThemeDefaults`、`ThemeSettings`、`DefaultSeedColor`、`modifyColorSchemeForBlackBackground`、Android 的 `AppTheme.android.kt`、TV 的 `AniTvTheme`。
- Windows、Linux 的配色逻辑(含跟随系统强调色)原样保留。

## 1. 目标与非目标

**目标**

1. 背景、卡片、导航为带一点蓝的中性色(浅色「雾蓝灰」,深色「石板夜色」),去掉种子色带来的紫色调。
2. 默认主题色显示为品牌蓝:浅色 `#3072A1`,深色 `#76AEDD`。
3. 用户选其他预设主题色、或开启「条目页动态主题」时,按 §6 推导同风格的强调色,背景仍为中性色。
4. 文字配色组合 ≥ 4.5:1,非文字 UI 元素 ≥ 3:1(§3)。**例外**:开关关闭时的轨道用 separator,与背景的对比不到 3:1。这是刻意的:开关的开 / 关靠圆钮位置与开启时的 accentFill 区分,不靠关闭轨道的颜色(02 C7)。

**非目标**

- Android、Windows、Linux、Android TV 的配色(之后另行设计)。

## 2. Token 表

### 2.1 通用 token

| Token | 用途 | 浅色 | 深色 |
|---|---|---|---|
| bg | 页面背景 | `#F1F4F9` | `#0D141B` |
| card | 卡片、列表卡、对话框、弹出面板内的卡 | `#FFFFFF` | `#182029` |
| tabBg | 浮动导航栏、弹出菜单、浮起表面 | `#FFFFFF` | `#1E2833` |
| fill | 中性填充:次要按钮、输入框、进度轨、已看选集 | `#E6EBF3` | `#252F39` |
| separator | 分隔线、关闭的开关轨道 | `#C7CCD4` | `#303A44` |
| label | 主要文字 | `#131D26` | `#E6EAEF` |
| labelSoft | 次一级文字(未选中筛选胶囊、表格次要列) | `#3A434C` | `#C5CBD1` |
| label2 | 次要文字、未选中图标 | `#5C646D` | `#9FA7B0` |
| accent | 链接文字、当前位置、进度、选中图标 | `#3072A1` | `#76AEDD` |
| tint | 强调淡底:选中胶囊、选中行、当前集 | `#E6EEF4` | `#293A49` |
| onTint | tint 上的文字 | `#1B4A6E` | `#C1DDF5` |
| accentFill | 主要按钮底、开关开启 | `#3072A1` | `#2F78A9` |
| onAccentFill | accentFill 上的文字 | `#FFFFFF` | `#FFFFFF` |
| alert | 唯一提示色:新内容/未读小圆点 | `#E2553A` | `#F07457` |
| error | 错误状态 | `#D70015` | `#FF6961` |

- 浅色 tint = `mixSrgb(accent, #FFFFFF, 0.12)`;深色 tint = `mixSrgb(accent, card, 0.18)`。
- 深色的 accent 与 accentFill 不同:accent `#76AEDD` 用于文字和图标(在深色底上对比度高),accentFill `#2F78A9` 用于按钮底(配白字)。

### 2.2 品牌常量(不随主题变)

| 名称 | 色值 | 用途 |
|---|---|---|
| brandSlate | `#2F3943` | 品牌头像底、App 图标底 |
| brandRing | `#7EBBED` | 头像光环、播放器进度条已播放段、播放器「弹」按钮底 |

### 2.3 浮起阴影

| 名称 | 浅色 | 深色 |
|---|---|---|
| floatingShadow | y 8、模糊 24、`rgba(19,29,38,0.14)` | y 8、模糊 24、`rgba(0,0,0,0.45)`,外加 1dp separator 描边 |

用于 C1 浮动导航栏、C15 弹出菜单。

### 2.4 macOS 专用

| Token | 用途 | 浅色 | 深色 |
|---|---|---|---|
| sidebar | 主侧边栏、设定窗口分类栏、检阅器底 | `#E7ECF2` | `#141C24` |
| sidebarField | 侧边栏搜索框底 | `rgba(19,29,38,0.06)` | `rgba(255,255,255,0.07)` |
| hover | 悬停底 | `rgba(19,29,38,0.05)` | `rgba(255,255,255,0.05)` |

窗口阴影由系统提供,不自绘。

## 3. 对比度

| 组合 | 浅色 | 深色 | 要求 |
|---|---|---|---|
| accent / bg | 4.70 | 7.80 | 文字 ≥ 4.5 |
| accent / card | 5.20 | 6.95 | 文字 ≥ 4.5 |
| label2 / fill | 5.02 | — | 文字 ≥ 4.5 |
| label2 / card | 6.0 | 6.75 | 文字 ≥ 4.5 |
| onTint / tint | 7.95 | 8.30 | 文字 ≥ 4.5 |
| onAccentFill / accentFill | 5.20 | 4.79 | 文字 ≥ 4.5 |
| outline / bg | 3.52 | 5.07 | 非文字 ≥ 3 |

## 4. 映射到 Material 3 `ColorScheme`

### 4.1 现有页面怎么取色

`AniThemeDefaults` 决定角色在页面上的用途。映射后每个角色落到对应 token:

| `AniThemeDefaults` 成员 | M3 角色 | Ani token |
|---|---|---|
| `pageContentBackgroundColor` | `surfaceContainerLowest` | bg |
| `navigationContainerColor` | `surfaceContainer` | tabBg |
| `topAppBarColors()` 默认 / 滚动后 | `surfaceContainerLowest` / `surfaceContainer` | bg / tabBg |
| `backgroundCardColors()` | `surfaceContainerLow` | card |
| `primaryCardColors()` | `surfaceContainerHigh` | card |

### 4.2 完整角色表

带 * 的角色由 §6 推导;表中是默认主题色(品牌蓝)的值。

| 角色 | 浅色 | 深色 | 对应 token / 说明 |
|---|---|---|---|
| primary * | `#3072A1` | `#76AEDD` | accent |
| onPrimary * | `#FFFFFF` | `#0D141B` | M3 默认 `Button` 在深色下为浅蓝底深字;Ani 主要按钮改用 accentFill(02 C8) |
| primaryContainer * | `#E6EEF4` | `#293A49` | tint |
| onPrimaryContainer * | `#1B4A6E` | `#C1DDF5` | onTint |
| inversePrimary * | `#76AEDD` | `#2A6590` | Snackbar 按钮;需在 inverseSurface 上 ≥ 4.5 |
| secondary | `#5C646D` | `#9FA7B0` | label2 |
| onSecondary | `#FFFFFF` | `#0D141B` | |
| secondaryContainer * | `#E6EEF4` | `#293A49` | tint(导航指示器、选中 FilterChip) |
| onSecondaryContainer * | `#1B4A6E` | `#C1DDF5` | onTint |
| tertiary | `#131D26` | `#E6EAEF` | label,见 §4.3 |
| onTertiary | `#FFFFFF` | `#0D141B` | |
| tertiaryContainer * | `#E6EEF4` | `#293A49` | tint |
| onTertiaryContainer * | `#1B4A6E` | `#C1DDF5` | onTint |
| background | `#F1F4F9` | `#0D141B` | bg |
| onBackground | `#131D26` | `#E6EAEF` | label |
| surface | `#F1F4F9` | `#0D141B` | bg |
| onSurface | `#131D26` | `#E6EAEF` | label |
| surfaceVariant | `#E6EBF3` | `#252F39` | fill |
| onSurfaceVariant | `#5C646D` | `#9FA7B0` | label2 |
| surfaceTint | `#F1F4F9` | `#0D141B` | **与 surface 同值**,见 §4.3 |
| inverseSurface | `#1E2833` | `#E6EAEF` | |
| inverseOnSurface | `#E6EAEF` | `#182029` | |
| error | `#D70015` | `#FF6961` | error |
| onError | `#FFFFFF` | `#3C110E` | |
| errorContainer | `#FBE6E8` | `#452A2A` | |
| onErrorContainer | `#8C0009` | `#FFB4AB` | |
| outline | `#7A828B` | `#7D8792` | 描边按钮、描边输入框 |
| outlineVariant | `#C7CCD4` | `#303A44` | separator |
| scrim | `#000000` | `#000000` | |
| surfaceBright | `#FFFFFF` | `#252F39` | |
| surfaceDim | `#E6EBF3` | `#0D141B` | |
| surfaceContainerLowest | `#F1F4F9` | `#0D141B` | bg |
| surfaceContainerLow | `#FFFFFF` | `#182029` | card |
| surfaceContainer | `#FFFFFF` | `#1E2833` | tabBg |
| surfaceContainerHigh | `#FFFFFF` | `#182029` | card |
| surfaceContainerHighest | `#E6EBF3` | `#252F39` | fill |

`*Fixed` 系列角色不覆写,沿用基础配色。

### 4.3 刻意的取舍

- **浅色模式 surfaceContainer 系列不单调递增**:Lowest 为雾蓝灰,Low/High/Container 为白,Highest 又是灰。这对应「灰底白卡」的层次。深色模式仍单调递增。
- **tertiary 系列不是第三种强调色**:设计稿的评分、排名等数字用 label,选中态用 tint。读 tertiary 的现有组件(评分数字与星星、弹幕源 FilterChip、选择资料来源摘要等)因此显示为中性文字与 tint 淡底,不出现 materialkolor 由 accent 推出的其他色相。
- **`surfaceTint` 与 `surface` 同值**:关闭 M3 色调高度,带 `tonalElevation` 的 `Surface` 不会泛出强调色。不要设为 `Color.Transparent`(`surfaceColorAtElevation` 合成时会把表面压暗)。
- **M3 组件默认值受到的影响**(预期内,组件按 02 改样式时处理):
  - 未传颜色的 `Card` 默认 `surfaceContainerHighest`,为灰卡。要白卡用 `AniThemeDefaults.primaryCardColors()`。
  - 深色下 M3 默认 `Button` 为浅蓝底 `#76AEDD` 配深字,对比度合格,但与设计稿的 accentFill 不同;按 02 C8 改。
  - M3 默认 `Switch` 开启轨道为 primary,深色下与设计稿的 accentFill 不同;按 02 C7 改。
  - `DropdownMenu`(`surfaceContainer`)为 tabBg。
  - `ModalBottomSheet`(`surfaceContainerLow`)为 card;设计稿的底部面板底色是 bg,按 02 C14 传色。

## 5. 取色约定与新增 `AniColors`

### 5.1 每个 token 只有一个取法

Apple 版 Composable 取色**只用下表的取法**。同值的角色(例如 card 也等于 `surfaceContainerHigh`)不要混用,以免高对比度深色主题或之后调整映射时出现不一致。

| Token | 取法 | 说明 |
|---|---|---|
| bg | `colorScheme.surfaceContainerLowest` | 高对比度深色主题下变纯黑,符合预期 |
| card | `colorScheme.surfaceContainerLow` | 不用 `surfaceContainerHigh` |
| tabBg | `colorScheme.surfaceContainer` | |
| fill | `colorScheme.surfaceContainerHighest` | 不用 `surfaceVariant`(高对比度深色主题会把它改成黑色) |
| separator | `colorScheme.outlineVariant` | |
| label | `colorScheme.onSurface` | |
| labelSoft | `AniColors.labelSoft` | |
| label2 | `colorScheme.secondary` | 不用 `onSurfaceVariant`(高对比度深色主题会把它改成白色,与 label 分不开) |
| accent | `colorScheme.primary` | |
| tint | `colorScheme.secondaryContainer` | 与 `primaryContainer` 同值;M3 导航指示器、FilterChip 默认读 `secondaryContainer` |
| onTint | `colorScheme.onSecondaryContainer` | |
| accentFill、onAccentFill、alert | `AniColors.accentFill`、`AniColors.onAccentFill`、`AniColors.alert` | |
| error | `colorScheme.error` | |
| floatingShadow | `AniColors.floatingShadowColor` + `AniColors.floatingBorder` | 深色才有描边 |
| sidebar、sidebarField、hover | `AniColors.sidebar`、`AniColors.sidebarField`、`AniColors.hover` | 仅 macOS |

`modifyColorSchemeForBlackBackground` 会改写 background、onBackground、surface、onSurface、surfaceContainerLowest、surfaceVariant、onSurfaceVariant。上表因此避开 `surfaceVariant` 与 `onSurfaceVariant`。

### 5.2 新增 `AniColors`(M3 没有的角色)

路径:`app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/AniColors.kt`

只读的 token 集合,从当前 `MaterialTheme.colorScheme` 推出,**不需要改 `AniTheme`、不新增 CompositionLocal**。

```kotlin
/**
 * Material 3 没有对应角色的 Ani 颜色 token. 取值规则见 docs/design/ani-style/01-tokens.md §5.
 */
object AniColors {
    val labelSoft: Color @Composable @ReadOnlyComposable get()
    val accentFill: Color @Composable @ReadOnlyComposable get()
    val onAccentFill: Color @Composable @ReadOnlyComposable get()
    val alert: Color @Composable @ReadOnlyComposable get()
    val floatingShadowColor: Color @Composable @ReadOnlyComposable get()
    val floatingBorder: Color @Composable @ReadOnlyComposable get() // 浅色 Color.Transparent, 深色 separator
    val sidebar: Color @Composable @ReadOnlyComposable get()
    val sidebarField: Color @Composable @ReadOnlyComposable get()
    val hover: Color @Composable @ReadOnlyComposable get()
    val brandSlate: Color   // #2F3943
    val brandRing: Color    // #7EBBED
}
```

取值规则:

- 深浅判断:`MaterialTheme.colorScheme.surfaceContainerLowest.luminance() < 0.5f`。
- `labelSoft`、`alert`、`sidebar`、`sidebarField`、`hover`、`floatingShadowColor`:按深浅取 §2 的固定值。
- `accentFill`:浅色 = `colorScheme.primary`;深色时,`colorScheme.primary == #76AEDD`(默认品牌值)取 `#2F78A9`,否则取 `Hct.from(hue(primary), chroma(primary), 48.0)`。HCT 色调 48 与白色的对比度对任意色相都约为 4.8,保证白字可读。
- **品牌值判断用 `primary`**:`AniColors` 只读 `colorScheme`,拿不到种子色,所以用 `primary` 是否等于品牌值(浅色 `#3072A1`、深色 `#76AEDD`)来判断。`aniAccent` 是纯函数、拿得到种子色,仍按 §6.1 用 `seedColor == DefaultSeedColor` 判断。
- `onAccentFill`:`Color.White`。
- `AniColors` 只供 Apple 版 Composable 使用;其他平台的既有画面不读取它。

## 6. 强调色推导

### 6.1 规则

- `seedColor == DefaultSeedColor`(`#4F378B`,用户没改过主题色):使用 §2.1 的品牌固定值。**不修改 `DefaultSeedColor` 常量**,避免已保存设置的迁移问题。
- 其他种子色(设置里的 10 个预设色、条目页动态主题取到的封面色):`hue` 取种子色的 HCT 色相。

| 输出 | 浅色 | 深色 |
|---|---|---|
| accent | `Hct.from(hue, 48.0, 46.0)` | `Hct.from(hue, 36.0, 70.0)` |
| tint | `mixSrgb(accent, #FFFFFF, 0.12)` | `mixSrgb(accent, card, 0.18)` |
| onTint | `Hct.from(hue, 40.0, 30.0)` | `Hct.from(hue, 24.0, 87.0)` |
| inverse | 深色 accent | `Hct.from(hue, 48.0, 40.0)` |
| onPrimary | `#FFFFFF` | `#0D141B` |

- 色调(tone)即 L*,对比度只由色调决定:浅色 accent 色调 46 在 bg 上约 4.69、在白色上约 5.2;深色 accent 色调 70 在 card 上约 7.2。chroma 超出色域时 HCT 会自动收敛,任意种子色(含灰色)都成立。
- `mixSrgb(top, bottom, f)`:在 sRGB 通道上线性混合 `top * f + bottom * (1 - f)`。**不要用 Compose 的 `lerp(Color, Color, Float)`**(它在 Oklab 空间插值)。
- 中性色(bg、card、fill、separator、label 系列、error)不随种子色改变。

## 7. 字级(新增 `AniTextStyles`)

路径:`app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/AniTextStyles.kt`

新增只读的 `TextStyle` 集合,供按 02–04 改样式的组件使用。**不修改 `MaterialTheme.typography`**。数值为「字号/行高 字重」,单位 sp。

实现方式:

```kotlin
object AniTextStyles {
    val largeTitle: TextStyle @Composable @ReadOnlyComposable get() = style(34, 41, FontWeight.ExtraBold, letterSpacing = -0.5)
    // 其余同 §7.1、§7.2
}

@Composable
@ReadOnlyComposable
private fun style(size: Int, lineHeight: Int, weight: FontWeight, letterSpacing: Double = 0.0): TextStyle =
    TextStyle(
        fontFamily = LocalPlatformFontFamily.current.defaultFontFamily, // 为 null 时不指定,沿用默认字体
        fontSize = size.sp, lineHeight = lineHeight.sp, fontWeight = weight,
        letterSpacing = letterSpacing.sp,
        // 首行上方与末行下方保留行距, 单行文字高度等于行高, 与设计稿的 CSS 行高一致
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    )
```

- 每个样式都是 `@Composable` getter,因为字体族要从 `LocalPlatformFontFamily` 读。
- `defaultFontFamily` 为 `null` 时 `TextStyle` 不指定字体族,由系统默认字体显示。
- 签名与 import 以编译为准。

### 7.1 手机

| 名称 | 规格 | 用途 |
|---|---|---|
| largeTitle | 34/41 W800,字距 -0.5 | 分页大标题(追番、探索、我的) |
| title | 20/26 W700 | 区块标题(继续观看、选集) |
| headline | 17/22 W600 | 卡片主标题、推入页标题 |
| body | 16/22 W400 | 列表行标题(W600)、设定行 |
| callout | 15/20 W400 | 链接文字、按钮文字 |
| subhead | 14/20 W400 | 胶囊文字、次要标题 |
| footnote | 13/18 W400 | 元信息、分组标题 |
| caption | 12/16 W400 | 辅助说明、数字标注 |
| tabLabel | 10/12 W500(选中 W700) | 浮动导航栏文字 |

### 7.2 macOS

| 名称 | 规格 | 用途 |
|---|---|---|
| macLargeTitle | 28/34 W800,字距 -0.4 | 页面大标题 |
| macTitle | 17/22 W700 | 区块标题 |
| macWindowTitle | 15/20 W600 | 设定窗格标题、小窗口标题 |
| macBody | 14/20 W400 | 表格行标题(W600)、侧边栏项目 |
| macCallout | 13/18 W400 | 表格次要列、按钮、胶囊 |
| macCaption | 12/16 W400 | 表头、辅助说明 |
| macSidebarSection | 11/14 W600 | 侧边栏分组标题 |

数字列用等宽数字(`fontFeatureSettings = "tnum"`)。

## 8. 间距、圆角、尺寸

| 项目 | 手机 | macOS |
|---|---|---|
| 栅格 | 8 | 8 |
| 页面左右内距 | 16 | 内容区 32;窗格 28 |
| 区块间距 | 24 | 28 |
| 标题到内容 | 12 | 12 |
| 卡片圆角 | 16(主视觉卡 22) | 14 |
| 胶囊 | 高 32、圆角 16 | 高 28、圆角 14 |
| 控件 | 圆形按钮 36/40 | 高 28、圆角 6 |
| 主要按钮 | 高 44–50、全圆角(登录流程满版按钮 50) | 高 28、圆角 6(小窗口内满版按钮高 36、圆角 8) |
| 列表行 | 48(设定)、52–76(内容) | 44–48(设定)、52–76(表格) |
| 封面圆角 | 8(列表)、10(网格)、12(推荐卡) | 6(表格)、10(网格) |
| 窗口圆角 | — | 12 |

## 9. 代码改动

### 9.1 新增 `AniColorScheme.kt`(必做)

路径:`app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/AniColorScheme.kt`

```kotlin
/**
 * Ani 配色: 背景/卡片/导航为中性石板色, 强调色由主题色推导.
 * 角色映射与色值见 docs/design/ani-style/01-tokens.md.
 */
fun aniColorScheme(seedColor: Color, isDark: Boolean): ColorScheme {
    val a = aniAccent(seedColor, isDark)
    val n = if (isDark) AniNeutrals.Dark else AniNeutrals.Light
    val base = dynamicColorScheme(
        primary = a.accent,
        isDark = isDark,
        isAmoled = false,
        style = PaletteStyle.TonalSpot,
    )
    return base.copy(
        // §4.2 每个非 * 角色取 n, * 角色取 a; `*Fixed` 系列不覆写
    )
}

@Immutable
data class AniAccent(
    val accent: Color, val onAccent: Color,
    val tint: Color, val onTint: Color,
    val inverse: Color,
)

/** 按 §6 推导. [DefaultSeedColor] 返回品牌固定值. */
fun aniAccent(seedColor: Color, isDark: Boolean): AniAccent
```

- 纯函数,不读 `CompositionLocal`,便于测试。
- 签名与 import 以编译为准。

### 9.2 新增 `Platform.isApple()`(必做)

路径:`utils/platform/src/commonMain/kotlin/Platform.kt`,和现有的 `isIos()`、`isMacOS()` 写在一起:

```kotlin
inline fun Platform.isApple(): Boolean = isIos() || isMacOS()
```

### 9.3 Apple 平台的 `appColorScheme`(必做)

| 文件 | 改法 |
|---|---|
| `ui-foundation/src/iosMain/.../AppTheme.ios.kt` | `remember` 内改为 `modifyColorSchemeForBlackBackground(aniColorScheme(seedColor, isDark), isDark, useBlackBackground)` |
| `ui-foundation/src/desktopMain/.../AppTheme.desktop.kt` | 开头按平台分支:`LocalPlatform.current.isMacOS()` 时返回 `remember(seedColor, isDark, useBlackBackground) { modifyColorSchemeForBlackBackground(aniColorScheme(seedColor, isDark), isDark, useBlackBackground) }`;Windows/Linux 走原逻辑(含系统强调色) |
| `ui-foundation/src/androidMain/.../AppTheme.android.kt` | **不改** |

### 9.4 条目页动态主题(必做,G17)

`app/shared/ui-foundation/src/commonMain/kotlin/ui/foundation/theme/MaterialThemeFromPaletteAndImage.kt`:Apple 平台(`LocalPlatform.current.isApple()`,在 `LaunchedEffect` 外先读出)把 `dynamicColorScheme(...)` 换成 `aniColorScheme(封面色, isDark)`,强调色跟随封面,背景维持中性色;其他平台不变。

`useBlackBackground`(高对比度深色主题)保留,继续由现有的 `modifyColorSchemeForBlackBackground` 处理。

### 9.5 新增 `AniColors.kt`、`AniTextStyles.kt`(必做)

见 §5、§7。

### 9.6 主题色色票(随 03 §5.2、04 §5.2 的主题页实现)

- **新增** Apple 版圆形色票组件(30 圆,选中加双圈描边,见 03 §5.2),显示推导后的强调色 `aniAccent(seed, isDark).accent`;默认色因此显示为品牌蓝。
- iOS 与 macOS 的主题设置在调用处按 `isApple()` 选用它;原 `ColorButton.kt`(`app/shared/ui-settings/src/commonMain/kotlin/ui/settings/tabs/theme/ColorButton.kt`)**不改**,Android、Windows、Linux 照旧。
- 色票的数量与顺序照 `AniThemeDefaults.themeColorOptions`(`ui-foundation/.../ui/theme/ThemeColorOptions.kt`,共 11 个,默认 `DefaultSeedColor` 是第 6 个),不改顺序。
- **存储值与选中判断仍用种子色**,只改显示。

### 9.7 不需要改的地方

- `AniTheme`、`AniThemeDefaults`、`modifyColorSchemeForBlackBackground`。
- `ThemeSettings` 的结构与默认值、`DefaultSeedColor`、`themeColorOptions` 的内容与顺序、`ColorButton.kt`。
- Android 的 `AppTheme.android.kt`;Windows/Linux 的配色分支。
- TV 的 `AniTvTheme` / `TvColorMapping`。

## 10. 测试

新增 `app/shared/ui-foundation/src/commonTest/kotlin/ui/foundation/theme/AniColorSchemeTest.kt`:

1. `default seed uses brand colors in light scheme`、`default seed uses brand colors in dark scheme`:§4.2 每个角色(`*Fixed` 系列除外)与期望值相等。
2. `derived schemes meet contrast requirements`:对 `AniThemeDefaults.themeColorOptions` 全部种子色,加 `#808080`、`#FFFF00`、`#0000FF`、`#FF0000`,浅色深色各验证:
   - `primary` vs `surfaceContainerLowest` ≥ 4.5,`primary` vs `surfaceContainerLow` ≥ 4.5
   - `onPrimary` vs `primary` ≥ 4.5
   - `onSecondaryContainer` vs `secondaryContainer` ≥ 4.5
   - `tertiary` vs `surfaceContainerLowest` ≥ 4.5,`onTertiary` vs `tertiary` ≥ 4.5,`onTertiaryContainer` vs `tertiaryContainer` ≥ 4.5
   - `onSurfaceVariant` vs `surfaceContainerHighest` ≥ 4.5
   - `inversePrimary` vs `inverseSurface` ≥ 4.5
   - `outline` vs `surfaceContainerLowest` ≥ 3.0
3. `accent fill keeps white text readable`:上述种子色在深色下由 §5 规则得到的 accentFill,与白色对比 ≥ 4.5。
4. `surface tint equals surface`。

运行 `./gradlew :app:shared:ui-foundation:desktopTest`;另编译一次 iOS 目标,并确认 Android 目标仍能编译。

## 11. 已知限制

- 写死的颜色(`Color(0x…)`)不跟随本方案;按 02–04 改到对应页面时一并换成 token,其余在 PR 中记录。
- 浅色 accent 放在 fill 上为 4.3:1,只用于图标,不用于小字。
