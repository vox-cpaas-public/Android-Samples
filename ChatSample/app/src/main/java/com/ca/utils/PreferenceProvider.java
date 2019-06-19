package com.ca.utils;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class PreferenceProvider {
	SharedPreferences Pref;
	Editor edit;
	public static String IS_FILE_SHARE_AVAILABLE="isFileShareAvailable";
	public PreferenceProvider(Context applicationContext) {
		Pref = applicationContext.getSharedPreferences(Constants.MyPREFERENCES,
				Context.MODE_PRIVATE);
	}
	public void setPrefString(String key, String val) {
		edit = Pref.edit();
		edit.putString(key, val);
		edit.commit();
	}
	public void setPrefboolean(String key, boolean val) {
		edit = Pref.edit();
		edit.putBoolean(key, val);
		edit.commit();
	}
	public void setPrefint(String key, int val) {
		edit = Pref.edit();
		edit.putInt(key, val);
		edit.commit();
	}
	public void setPreffloat(String key, float val) {
		edit = Pref.edit();
		edit.putFloat(key, val);
		edit.commit();
	}
	public String getPrefString(String key) {
		return Pref.getString(key, "");
	}
	public boolean getPrefBoolean(String key) {
		return Pref.getBoolean(key, false);
	}
	public int getPrefInt(String key) {
		return Pref.getInt(key, 0);
	}
	public float getPrefFloat(String key) {
		return Pref.getFloat(key, 0);
	}
	public int getPrefInt(String key, int i) {
		return Pref.getInt(key, -1);
	}
}
