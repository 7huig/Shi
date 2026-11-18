# 验收清单与常见问题

[← 返回 README](../README.md)

## 手动验收清单

以下均在 API 31 Pixel 6 模拟器（1080×2400）上使用 debug 包实际点按验证；release 包另做 R8 构建与签名校验。

- [x] 应用名叫「時」，桌面图标与顶栏一致；顶栏两个页签「時記」「時計」同款 23sp 衬线，选中为墨色并带 2dp 指示条，未选为次要色。
- [x] 时间轴页没有整条「完成」按钮，右下角是 62dp 松绿圆圈 + 主题前景色加号；点击打开「记录一段」，长按打开「快速记录」。
- [x] 主界面时间 13sp、分类路径 16sp；日期标题为「今天 · M月d日 / 昨天 · M月d日 / M月d日」并配「周X」副标题，与统计页日期语法一致。
- [x] 左侧时分标签的中心正落在相邻两个事件之间（首段保持顶部对齐）。
- [x] 点记录块内空白区域也能打开编辑；块内每条记录仍可单独点按。
- [x] 记录弹窗以「一级 / 二级 / 三级」三列依次选择分类，顶部显示完整路径，点任意一级即把记录记到该级；三列各自滚动并自动定位选中项。明确标注「开始 / 结束」并实时显示时长；底部「取消 / 保存」固定，内容区独立滚动，标题右侧可关闭；编辑页可删除记录。长空档默认只预填最近 30 分钟，并提供「补齐整段空档」。
- [x] 分类编辑弹窗为 4 行 × 6 列节气色板：春 / 夏 / 秋 / 冬 四行，每色下方标注节气名，选中色块有描边。
- [x] 统计页顶栏为 日 / 周 / 月 / 年 / 自定义 五个 chip；左右箭头可查看前一个 / 后一个周期（当前为「今天」时右箭头禁用）。
- [x] 三层环图：内、中、外三圈分别对应根 / 子 / 孙分类；点弧段后中心显示分类详情与占比，对应根分类行同步高亮。
- [x] 空周期直接显示「这段时间还没有记录」与返回记录提示，不再画一个占半屏的空环。
- [x] 月视图、年视图、自定义视图均显示对应数据；自定义区间一次弹出起止日期范围选择器。
- [x] 分类用时行：分类名右侧「N次」与占比，时长左侧环比——增加红色、减少绿色并带 ▲▼，持平「0%」灰色，上一周期无记录显示中性色「新增」。
- [x] 统计页顶部显示「已记录 N%」与「覆盖时长 / 已过时长」，重叠记录按并集只算一次。
- [x] 单条记录长按删除后，底部 Snackbar 可在提示条消失前撤销；删除分类时可选择保留事件并移到父级 / 未分类。
- [x] 导出文件名为 `時-yyyy-MM-dd.json`（schema 2，兼容导入 schema 1）。
- [x] 深色模式下环图、环比红绿、色板、FAB 均正确。
- [x] 分类页提示语为「点箭头展开或收起；按住右侧手柄上下拖动，可调整同级顺序。最多 3 层；删除分类时可以选择一并删除事件，或保留事件并移到父级。」
- [x] 分类页默认全部收起，只显示根分类与「· N」子级数量；点行内箭头展开 / 收起（200ms 旋转动画），顶栏「全部展开 / 全部收起」一键切换；在收起的父分类上点「+」新建子分类时会自动展开父分类。
- [x] 按住某行右侧三横线手柄上下拖动，被拖行浮起跟手、其余行平滑让位，指针越过相邻行中心 6dp 才换位（临界点不抖动）；松手后顺序写入数据库，杀掉进程重进仍然保留。
- [x] 「時計」中点分类行主体只选中，点行尾箭头进入下一级统计；面包屑「全部分类 / …」可逐级返回。
- [x] 搜索结果顶部显示「找到 N 条记录」与「共 X 小时」的总时长；备注中的命中词高亮。
- [x] 旧版本数据库升级到 v5：原记录完整，自动补全事件 UUID；覆盖安装后启动与数据展示正常。
- [x] `am force-stop` 后重启，数据完整保留。
- [x] `./gradlew testDebugUnitTest assembleDebug --console=plain` 全绿（Migration 4 + CategoryDeletion 2 + TransferJson 21 + CategoryCascade 8 + CategoryVisibility 7 + LegacyEventUuid 3 + OverlapResolver 12 + Statistics 39 + TimelineBuilder 8 + DragMath 15 = 119 例）。
- [x] `./gradlew assembleRelease` 产物已签名（R8 压缩 + 资源收缩），签名证书与 SHA-256 已核对。

