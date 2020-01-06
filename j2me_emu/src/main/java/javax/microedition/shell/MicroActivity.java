/*
 * Copyright 2015-2016 Nickolay Savchenko
 * Copyright 2017-2018 Nikita Shakarun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.microedition.shell;


import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.swkj.j2me_emu.R;

import java.util.Locale;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.ViewHandler;
import javax.microedition.lcdui.event.EventQueue;
import javax.microedition.lcdui.event.SimpleEvent;
import javax.microedition.lcdui.overlay.OverlayView;
import javax.microedition.m3g.Graphics3D;
import javax.microedition.midlet.MIDlet;
import javax.microedition.setting.KeyMapper;
import javax.microedition.util.Config;
import javax.microedition.util.ContextHolder;


public class MicroActivity extends AppCompatActivity {
	private static final int ORIENTATION_DEFAULT = 0;
	private static final int ORIENTATION_AUTO = 1;
	private static final int ORIENTATION_PORTRAIT = 2;
	private static final int ORIENTATION_LANDSCAPE = 3;

	private Displayable current;
	private boolean visible;
	private boolean loaded;
	private boolean started;
	private boolean actionBarEnabled;
	private LinearLayout layout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		setProperties();
		applyConfiguration();
		super.onCreate(savedInstanceState);
		ContextHolder.setCurrentActivity(this);
		setContentView(R.layout.activity_micro);
		OverlayView overlayView = findViewById(R.id.vOverlay);
		layout = findViewById(R.id.displayable_container);

		actionBarEnabled = sp.getBoolean("pref_actionbar_switch", false);
		boolean wakelockEnabled = sp.getBoolean("pref_wakelock_switch", false);
		if (wakelockEnabled) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		setOrientation(ORIENTATION_LANDSCAPE);
		Display.initDisplay();
		Graphics3D.initGraphics3D();
		try {
            String midName = getString(R.string.start_mid);
			Class cl = Class.forName(midName);
			MIDlet obj = (MIDlet) cl.newInstance();
			obj.setContext(this);
			if(obj!=null){
				startMidlet(obj);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		visible = true;
		if (loaded) {
			if (started) {
				Display.getDisplay(null).activityResumed();
			} else {
				started = true;
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		visible = false;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (loaded) {
			Display.getDisplay(null).activityStopped();
		}
	}


	private void setOrientation(int orientation) {
		switch (orientation) {
			case ORIENTATION_AUTO:
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
				break;
			case ORIENTATION_PORTRAIT:
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
				break;
			case ORIENTATION_LANDSCAPE:
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
				break;
			case ORIENTATION_DEFAULT:
			default:
				break;
		}
	}



	private void loadMIDlet(MIDlet miDlet) throws Exception {
			startMidlet(miDlet);
	}


	private void startMidlet(final MIDlet midlet) {
		try {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						midlet.startApp();
						loaded = true;
					} catch (Throwable t) {
						t.printStackTrace();
						ContextHolder.notifyDestroyed();
					}
				}
			};
			(new Thread(r, "MIDletLoader")).start();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}




	public void setCurrent(Displayable disp) {
		current = disp;
		ViewHandler.postEvent(msgSetCurrent);
	}

	public Displayable getCurrent() {
		return current;
	}

	public boolean isVisible() {
		return visible;
	}



	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
//		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
//			onKeyDown(event.getKeyCode(), event);
//			return true;
//		}
		System.out.println("keyCode="+event.getKeyCode());
		return super.dispatchKeyEvent(event);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
			case KeyEvent.KEYCODE_MENU:
//				openOptionsMenu();
				System.out.println("锁定返回键>>>>>>>>>>>>>>");
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	private SimpleEvent msgSetCurrent = new SimpleEvent() {
		@Override
		public void process() {
			current.setParentActivity(MicroActivity.this);
			current.clearDisplayableView();
			layout.removeAllViews();
			layout.addView(current.getDisplayableView());
//			invalidateOptionsMenu();
			//ActionBar actionBar = getSupportActionBar();
			//LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();
			if (current instanceof Canvas) {
				hideSystemUI();
//				if (!actionBarEnabled) {
//					actionBar.hide();
//				} else {
//					actionBar.setTitle(MyClassLoader.getName());
//					//layoutParams.height = (int) (getToolBarHeight() / 1.5);
//				}
			} else {
                hideSystemUI();
//				showSystemUI();
//				actionBar.show();
//				actionBar.setTitle(current.getTitle());
				//layoutParams.height = getToolBarHeight();
			}
			//toolbar.setLayoutParams(layoutParams);
		}
	};



	private int getToolBarHeight() {
		int[] attrs = new int[]{R.attr.actionBarSize};
		TypedArray ta = obtainStyledAttributes(attrs);
		int toolBarHeight = ta.getDimensionPixelSize(0, -1);
		ta.recycle();
		return toolBarHeight;
	}

	private void hideSystemUI() {
		System.out.println("当前版本号为 SDK_VERSION= "+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(
					//View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
							| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
							| View.SYSTEM_UI_FLAG_FULLSCREEN
							//| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
							);
		} else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			//透明状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			//透明导航栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//		}
	}

	private void showSystemUI() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}


	private void applyConfiguration() {
		try {
			int fontSizeSmall =18;
			int fontSizeMedium = 22;
			int fontSizeLarge = 26;
			boolean fontApplyDimensions = false;

			int screenWidth = Config.getOrigon_ScreenW();
			int screenHeight = Config.getOrigon_ScreenH();
			int screenBackgroundColor = 0x14e370;
			int screenScaleRatio = 100;
			boolean screenScaleToFit =true;
			boolean screenKeepAspectRatio = false;
			boolean screenFilter = false;
			boolean immediateMode = false;
			boolean touchInput = true;
			boolean hwAcceleration = false;
			boolean parallel = false;
			boolean showFps = false;
			boolean limitFps = false;
			int fpsLimit = 0;

			Font.setSize(Font.SIZE_SMALL, fontSizeSmall);
			Font.setSize(Font.SIZE_MEDIUM, fontSizeMedium);
			Font.setSize(Font.SIZE_LARGE, fontSizeLarge);
			Font.setApplyDimensions(fontApplyDimensions);

			final String[] propLines = "".split("\n");
			for (String line : propLines) {
				String[] prop = line.split(":", 2);
				if (prop.length == 2) {
					System.setProperty(prop[0], prop[1]);
				}
			}

			SparseIntArray intArray = KeyMapper.getArrayPref(this);
			Canvas.setVirtualSize(screenWidth, screenHeight, screenScaleToFit,
					screenKeepAspectRatio, screenScaleRatio);
			Canvas.setFilterBitmap(screenFilter);
			EventQueue.setImmediate(immediateMode);
			Canvas.setHardwareAcceleration(hwAcceleration, parallel);
			Canvas.setBackgroundColor(screenBackgroundColor);
			Canvas.setKeyMapping(intArray);
			Canvas.setHasTouchInput(touchInput);
			Canvas.setShowFps(showFps);
			Canvas.setLimitFps(limitFps, fpsLimit);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    private void setProperties() {
        System.setProperty("microedition.sensor.version", "1");
        System.setProperty("microedition.platform", "Nokia 6233");
        System.setProperty("microedition.configuration", "CDLC-1.1");
        System.setProperty("microedition.profiles", "MIDP-2.0");
        System.setProperty("microedition.m3g.version", "1.1");
        System.setProperty("microedition.media.version", "1.0");
        System.setProperty("supports.mixing", "true");
        System.setProperty("supports.audio.capture", "true");
        System.setProperty("supports.video.capture", "false");
        System.setProperty("supports.recording", "false");
        System.setProperty("microedition.pim.version", "1.0");
        System.setProperty("microedition.io.file.FileConnection.version", "1.0");
        final Locale defaultLocale = Locale.getDefault();
        final String country = defaultLocale.getCountry();
        System.setProperty("microedition.locale", defaultLocale.getLanguage()
                + (country.length() == 2 ? "-" + country : ""));
        System.setProperty("microedition.encoding", "ISO-8859-1");
        System.setProperty("user.home", Environment.getExternalStorageDirectory().getPath());
        System.setProperty("com.siemens.IMEI", "000000000000000");
        System.setProperty("com.siemens.mp.systemfolder.ringingtone", "fs/MyStuff/Ringtones");
        System.setProperty("com.siemens.mp.systemfolder.pictures", "fs/MyStuff/Pictures");
        System.setProperty("device.imei", "000000000000000");
    }

}
