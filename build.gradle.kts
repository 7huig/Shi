// 根构建脚本只锁定插件版本，具体配置留在 :app 模块。
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
