package umpaz.brewinandchewin.client;

import umpaz.brewinandchewin.client.renderer.texture.BnCTextureModifiers;
import umpaz.brewinandchewin.platform.client.BnCClientPlatformHelper;

public class BrewinAndChewinClient {
    private static BnCClientPlatformHelper helper;

    public static BnCClientPlatformHelper getHelper() {
        return helper;
    }

    public static void init(BnCClientPlatformHelper helper) {
        BrewinAndChewinClient.helper = helper;
        BnCTextureModifiers.init();
    }
}
