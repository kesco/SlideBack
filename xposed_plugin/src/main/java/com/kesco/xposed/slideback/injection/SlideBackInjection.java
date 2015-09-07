package com.kesco.xposed.slideback.injection;

import android.app.Activity;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by kesco on 15-9-4.
 */
public class SlideBackInjection implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.kesco.demo.imsi")) {
            return;
        }
        XposedBridge.log(lpparam.packageName + " detected!");
        XposedHelpers.findAndHookMethod("android.support.v7.app.AppCompatActivity", lpparam.classLoader, "setContentView", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("before AppCompatActivity setContentView");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("after AppCompatActivity setContentView");
            }
        });
        XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "setContentView", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("before setContentView");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("after setContentView");
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("before onCreate");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("after onCreate");
            }
        });
    }

    @Override
    public void initZygote(StartupParam suparma) throws Throwable {
        XposedBridge.log(suparma.modulePath);
    }
}
