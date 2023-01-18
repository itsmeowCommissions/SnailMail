package net.examplemod.util;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class SnailMailCommonConfig {
    @ExpectPlatform
    public static boolean lockBoxes() {
        return true;
    }

    @ExpectPlatform
    public static boolean protectBoxDestroy() {
        return true;
    }

    @ExpectPlatform
    public static boolean opBypassLock() {
        return true;
    }

    @ExpectPlatform
    public static int bypassLockOpLevel() {
        return 3;
    }
}
