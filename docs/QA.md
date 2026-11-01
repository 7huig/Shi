# 验收清单与常见问题

[← 返回 README](../README.md)

## 手动验收清单

以下均在 API 31 Pixel 6 模拟器（1080×2400）上实际点按验证；release 包（R8 + resource shrink）另在 API 31 模拟器上安装启动验证。

- [x] 应用名叫「時」，桌面图标与顶栏一致；顶栏两个页签「時記」「時計」同款 23sp 衬线，选中为墨色、未选为次要色。
- [x] 时间轴页没有整条「完成」按钮，右下角是 62dp 蓝底圆圈 + 白色加号；点击打开「记录一段」。
- [x] 主界面时间 13sp、分类路径 14sp；日期标题为「今天 / 昨天 / M月d日」，不显示年份，星期只在副标题小字里（如「11月1日 · 周日」）。
- [x] 点记录块内空白区域也能打开编辑；块内每条记录仍可单独点按。
- [x] 分类编辑弹窗为 4 行 × 6 列节气色板：春 / 夏 / 秋 / 冬 四行，每色下方标注节气名，选中色块有描边。
- [x] 统计页顶栏为 日 / 周 / 月 / 年 / 自定义 五个 chip；左右箭头可查看前一个 / 后一个周期（当前为「今天」时右箭头禁用）。
- [x] 周视图三层环图：外、中、内三圈分别对应根 / 子 / 孙分类，中心显示总时长与记录条数。
- [x] 空周期显示浅色空轨道 + 「—」+「这段时间还没有记录」。
- [x] 月视图、年视图、自定义视图均显示对应数据；点「修改」弹出起始日 DatePicker。
- [x] 分类用时行：分类名右侧「N次」，时长左侧环比——增加红色、减少绿色、持平「0%」灰色、上一周期无记录显示红色「新增」。
- [x] 导出文件名为 `時-yyyy-MM-dd.json`（schema 1）。
- [x] 深色模式下环图、环比红绿、色板、FAB 均正确。
- [x] 分类页提示语为「分类最多 3 层。删除分类时，其中的事件会一并删除。」
- [x] 旧版本数据库（v3）升级到 v4：默认分类换新色、用户自定义色保留、记录完整。
- [x] `am force-stop` 后重启，数据完整保留。
- [x] `./gradlew testDebugUnitTest assembleDebug --console=plain --offline` 全绿（TimelineBuilder 7 + OverlapResolver 12 + Statistics 26 = 45 例）。
- [x] `./gradlew assembleRelease` 产物已签名（R8 压缩 + 资源收缩），安装后启动、空态、记录弹窗正常。

## 常见问题

### 数据存在哪？卸载会丢吗？

全部数据在应用私有的 Room / SQLite 数据库 `time-log.db`（Android 的 `/data/data/io.github.huig7.shi/databases/`）里。没有云端、没有账号，**卸载应用或清除数据会一并删除**；换机、刷机前请先在统计页底部的「数据」卡片导出 JSON。

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

目前只有 JVM 单元测试，覆盖时间分块、重叠求解与统计三类纯函数：

```bash
./gradlew testDebugUnitTest
```

共 45 例。没有 instrumentation / Compose UI 测试，交互验收依赖上面的手动清单。

### 图标是用什么做的？字体许可？

`design/icons/render.py` 用 Pillow 以 4× 超采样绘制 5 个方向，选定 A（墨時）后由 `build_launcher_assets.py` 生成自适应图标前景。字体为 **Noto Serif SC / Noto Sans SC**，采用 [SIL Open Font License 1.1](https://openfontlicense.org/)，可自由使用与再分发；字体文件本身不随仓库分发，脚本从系统字体目录加载。

### 截图是怎么生成的？

在模拟器 / 真机上截取 1080×2400 的 PNG，再等比缩到 720px 宽、转成 WebP（quality 82）放进 `docs/screenshots/`，仓库里只保留 `.webp`，避免 README 加载几 MB 的 PNG。
