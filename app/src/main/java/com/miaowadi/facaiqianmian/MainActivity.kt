package com.miaowadi.facaiqianmian

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private var accessToken: String? = null
    private var userEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showHome() }

    private fun layout(title: String): LinearLayout {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(28), dp(22), dp(22)); setBackgroundColor(Color.rgb(12,18,32)) }
        val scroll = ScrollView(this); scroll.addView(root); setContentView(scroll)
        text(root, title, 27f, Color.WHITE)
        return root
    }
    private fun showHome() {
        val box = layout("发财千面")
        text(box, "作者：妙瓦底阿森", 16f, Color.LTGRAY)
        text(box, "使用现有 Render 后端与 Supabase 登录。支付功能在完成链上验证前不会开放。", 14f, Color.rgb(190,200,220))
        if (accessToken == null) button(box, "登录 / 注册") { showAuth() }
        else {
            text(box, "当前账号：$userEmail", 15f, Color.WHITE)
            button(box, "创建月套餐测试订单") { createOrder() }
            button(box, "退出登录") { accessToken = null; userEmail = ""; showHome() }
        }
        button(box, "检查后端连接") {
            request("GET", "${BuildConfig.BACKEND_URL}/health", null, null) { ok, body -> toast(if (ok) "后端正常：$body" else "连接失败：$body") }
        }
        text(box, "首版测试框架；请勿向订单地址转账。", 12f, Color.rgb(255,190,90))
    }
    private fun showAuth() {
        val box = layout("账号登录")
        val email = edit(box, "邮箱")
        val password = edit(box, "密码", true)
        text(box, "作者：妙瓦底阿森", 12f, Color.LTGRAY)
        val status = text(box, "", 13f, Color.YELLOW)
        button(box, "登录") { auth(email.text.toString().trim(), password.text.toString(), false, status) }
        button(box, "注册新账号") { auth(email.text.toString().trim(), password.text.toString(), true, status) }
        button(box, "返回") { showHome() }
    }
    private fun auth(email: String, password: String, register: Boolean, status: TextView) {
        if (email.isBlank() || password.length < 6) { status.text = "请输入有效邮箱，密码至少 6 位。"; return }
        if (BuildConfig.SUPABASE_URL.contains("YOUR_PROJECT") || BuildConfig.SUPABASE_PUBLISHABLE_KEY.contains("YOUR_SUPABASE")) {
            status.text = "请先在 app/build.gradle.kts 配置 Supabase Project URL 和 Publishable Key。"; return
        }
        val path = if (register) "/auth/v1/signup" else "/auth/v1/token?grant_type=password"
        request("POST", BuildConfig.SUPABASE_URL + path, JSONObject().put("email", email).put("password", password).toString(),
            mapOf("apikey" to BuildConfig.SUPABASE_PUBLISHABLE_KEY)) { ok, response ->
            if (ok) {
                val json = runCatching { JSONObject(response) }.getOrNull()
                val token = json?.optString("access_token").orEmpty()
                if (token.isNotBlank()) { accessToken = token; userEmail = email; showHome() }
                else if (register) status.text = "注册请求已提交。如开启邮箱验证，请先验证邮箱再登录。"
                else status.text = "未取得登录令牌：$response"
            } else status.text = "请求失败：$response"
        }
    }
    private fun createOrder() {
        val token = accessToken ?: run { showAuth(); return }
        request("POST", "${BuildConfig.BACKEND_URL}/api/create-order", JSONObject().put("plan_code","monthly").toString(),
            mapOf("Authorization" to "Bearer $token", "Content-Type" to "application/json")) { ok, response ->
            val box = layout("订单测试结果")
            val j = runCatching { JSONObject(response) }.getOrNull()
            if (ok && j != null) {
                text(box, "金额：${j.optString("amount_usdt")} USDT", 24f, Color.rgb(90,220,170))
                text(box, "订单号：${j.optString("order_id")}", 13f, Color.WHITE)
                text(box, "收款地址：${j.optString("receive_address")}", 13f, Color.WHITE)
                text(box, "状态：${j.optString("status")}", 14f, Color.LTGRAY)
                text(box, "仅用于联调，支付扫描尚未验证，请勿转账。", 13f, Color.YELLOW)
            } else text(box, "创建失败：$response", 14f, Color.YELLOW)
            button(box, "返回首页") { showHome() }
        }
    }
    private fun edit(parent: LinearLayout, hint: String, password: Boolean = false): EditText {
        val e = EditText(this).apply { this.hint = hint; textSize = 16f; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY); setPadding(dp(12),dp(10),dp(12),dp(10)); setBackgroundColor(Color.rgb(30,39,57))
            inputType = if (password) android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD else if (hint=="邮箱") android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS else android.text.InputType.TYPE_CLASS_TEXT
        }
        val lp = LinearLayout.LayoutParams(-1, dp(52)); lp.bottomMargin = dp(12); parent.addView(e, lp); return e
    }
    private fun text(parent: LinearLayout, value: String, size: Float, color: Int): TextView {
        val tv = TextView(this).apply { text=value; textSize=size; setTextColor(color); setPadding(0,dp(8),0,dp(8)) }; parent.addView(tv); return tv
    }
    private fun button(parent: LinearLayout, label: String, action: () -> Unit) {
        val b = Button(this).apply { text=label; setOnClickListener { action() } }
        val lp=LinearLayout.LayoutParams(-1,dp(52)); lp.bottomMargin=dp(10); parent.addView(b,lp)
    }
    private fun request(method: String, url: String, body: String?, headers: Map<String,String>?, done: (Boolean,String)->Unit) {
        thread {
            var c: HttpURLConnection? = null
            val result = try {
                c = URL(url).openConnection() as HttpURLConnection
                c.requestMethod=method; c.connectTimeout=15000; c.readTimeout=20000
                headers?.forEach { (k,v) -> c.setRequestProperty(k,v) }
                if (body != null) { c.doOutput=true; c.setRequestProperty("Content-Type","application/json"); c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) } }
                val code=c.responseCode; val stream=if(code in 200..299) c.inputStream else c.errorStream
                Pair(code in 200..299, stream?.bufferedReader()?.use { it.readText() } ?: "")
            } catch (e: Exception) { Pair(false,e.message ?: "网络错误") } finally { c?.disconnect() }
            runOnUiThread { done(result.first,result.second) }
        }
    }
    private fun toast(s: String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()
    private fun dp(v: Int) = (v*resources.displayMetrics.density).toInt()
}
