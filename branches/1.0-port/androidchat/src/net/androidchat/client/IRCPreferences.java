package net.androidchat.client;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;

public class IRCPreferences extends PreferenceActivity {
	
	public IRCPreferences() {
		
	}

	protected int resourceId() {
		return R.xml.irc_preferences;
	}

	@Override
	public void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);
		
		PreferenceManager manager = getPreferenceManager();
		manager.setSharedPreferencesName("androidChatPrefs");
		manager.setSharedPreferencesMode(MODE_WORLD_WRITEABLE);
		this.
		addPreferencesFromResource(resourceId());
		
		Preference p = findPreference("pref_sync_wallpapers_key");
		if (p != null) {
			//SharedPreferences sp = p.getSharedPreferences();
			p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object newObjValue) {
					Boolean newValue = (Boolean)newObjValue;
					if (newValue == null)
						return false;
					p.setSummary(String.format(getResources().getString(R.string.irc_nickname), newValue));
					return true;
				}
			});
		}

	} 
}