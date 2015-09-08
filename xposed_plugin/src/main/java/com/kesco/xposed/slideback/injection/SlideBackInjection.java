package com.kesco.xposed.slideback.injection;

import android.app.Activity;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.kesco.adk.moko.slideback.SlideEdge;
import com.kesco.adk.moko.slideback.SlideLayout;
import com.kesco.adk.moko.slideback.SlideListener;
import com.kesco.adk.moko.slideback.SlideState;
import com.kesco.adk.moko.slideback.SlidebackPackage;

import org.jetbrains.annotations.NotNull;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SlideBackInjection implements IXposedHookZygoteInit,IXposedHookLoadPackage, IXposedHookInitPackageResources {

    private static String modPath;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        modPath = startupParam.modulePath;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.kesco.demo.imsi")) {
            return;
        }
        XposedHelpers.findAndHookMethod("android.support.v7.app.AppCompatActivity", lpparam.classLoader, "setContentView", "int", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                final Activity act = (Activity) param.thisObject;
                Window win = act.getWindow();
                ViewGroup decorView = (ViewGroup) win.getDecorView();
                Drawable bg = decorView.getBackground();
                act.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                decorView.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                View screenView = decorView.getChildAt(0);
                decorView.removeViewAt(0);
                SlideLayout slideLayout = new SlideLayout(act, screenView);
                slideLayout.addView(screenView);
                decorView.addView(slideLayout, 0);
                screenView.setBackgroundDrawable(bg);
                slideLayout.setSlideEdge(SlideEdge.LEFT);
                slideLayout.setListener(new SlideListener() {
                    @Override
                    public void onSlideStart() {
                    }

                    @Override
                    public void onSlide(float percent, @NotNull SlideState state) {
                        XposedBridge.log(act.getClass().getSimpleName() + ": " + percent);
                    }

                    @Override
                    public void onSlideFinish() {
                        XposedBridge.log(act.getClass().getSimpleName() + " : Finish");
                        act.finish();
                        act.overridePendingTransition(0, 0);
                    }
                });
                SlidebackPackage.convertActivityFromTranslucent(act);
            }
        });
        XposedHelpers.findAndHookMethod("android.support.v7.app.AppCompatActivity", lpparam.classLoader, "onPostCreate", Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Activity act = (Activity) param.thisObject;
                SlidebackPackage.convertActivityToTranslucent(act);
            }
        });
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals("com.kesco.demo.imsi")) {
            return;
        }
        XResources res = resparam.res;
        XModuleResources modRes = XModuleResources.createInstance(modPath, res);

        res.setReplacement(com.kesco.adk.moko.slideback.R.color.start_shadow_color,modRes.fwd(com.kesco.adk.moko.slideback.R.color.start_shadow_color));
        res.setReplacement(com.kesco.adk.moko.slideback.R.color.center_shadow_color, modRes.fwd(com.kesco.adk.moko.slideback.R.color.center_shadow_color));
        res.setReplacement(com.kesco.adk.moko.slideback.R.color.end_shadow_color,modRes.fwd(com.kesco.adk.moko.slideback.R.color.end_shadow_color));
        res.setReplacement(com.kesco.adk.moko.slideback.R.dimen.shadow_width,modRes.fwd(com.kesco.adk.moko.slideback.R.dimen.shadow_width));
    }
}
