package com.noobexon.xposedfakelocation.xposed.hooks

import android.telephony.CellInfo
import android.telephony.NeighboringCellInfo
import com.noobexon.xposedfakelocation.xposed.utils.LocationUtil
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

class PhoneServicesHooks(private val appLpparam: LoadPackageParam) {
    private val tag = "[PhoneServicesHooks]"

    fun initHooks() {
        val phoneInterfaceManagerClass = findClass(
            appLpparam.classLoader,
            "com.android.phone.PhoneInterfaceManager"
        ) ?: return

        hookCellLocation(phoneInterfaceManagerClass)
        hookCellInfo(phoneInterfaceManagerClass)
        XposedBridge.log("$tag Instantiated hooks successfully")
    }

    private fun hookCellLocation(phoneInterfaceManagerClass: Class<*>) {
        hookAll(phoneInterfaceManagerClass, "getCellLocation", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = null
                XposedBridge.log("$tag Cleared cell location while spoofing.")
            }
        })
    }

    private fun hookCellInfo(phoneInterfaceManagerClass: Class<*>) {
        hookAll(phoneInterfaceManagerClass, "getAllCellInfo", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                // Return empty cell info on purpose so apps that rely on tower checks can fall back
                // to GPS-derived location when spoofing is active.
                // TODO: This may conflict with other telephony signals (for example, network type
                // still reporting MOBILE). If users report issues, synthesize coherent fake data.
                param.result = emptyList<CellInfo>()
                XposedBridge.log("$tag Cleared all cell info while spoofing.")
            }
        })

        hookAll(phoneInterfaceManagerClass, "getNeighboringCellInfo", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                // Same reasoning as getAllCellInfo: keep neighboring towers empty to encourage
                // GPS fallback in apps that combine cell and GNSS signals.
                // TODO: If consistency checks fail in some apps, provide coherent fake neighbors.
                param.result = emptyList<NeighboringCellInfo>()
                XposedBridge.log("$tag Cleared neighboring cell info while spoofing.")
            }
        })

        hookAll(phoneInterfaceManagerClass, "requestCellInfoUpdateInternal", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!shouldSpoofArgs(param.args)) return
                param.result = defaultReturnValue(param.method as? Method)
                XposedBridge.log("$tag Blocked async cell info update while spoofing.")
            }
        })
    }

    private fun hookAll(clazz: Class<*>, methodName: String, callback: XC_MethodHook) {
        try {
            val hooks = XposedBridge.hookAllMethods(clazz, methodName, callback)
            if (hooks.isNotEmpty()) {
                XposedBridge.log("$tag Hooked ${clazz.name}#$methodName (${hooks.size} overloads).")
            }
        } catch (e: Throwable) {
            XposedBridge.log("$tag Failed hooking ${clazz.name}#$methodName: ${e.message}")
        }
    }

    private fun findClass(classLoader: ClassLoader, vararg names: String): Class<*>? {
        names.forEach { name ->
            try {
                return XposedHelpers.findClass(name, classLoader)
            } catch (_: Throwable) {
                // Keep trying ROM-specific framework names.
            }
        }
        XposedBridge.log("$tag None of these classes were found: ${names.joinToString()}")
        return null
    }

    private fun shouldSpoofArgs(args: Array<Any?>?): Boolean {
//        return args?.asSequence()
//            ?.mapNotNull(::extractPackageName)
//            ?.any(LocationUtil::shouldSpoofPackage) == true
        return false // TODO: this is just a temp fix
    }

    private fun extractPackageName(value: Any?): String? {
        if (value is String) return value.takeIf { "." in it && !it.startsWith("android.") }
        return null
    }

    private fun defaultReturnValue(method: Method?): Any? {
        return when (method?.returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0F
            java.lang.Double.TYPE -> 0.0
            else -> null
        }
    }
}
