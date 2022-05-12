package dev.itsmeow.snailmail.util.fabric;

import dev.itsmeow.snailmail.util.ConfigInterface;

public class SnailMailCommonConfigImpl {

    public static ConfigInterface CONFIG_WRAPPER = new ConfigInterface() {};

    public static boolean lockBoxes() {
        return CONFIG_WRAPPER.lockBoxes();
    }

    public static boolean protectBoxDestroy() {
        return CONFIG_WRAPPER.protectBoxDestroy();
    }

    public static boolean opBypassLock() {
        return CONFIG_WRAPPER.opBypassLock();
    }

    public static int bypassLockOpLevel() {
        return CONFIG_WRAPPER.bypassLockOpLevel();
    }

}
