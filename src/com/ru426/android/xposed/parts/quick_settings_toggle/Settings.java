package com.ru426.android.xposed.parts.quick_settings_toggle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;

public class Settings extends PreferenceActivity {
	private static Context mContext;
	private static SharedPreferences prefs;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		if(prefs.getBoolean(getString(R.string.ru_use_light_theme_key), false)){
			setTheme(android.R.style.Theme_DeviceDefault_Light);
		}
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings_fragment_tools);
	    init();
	    initOption();
	}

	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		}
        return super.onMenuItemSelected(featureId, item);
    }
	
	private static void showHomeButton(){
		if(mContext != null && ((Activity) mContext).getActionBar() != null){
			((Activity) mContext).getActionBar().setHomeButtonEnabled(true);
	        ((Activity) mContext).getActionBar().setDisplayHomeAsUpEnabled(true);
		}		
	}
	
	static void showRestartToast(){
		Toast.makeText(mContext, R.string.ru_restart_message, Toast.LENGTH_SHORT).show();
	}
	
	@SuppressWarnings("deprecation")
	private void init(){
		boolean isHookFlashlight = ((CheckBoxPreference) findPreference(getString(R.string.is_hook_flashlight_key))).isChecked();
		ListPreference flashlightTarget = (ListPreference) findPreference(getString(R.string.hook_flashlight_target_key));		
		int listId = flashlightTarget.findIndexOfValue(flashlightTarget.getValue());
		if(listId < 0) listId = 0;
		CharSequence[] entries = flashlightTarget.getEntries();
		flashlightTarget.setSummary(mContext.getString(R.string.hook_flashlight_target_summary, entries[listId]));
		flashlightTarget.setEnabled(isHookFlashlight);
		findPreference(getString(R.string.hook_flashlight_collapse_key)).setEnabled(isHookFlashlight);
	}
	
	@SuppressWarnings("deprecation")
	private void initOption(){
		showHomeButton();
		setPreferenceChangeListener(getPreferenceScreen());
	}

	private static void setPreferenceChangeListener(PreferenceScreen preferenceScreen){
		for(int i = 0; i < preferenceScreen.getPreferenceCount(); i++){
			if(preferenceScreen.getPreference(i) instanceof PreferenceCategory){
				for(int j = 0; j < ((PreferenceCategory) preferenceScreen.getPreference(i)).getPreferenceCount(); j++){
					((PreferenceCategory) preferenceScreen.getPreference(i)).getPreference(j).setOnPreferenceChangeListener(onPreferenceChangeListener);
				}
			}else{
				preferenceScreen.getPreference(i).setOnPreferenceChangeListener(onPreferenceChangeListener);				
			}
		}
	}
	
	private static OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			switch(preference.getTitleRes()){
			case R.string.is_hook_brightness_title:
				if(!prefs.getBoolean(preference.getKey(), false) && (Boolean) newValue){
					showRestartToast();
				}
				break;
			case R.string.is_hook_flashlight_title:				
				preference.getPreferenceManager().findPreference(mContext.getString(R.string.hook_flashlight_target_key)).setEnabled((Boolean) newValue);
				preference.getPreferenceManager().findPreference(mContext.getString(R.string.hook_flashlight_collapse_key)).setEnabled((Boolean) newValue);
				if(!prefs.getBoolean(preference.getKey(), false) && (Boolean) newValue){
					showRestartToast();
				}
				break;
			case R.string.hook_flashlight_target_title:
				ListPreference listPref = (ListPreference) preference;
				int listId = listPref.findIndexOfValue((String) newValue);
				CharSequence[] entries = listPref.getEntries();
				preference.setSummary(mContext.getString(R.string.hook_flashlight_target_summary, entries[listId]));
				break;
			case R.string.hook_flashlight_collapse_title:
				Intent intent = new Intent(FlashLightToolModule.STATE_CHANGE);
				intent.putExtra(FlashLightToolModule.STATE_EXTRA_IS_COLLAPSE_ON_LIGHT, (Boolean) newValue);
				mContext.sendBroadcast(intent);
				break;
			default:
				return false;
			}
			return true;
		}		
	};
}
