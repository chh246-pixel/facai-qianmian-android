# 发财千面 Android App 基础框架

作者：妙瓦底阿森

## 当前功能
- 中文首页与后端健康检查
- Supabase 邮箱注册/登录
- 登录后调用现有 Render `/api/create-order` 创建月套餐测试订单
- GitHub Actions 自动构建调试版 APK

## 构建前必须配置
打开 `app/build.gradle.kts`，已预填现有客户端中找到的 Supabase Project URL 与 Publishable Key。首次构建前，请在 Supabase 后台确认它们仍属于你当前使用的项目。

绝对不要填写 `service_role`、`sb_secret_...` 或任何服务端密钥。服务端密钥只保存在 Render。

当前版本只是联调基础框架；套餐暂时固定为 monthly，尚未完成订单状态刷新与支付扫描验证。请勿向任何订单地址转账。

## 生成 APK（不需要本地 Android Studio）
1. 上传本项目文件到一个新的 GitHub 仓库，例如 `facai-qianmian-android`。
2. 打开仓库 Actions，选择 `Build Android APK` 工作流并运行（或等待 push 自动运行）。
3. 打开成功的运行记录，在 Artifacts 下载 `miaowadi-debug-apk`。
4. 解压得到 `app-debug.apk`。这是调试签名 APK，用于安装测试，不是 Google Play 正式发布版本。
