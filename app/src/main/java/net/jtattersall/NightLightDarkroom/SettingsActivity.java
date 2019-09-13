package net.jtattersall.NightLightDarkroom;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // only allow numbers to be entered
            EditTextPreference normalTempPref = getPreferenceManager().findPreference("normalTemp");
            EditTextPreference darkroomTempPref = getPreferenceManager().findPreference("darkroomTemp");

            EditTextPreference.OnBindEditTextListener listener = new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            };

            normalTempPref.setOnBindEditTextListener(listener);
            darkroomTempPref.setOnBindEditTextListener(listener);

            // set to current colour temperature if not already saved
            if (TextUtils.isEmpty(normalTempPref.getText())) {
                int currentTemp = 1500;
                try {
                    currentTemp = Settings.Secure.getInt(getContext().getContentResolver(), "night_display_color_temperature");
                } catch (Settings.SettingNotFoundException ex) {}

                normalTempPref.setText(String.valueOf(currentTemp));
            }
        }
    }
}