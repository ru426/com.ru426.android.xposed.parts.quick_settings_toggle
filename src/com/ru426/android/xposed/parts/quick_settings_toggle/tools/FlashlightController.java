package com.ru426.android.xposed.parts.quick_settings_toggle.tools;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class FlashlightController {
	public static Camera camera = null;
	public static boolean toggleFlashlight(){
		boolean flashOn = false;
		try{
			if(camera != null){
				Camera.Parameters params= camera.getParameters();
	    		params.setFlashMode(Parameters.FLASH_MODE_OFF);
	    		camera.setParameters(params);
	    		camera.release();
	            camera = null;
	            flashOn = false;
	    	}else{
	    		camera = Camera.open();
				Camera.Parameters params= camera.getParameters();
				params.setFlashMode(Parameters.FLASH_MODE_TORCH);   	
		    	camera.setParameters(params);
		    	flashOn = true;
	    	}
		}catch(Exception e){
			e.printStackTrace();
		}
		return flashOn;	
	}
}
