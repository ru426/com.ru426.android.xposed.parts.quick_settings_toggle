package com.ru426.android.xposed.parts.quick_settings_toggle.tools;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ru426.android.xposed.parts.quick_settings_toggle.FlashLightToolModule;
import com.ru426.android.xposed.parts.quick_settings_toggle.R;

public class FlashlightActivity extends Activity {
	public static final String FLASH_STATE_ON_KEY = "FlashStateOnKey";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.edit().putBoolean(FLASH_STATE_ON_KEY, FlashlightController.toggleFlashlight()).commit();
		Intent intent = new Intent();
		intent.setAction(FlashLightToolModule.STATE_CHANGE);
		intent.putExtra(FlashLightToolModule.STATE_EXTRA_FLASH_ON, prefs.getBoolean(FLASH_STATE_ON_KEY, false));
		intent.putExtra(FlashLightToolModule.STATE_EXTRA_IS_COLLAPSE_ON_LIGHT, prefs.getBoolean(getString(R.string.hook_flashlight_collapse_key), false));
		sendBroadcast(intent);
		finish();
	}
}
