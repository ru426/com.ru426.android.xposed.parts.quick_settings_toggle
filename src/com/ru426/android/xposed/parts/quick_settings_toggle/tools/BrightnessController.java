package com.ru426.android.xposed.parts.quick_settings_toggle.tools;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.ru426.android.xposed.library.util.XModUtil;
import com.ru426.android.xposed.parts.quick_settings_toggle.R;

public class BrightnessController extends LinearLayout implements android.widget.CompoundButton.OnCheckedChangeListener, android.widget.SeekBar.OnSeekBarChangeListener{
	private Context mContext;
	
	private int mCurrentState;
	private boolean mAutomatic;
	private int mBrightness;
	private int brightnessMode;
	ArrayList<Integer> brightnessArray = new ArrayList<Integer>();
	
	private CheckBox mCheckBox;
	private SeekBar mSeekBar;
	private View divider;
	
	protected SettingsContentObserver mSettingsContentObserver;
	private Window window;
	
	public BrightnessController(Context context, AttributeSet attribute) {
		super(context, attribute);
		View.inflate(context, R.layout.brightness_dialog_layout, this);
		mContext = context;
		Activity parent = (Activity) mContext;
	    window = parent.getWindow();
		mSettingsContentObserver = new SettingsContentObserver(new Handler());
		mCurrentState = getState();
		init();
	}

	public void init() {
		try {
			mAutomatic = android.provider.Settings.System.getInt(mContext.getContentResolver(), "screen_brightness_mode") == 1;
			mBrightness = android.provider.Settings.System.getInt(mContext.getContentResolver(), "screen_brightness", 80);
		} catch (android.provider.Settings.SettingNotFoundException e) {
			e.printStackTrace();
		}
		mSeekBar = (SeekBar) findViewById(R.id.seekbar);
		mSeekBar.setMax(255);
		mSeekBar.setOnSeekBarChangeListener(this);
		reDrawSeekBar(mSeekBar);
		mSeekBar.setProgress(mBrightness);
		
		mCheckBox = (CheckBox) findViewById(R.id.automatic_mode);
		mCheckBox.setChecked(mAutomatic);
		mCheckBox.setOnCheckedChangeListener(this);
		brightnessArray.add(0);
		brightnessArray.add(0);
		
		divider = findViewById(R.id.divider);
		divider.setBackgroundColor(XModUtil.getAccentColor(mContext));
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if(mSettingsContentObserver != null){
			mSettingsContentObserver.observe();
			mCurrentState = getState();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mSettingsContentObserver != null){
			mSettingsContentObserver.unobserve();
		}
	}
	
	@Override
	public void onCheckedChanged(CompoundButton view, boolean flag) {
		mAutomatic = flag;
		int i = mAutomatic ? 1 : 0 ;
		try{
			android.provider.Settings.System.putInt(mContext.getContentResolver(), "screen_brightness_mode", i);
		}catch(SecurityException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
		if(fromUser){
			android.provider.Settings.System.putInt(mContext.getContentResolver(), "screen_brightness", progress);
			mBrightness = progress;
			setBrightness(mBrightness);
			int mState = getState();
			if (mState != mCurrentState) {
				mCurrentState = mState;
			}
		}		
	}
	
	private void setBrightness(int brightness){
		if(window != null){
			try {
				WindowManager.LayoutParams params = window.getAttributes();
				params.screenBrightness = (float) ((brightness*100/255)/100.0);
				if(params.screenBrightness <= 0){
					params.screenBrightness = 0.1f;
				}
				window.setAttributes(params);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private int getState() {
		return android.provider.Settings.System.getInt(mContext.getContentResolver(), "screen_brightness", 80) <= 150 ? 0 : 1;
	}
	
	private void reDrawSeekBar(SeekBar seekBar){
		if(mContext == null) return;
		Drawable particle = seekBar.getProgressDrawable();
		particle.clearColorFilter();
		try{
			particle.setColorFilter(XModUtil.getAccentColor(mContext), Mode.SRC_ATOP);
		}catch(NotFoundException e){
			particle.setColorFilter(Color.WHITE, Mode.SRC_ATOP);
		}
		ClipDrawable progress = new ClipDrawable(particle, Gravity.LEFT, ClipDrawable.HORIZONTAL);
		seekBar.setProgressDrawable(null);
		seekBar.setProgressDrawable(progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {		
		try {
			brightnessMode = Settings.System.getInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE);
			android.provider.Settings.System.putInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		android.provider.Settings.System.putInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
	}
	
	public class SettingsContentObserver extends ContentObserver {
		public SettingsContentObserver(Handler handler) {
			super(handler);
		}
		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if(!selfChange){
				try {
					mAutomatic = android.provider.Settings.System.getInt(mContext.getContentResolver(), "screen_brightness_mode") == 1;
					mBrightness = android.provider.Settings.System.getInt(mContext.getContentResolver(), "screen_brightness");
					
					brightnessArray.add(mBrightness);
					if(mBrightness == 0 && brightnessArray.size() > 1){
						mBrightness = brightnessArray.get(brightnessArray.size()-2);
						android.provider.Settings.System.putInt(mContext.getContentResolver(), "screen_brightness", mBrightness);
					}
					mSeekBar.setProgress(mBrightness);
					mCheckBox.setChecked(mAutomatic);
					if(brightnessArray.size() > 2){
						for(int i = 0; i < brightnessArray.size()-2; i++){
							brightnessArray.remove(i);
						}
					}
				} catch (android.provider.Settings.SettingNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		
		public void observe() {
			ContentResolver contentresolver = mContext.getContentResolver();
			contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("screen_brightness"), false, this);
			contentresolver.registerContentObserver(android.provider.Settings.System.getUriFor("screen_brightness_mode"), false, this);
		}
		
		public void unobserve() {
			mContext.getContentResolver().unregisterContentObserver(this);
		}
	}
}
