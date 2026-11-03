# 参与 Shi

感谢你愿意改进 Shi。项目优先考虑本地数据可靠性、安静克制的交互，以及 Android 10+ 设备上的可用性。

## 开始之前

- Bug 请通过 Issue 表单提交版本、Android/API、设备、复现步骤和截图。
- 新功能或较大的交互调整，建议先开 Issue 对齐方向，避免做成通用时间 Dashboard。
- 不要在 Issue、PR 或日志中上传含私人备注的 JSON 备份；演示数据请先脱敏。

## 本地验证

前置：JDK 17、Android SDK Platform 36、Build Tools 36.0.0。

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Windows 使用 `gradlew.bat`。涉及数据库迁移时，请同时用旧版本数据做覆盖安装验证；涉及界面时，请附前后截图，并检查深色模式与窄屏。

## 提交与 PR

- 沿用 Conventional Commits，例如 `feat: ...`、`fix: ...`、`docs: ...`、`chore(release): ...`。
- 保持改动聚焦，不在同一 PR 中混入无关格式化或依赖升级。
- PR 中说明动机、实现、验证结果和未覆盖风险；不要只写“已测试”。
- 不要提交 `local.properties`、`keystore.properties`、keystore、个人备份或模拟器数据。

## 设计约定

保留纸、墨、松绿和节气色板的识别度。优先保证记录效率、文字对比度、至少 48dp 的触控区域、明确的错误恢复，以及未保存数据的保护。
