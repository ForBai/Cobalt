package org.cobalt.init;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class AddonPrelaunchEntryPoint implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        AddonPrelaunchLoader.loadAddons();
    }
}
