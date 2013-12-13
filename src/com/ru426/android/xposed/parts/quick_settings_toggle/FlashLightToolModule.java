package com.ru426.android.xposed.parts.quick_settings_toggle;

import java.util.LinkedList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.ru426.android.xposed.library.ModuleBase;
import com.ru426.android.xposed.library.util.XModUtil;
import com.ru426.android.xposed.parts.quick_settings_toggle.tools.FlashlightActivity;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class FlashLightToolModule extends ModuleBase {
	private static final String TAG = FlashLightToolModule.class.getSimpleName();
	
	public static final String STATE_CHANGE = FlashLightToolModule.class.getName() + ".intent.action.STATE_CHANGE";
	public static final String STATE_EXTRA_FLASH_ON = FlashLightToolModule.class.getName() + ".intent.extra.STATE_EXTRA_FLASH_ON";
	public static final String STATE_EXTRA_IS_COLLAPSE_ON_LIGHT = FlashLightToolModule.class.getName() + ".intent.extra.STATE_EXTRA_EXTRA_IS_COLLAPSE_ON_LIGHT";

	private static ImageView toolsButtonImage;
	private static TextView toolsButtonText;

	private static boolean cameraStateOn = false;
	private static boolean isCollapseOnLight = false;
	
	@Override
	public void init(XSharedPreferences prefs, ClassLoader classLoader, boolean isDebug) {
		super.init(prefs, classLoader, isDebug);
		isCollapseOnLight = (Boolean) xGetValue(prefs, xGetString(R.string.hook_flashlight_collapse_key), false);
		final String createMethodName = (String) xGetValue(prefs, xGetString(R.string.hook_flashlight_target_key), xModuleResources.getStringArray(R.array.flashlight_action_value_list)[0]);
		Class<?> xToolsMain = XposedHelpers.findClass("com.sonymobile.systemui.statusbar.tools.ToolsMain", classLoader);
		Object callback[] = new Object[1];
		callback[0] = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					xLog(TAG + " : " + "afterHookedMethod " + createMethodName);
					mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
					IntentFilter intentFilter = new IntentFilter();
					intentFilter.addAction(STATE_CHANGE);
					xRegisterReceiver(mContext, intentFilter);
					
					@SuppressWarnings("unchecked")
					LinkedList<FrameLayout> mButtons = (LinkedList<FrameLayout>) XposedHelpers.getObjectField(param.thisObject, "mButtons");
					FrameLayout toolsButton = mButtons.getLast();
					if(mContext != null && mContext.getResources().getIdentifier("tools_button_layout", "id", mContext.getPackageName()) > 0){
						LinearLayout toolsButtonLayout = (LinearLayout) toolsButton.findViewById(mContext.getResources().getIdentifier("tools_button_layout", "id", mContext.getPackageName()));
						for(int i = 0; i < toolsButtonLayout.getChildCount(); i++){
							if(toolsButtonLayout.getChildAt(i) instanceof FrameLayout){
								FrameLayout imageContainer = (FrameLayout) toolsButtonLayout.getChildAt(i);
								imageContainer.removeAllViews();
								toolsButtonImage = new ImageView(mContext);
								@SuppressWarnings("deprecation")
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
								toolsButtonImage.setLayoutParams(params);
								toolsButtonImage.setImageDrawable(xModuleResources.getDrawable(R.drawable.flashlight_button));
								toolsButtonImage.setScaleType(ScaleType.CENTER);								
								imageContainer.addView(toolsButtonImage);
							}else{
								if(mContext.getResources().getIdentifier("tools_button_text", "id", mContext.getPackageName()) > 0){
									if(toolsButtonLayout.getChildAt(i).getId() == mContext.getResources().getIdentifier("tools_button_text", "id", mContext.getPackageName())){
										toolsButtonText = (TextView) toolsButtonLayout.getChildAt(i);
										toolsButtonText.setText(xModuleResources.getString(R.string.flashlight_text));
									}
								}
							}
						}						
					}
					toolsButton.setOnClickListener(null);
					toolsButton.setOnClickListener(new OnClickListener() {									
						@Override
						public void onClick(View v) {
							if(mContext == null) return;
							try {
								Intent intent = new Intent();
								intent.setClassName(FlashLightToolModule.class.getPackage().getName(), FlashlightActivity.class.getCanonicalName());
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								if(isCollapseOnLight){
									XModUtil.collapseStatusBar(mContext);
								}
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
		xHookMethod(xToolsMain, createMethodName, callback, (Boolean) xGetValue(prefs, xGetString(R.string.is_hook_flashlight_key), false));
		
		Object callback2[] = new Object[2];
		callback2[0] = Configuration.class;
		callback2[1] = new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					xLog(TAG + " : " + "afterHookedMethod configurationChanged");
					mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
				    mConfiguration = (Configuration) param.args[0];
				    xModuleResources.updateConfiguration(mConfiguration, null);
					if(toolsButtonText != null && toolsButtonImage != null){
						toolsButtonText.setText(xModuleResources.getString(R.string.flashlight_text));
						setToggleAccentedColor(mContext);
						if(cameraStateOn){
							setToggleAccentedColor(mContext);
						}else{
							toolsButtonImage.setImageDrawable(xModuleResources.getDrawable(R.drawable.stat_flashlight_off));
							toolsButtonText.setTextColor(-0x1);
						}
					}					
				} catch (Throwable throwable) {
					XposedBridge.log(throwable);
				}				
			}
		};
		if((Boolean) xGetValue(prefs, xGetString(R.string.is_hook_flashlight_key), false)){
			try{//GXMod
				XposedHelpers.findAndHookMethod(xToolsMain, "configurationChanged", callback2);
			}catch(NoSuchMethodError e){//Else
				XposedHelpers.findAndHookMethod(xToolsMain, "onConfigurationChanged", callback2);
			}			
		}
	}
	
	@Override
	protected void xOnReceive(Context context, Intent intent) {
		super.xOnReceive(context, intent);
		xLog(TAG + " : " + "OnReceive " + intent.getAction());
		if (intent.getAction().equals(STATE_CHANGE)) {
			if(cameraStateOn = intent.getBooleanExtra(STATE_EXTRA_FLASH_ON, false)){
				setToggleAccentedColor(context);
			}else{
				toolsButtonImage.setImageDrawable(xModuleResources.getDrawable(R.drawable.stat_flashlight_off));
				toolsButtonText.setTextColor(-0x1);
			}
			isCollapseOnLight = intent.getBooleanExtra(STATE_EXTRA_IS_COLLAPSE_ON_LIGHT, false);
		}
	}

	private static void setToggleAccentedColor(Context mContext) {
		int color = XModUtil.getAccentColor(mContext);
		toolsButtonImage.setImageDrawable(xModuleResources.getDrawable(R.drawable.stat_flashlight_on));
		Drawable drawable = toolsButtonImage.getDrawable();
		try{
			drawable.setColorFilter(color, Mode.SRC_ATOP);
		}catch(NotFoundException e){
			drawable.setColorFilter(mContext.getResources().getColor(android.R.color.holo_blue_bright), Mode.SRC_ATOP);
		}
		toolsButtonImage.setImageDrawable(drawable);
		if(color != android.R.color.darker_gray) toolsButtonText.setTextColor(color);
	}
}