## 常见问题

### 数据存在哪？卸载会丢吗？

全部数据在应用私有的 Room / SQLite 数据库 `time-log.db`（Android 的 `/data/data/io.github.huig7.shi/databases/`）里。没有云端、没有账号，**卸载应用或清除数据会一并删除**；换机、刷机前请从顶栏「⋮ → 数据备份」或统计页底部导出 JSON。

### 真的不联网吗？

是的。AndroidManifest 中没有任何 `uses-permission`（包括 `INTERNET`），代码里也没有网络请求。导入 / 导出走系统文件选择器（SAF），不申请存储权限。

### 为什么 targetSdk 还是 31？

这是一个自用为主的项目，targetSdk 31 在 Android 10–16 上都能正常侧载安装和运行。若要上架 Google Play，需要按商店政策升级到 API 36 并重新验证行为变更；欢迎提 PR。

### 包名为什么是 `io.github.huig7.shi`？

采用开源项目常见的反向域名写法（`io.github.huig7.shi`），不再使用 `com.example` 这类占位前缀。因为 Java 包名的每一段不能以数字开头，用户名 `7huig` 不能直接作为包名段，所以写成 `huig7`。如果 fork 后要改成自己的包名，需要同步改 `namespace`、`applicationId` 与源码目录，属于破坏性变更：已安装应用的数据不会迁移，相当于一个新应用。

### release 包为什么比 debug 小这么多？

debug 包没有做代码裁剪，还带 Compose 的 tooling 依赖，未压缩 dex 约 28 MB；release 开启 R8（`isMinifyEnabled = true`）与 resource shrink（`isShrinkResources = true`），APK 约 2.9 MB。体积几乎全部来自 Kotlin / Compose / AndroidX 的 dex，资源本身只有约 0.4 MB。

### release 签名怎么配置？密钥丢了怎么办？

在仓库根目录新建 `keystore.properties`（已被 `.gitignore` 排除）：

```properties
storeFile=release-keystore.jks
storePassword=你的密码
keyAlias=shi
keyPassword=你的密码
```

并把 `release-keystore.jks` 放在仓库根目录。**keystore 与密码务必离线备份**：Android 要求覆盖安装 / 升级必须使用同一签名，密钥丢失后只能更换包名重新发布。

### 怎么跑测试？有 UI 测试吗？

目前只有 JVM 单元测试，覆盖时间分块、重叠求解、统计与覆盖率、旧备份 UUID、三级分类级联、分类删除迁移、分类展开可见性与拖拽落点数学等纯逻辑：

```bash
./gradlew testDebugUnitTest
```

共 119 例（Migration 4 + CategoryDeletion 2 + TransferJson 21 + CategoryCascade 8 + CategoryVisibility 7 + LegacyEventUuid 3 + OverlapResolver 12 + Statistics 39 + TimelineBuilder 8 + DragMath 15）。没有 instrumentation / Compose UI 测试，交互验收依赖上面的手动清单。

### 图标是用什么做的？字体许可？

`design/icons/render.py` 用 Pillow 以 4× 超采样绘制唯一的「墨時」方案，`build_launcher_assets.py` 直接由字体生成 5 档自适应图标前景。字体为 **Noto Serif SC**，采用 [SIL Open Font License 1.1](https://openfontlicense.org/)，可自由使用与再分发；字体文件本身不随仓库分发，脚本从系统字体目录加载。前景几何按 66dp 安全圆程序化校验（墨迹四角到中心 26.68dp），字形不会被 launcher 遮罩切到。

### 截图是怎么生成的？

在模拟器 / 真机上截取 1080×2400 的 PNG，再等比缩到 720px 宽、转成 WebP（quality 82）放进 `docs/screenshots/`，仓库里只保留 `.webp`，避免 README 加载几 MB 的 PNG。
