plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.miaowadi.facaiqianmian"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.miaowadi.facaiqianmian"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "SUPABASE_URL", "\"https://pxoynbyxknoebbtcceao.supabase.co\"")
        buildConfigField("String", "SUPABASE_PUBLISHABLE_KEY", "\"sb_publishable_kSrKiAc4GVuGx-X3fXQdLw_8o38zMEc\"")
        buildConfigField("String", "BACKEND_URL", "\"https://miaowadi-backend.onrender.com\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
