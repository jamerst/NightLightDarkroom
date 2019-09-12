package net.jtattersall.NightLightDarkroom;

import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class DarkroomTileService extends TileService {
    private boolean isDarkroom = false;
    private int normalTemp = 1500;

    @Override
    public void onClick() {
        Tile tile = this.getQsTile();

        if (isDarkroom) {
            Settings.Secure.putInt(this.getContentResolver(), "night_display_color_temperature", normalTemp);
            tile.setLabel("Normal");
            tile.setState(Tile.STATE_INACTIVE);
            tile.setIcon(Icon.createWithResource(this, R.drawable.ic_brightness_medium_black_24dp));
            tile.updateTile();
            isDarkroom = false;
        } else {
            int nightLightActive = 0;

            try {
                nightLightActive = Settings.Secure.getInt(this.getContentResolver(), "night_display_activated");
                normalTemp = Settings.Secure.getInt(this.getContentResolver(), "night_display_color_temperature");
            } catch (Settings.SettingNotFoundException ex) {
                // do nothing
            }

            if (nightLightActive == 1) {
                Settings.Secure.putInt(this.getContentResolver(), "night_display_color_temperature", 0);
                tile.setLabel("Darkroom");
                tile.setState(Tile.STATE_ACTIVE);
                tile.setIcon(Icon.createWithResource(this, R.drawable.ic_brightness_low_black_24dp));
                tile.updateTile();
                isDarkroom = true;
            }
        }
    }
}
