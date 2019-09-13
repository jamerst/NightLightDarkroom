package net.jtattersall.NightLightDarkroom;
;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;
import android.database.ContentObserver;
import android.os.Handler;
import android.net.Uri;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class DarkroomTileService extends TileService {
    private boolean isDarkroom = false;

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener preferencesListener;

    private ContentObserver nightLightActiveObserver;

    @Override
    public void onCreate() {
        // create notification channel for errors
        createNotificationChannel();

        // get preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // create listener for settings changing
        preferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                updateTemp();
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(preferencesListener);

        // define observer for night light enabled setting
        nightLightActiveObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                nightLightStateChange();
            }
        };
        // get URI for night light enabled setting
        Uri nightLightActiveUri = Settings.Secure.getUriFor("night_display_activated");

        // start observing night light enabled setting
        this.getContentResolver().registerContentObserver(nightLightActiveUri, false, nightLightActiveObserver);
    }

    @Override
    public void onDestroy() {
        // stop observing setting and preferences
        this.getContentResolver().unregisterContentObserver(nightLightActiveObserver);
        preferences.unregisterOnSharedPreferenceChangeListener(preferencesListener);
    }

    @Override
    public void onClick() {
        Tile tile = this.getQsTile();

        if (isNightLightEnabled()) {
            if (isDarkroom) {
                disableDarkroom(tile, false);
            } else {
                enableDarkroom(tile);
            }
        }
    }

    private void enableDarkroom(Tile tile) {
        int darkroomTemp = Integer.parseInt(preferences.getString("darkroomTemp", "0"));
        if (setColorTemp(darkroomTemp)) {
            isDarkroom = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(getString(R.string.tile_enabled_subtitle, darkroomTemp));
            }
            tile.setState(Tile.STATE_ACTIVE);
            tile.updateTile();
        }
    }

    private void disableDarkroom(Tile tile, boolean activeObserver) {
        int normalTemp = Integer.parseInt(preferences.getString("normalTemp", "1200"));
        if (setColorTemp(normalTemp) || activeObserver) {
            isDarkroom = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(getString(R.string.tile_disabled_subtitle, normalTemp));
            }
            tile.setState(Tile.STATE_INACTIVE);
            tile.updateTile();
        }
    }

    // called when settings are changed to update temperature and tile
    private void updateTemp() {
        Tile tile = getQsTile();
        if (isDarkroom) {
            int darkroomTemp = Integer.parseInt(preferences.getString("darkroomTemp", "0"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(getString(R.string.tile_enabled_subtitle, darkroomTemp));
            }
            setColorTemp(darkroomTemp);
        } else {
            int normalTemp = Integer.parseInt(preferences.getString("normalTemp", "1200"));
            setColorTemp(normalTemp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setSubtitle(getString(R.string.tile_disabled_subtitle, normalTemp));
            }
        }
        tile.updateTile();
    }

    // called when night light enabled state changes to update tile accordingly
    private void nightLightStateChange() {
        Tile tile = this.getQsTile();

        if (isNightLightEnabled()) {
            disableDarkroom(tile, true);
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            disableDarkroom(tile, true);
            tile.setState(Tile.STATE_UNAVAILABLE);
        }
    }

    private boolean isNightLightEnabled() {
        int nightLightActive = 0;

        try {
            nightLightActive = getSecureSettingInt("night_display_activated");
        } catch (Settings.SettingNotFoundException ex) {}

        return nightLightActive == 1;
    }

    private int getSecureSettingInt(String name) throws Settings.SettingNotFoundException {
        return Settings.Secure.getInt(this.getContentResolver(), name);
    }

    private boolean setColorTemp(int temp) {
        try {
            Settings.Secure.putInt(this.getContentResolver(), "night_display_color_temperature", temp);
            return true;
        } catch (SecurityException ex) {
            sendPermissionsErrorNotification();
            return false;
        }
    }

    private void sendPermissionsErrorNotification() {
        // define notification
        Notification.Builder builder = new Notification.Builder(this, "darkroomErrors")
            .setSmallIcon(R.drawable.ic_brightness_low_black_24dp)
            .setContentTitle(getString(R.string.permissions_error_title))
            .setContentText(getString(R.string.permissions_error_content))
            .setStyle(new Notification.BigTextStyle()
                    .bigText(getString(R.string.permissions_error_content))
            );

        // send notification
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel
        CharSequence name = getString(R.string.error_notification_channel_name);
        String description = getString(R.string.error_notification_channel_desc);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("darkroomErrors", name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
