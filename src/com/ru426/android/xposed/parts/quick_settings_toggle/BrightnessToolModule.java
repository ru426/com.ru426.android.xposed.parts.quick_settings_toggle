package com.ru426.android.xposed.parts.quick_settings_toggle;

import java.util.LinkedList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import com.ru426.android.xposed.library.ModuleBase;
import com.ru426.android.xposed.library.util.XModUtil;
import com.ru426.android.xposed.parts.quick_settings_toggle.tools.BrightnessDialog;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class BrightnessToolModule extends ModuleBase {
	private static final String TAG = BrightnessToolModule.class.getSimpleName();
	@Override
	public void init(XSharedPreferences prefs, ClassLoader classLoader, boolean isDebug) {
		super.init(prefs, classLoader, isDebug);		
		Class<?> xToolsMain = XposedHelpers.findClass("com.sonymobile.systemui.statusbar.tools.ToolsMain", classLoader);
		Object callback[] = new Object[1];
		callback[0] = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					xLog(TAG + " : " + "afterHookedMethod createBrightness");
					mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
					@SuppressWarnings("unchecked")
					LinkedList<FrameLayout> mButtons = (LinkedList<FrameLayout>) XposedHelpers.getObjectField(param.thisObject, "mButtons");
					FrameLayout brightnessTool = mButtons.getLast();
					brightnessTool.setOnClickListener(null);
					brightnessTool.setOnClickListener(new OnClickListener() {									
						@Override
						public void onClick(View v) {
							if(mContext == null) return;
							XModUtil.collapseStatusBar(mContext);							
							try {
								Intent intent = new Intent();
								intent.setClassName(BrightnessToolModule.class.getPackage().getName(), BrightnessDialog.class.getCanonicalName());
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startActivity(intent);
							} catch (ActivityNotFoundException e) {
								XposedBridge.log(e);
							}
						}
					});
				} catch (Throwable throwable) {
					XposedBridge.log(throwable);
				}				
			}
		};
		xHookMethod(xToolsMain, "createBrightness", callback, (Boolean) xGetValue(prefs, xGetString(R.string.is_hook_brightness_key), false));
	}	
}
