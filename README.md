# NightLightDarkroom
A simple quick settings tile app to add darkroom mode to Android Night Light

This app adds a simple quick settings toggle to emulate f.lux's "Darkroom" mode by setting the colour temperature to 0K.

The ``WRITE_SECURE_SETTINGS`` permission is required for this application to work. This has to be granted manually using ADB, and can be done so by running ``adb shell pm grant net.jtattersall.NightLightDarkroom android.permission.WRITE_SECURE_SETTINGS``.

For this to work correctly, the minimum colour temperature must be set to 0. **This setting is configured by the device manufacturer when the software is built and can only be overridden with root access.** A Magisk module to override this can be found [here](https://github.com/jamerst/magisk-intense-night-light), use at your own risk.