package com.ru426.android.xposed.parts.quick_settings_toggle.tools;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ru426.android.xposed.parts.quick_settings_toggle.R;

public class BrightnessDialog extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.ru_use_light_theme_key), false)){
			setTheme(R.style.RuDialogLight);
		}
		setContentView(R.layout.brightness_dialog);		
		BrightnessController brightnessController = (BrightnessController) findViewById(R.id.brightnessController);
		Button ok = (Button) brightnessController.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.ok:
					finish();
					break;
				}
			}
		});
	}
}
