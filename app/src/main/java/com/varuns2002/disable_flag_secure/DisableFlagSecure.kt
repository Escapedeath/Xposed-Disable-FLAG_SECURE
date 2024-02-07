package com.varuns2002.disable_flag_secure;

import android.view.SurfaceView;
import android.view.Window;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DisableFlagSecure implements IXposedHookLoadPackage {

    private final boolean debug = false;

    private final XC_MethodHook mForceSecureFlagHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            int flags = (int) param.args[0];
            flags |= LayoutParams.FLAG_SECURE;
            param.args[0] = flags;
        }
    };

    private final XC_MethodHook mForceSetSecureHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            param.args[0] = true;
        }
    };

    private final XC_MethodHook mForceSecureParamHook = new XC_MethodHook() {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            LayoutParams params = (LayoutParams) param.args[1];
            params.flags |= LayoutParams.FLAG_SECURE;
        }
    };

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {
        XposedBridge.log("Forced FLAG_SECURE for: " + (loadPackageParam.packageName != null ? loadPackageParam.packageName : "null"));

        XposedHelpers.findAndHookMethod(
            Window.class, "setFlags", int.class, int.class, mForceSecureFlagHook
        );

        XposedHelpers.findAndHookMethod(
            SurfaceView.class, "setSecure", boolean.class, mForceSetSecureHook
        );

        try {
            Class<?> windowsState = XposedHelpers.findClass(
                "com.android.server.wm.WindowState", loadPackageParam.classLoader
            );
            XposedHelpers.findAndHookMethod(
                windowsState, "isSecureLocked", XC_MethodReplacement.returnConstant(true)
            );
        } catch (Throwable error) {
            if (debug) XposedBridge.log("Force-FLAG_SECURE: " + error);
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.view.WindowManagerGlobal", loadPackageParam.classLoader, "addView",
                View.class, ViewGroup.LayoutParams.class, Display.class, Window.class, mForceSecureParamHook
            );
        } catch (Throwable error) {
            if (debug) XposedBridge.log("Force-FLAG_SECURE: " + error);
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.view.WindowManagerGlobal", loadPackageParam.classLoader, "addView",
                View.class, ViewGroup.LayoutParams.class, Display.class, Window.class, int.class, mForceSecureParamHook
            );
        } catch (Throwable error) {
            if (debug) XposedBridge.log("Force-FLAG_SECURE: " + error);
        }

        try {
            XposedHelpers.findAndHookMethod(
                "android.view.WindowManagerGlobal", loadPackageParam.classLoader, "updateViewLayout",
                View.class, ViewGroup.LayoutParams.class, mForceSecureParamHook
            );
        } catch (Throwable error) {
            if (debug) XposedBridge.log("Force-FLAG_SECURE: " + error);
        }
    }
}
