package dev.itsmeow.snailmail.util;

public interface ConfigInterface {

    default boolean lockBoxes() {
        return true;
    }

    default boolean protectBoxDestroy() {
        return true;
    }

    default boolean opBypassLock() {
        return true;
    }

    default int bypassLockOpLevel() {
        return 3;
    }
}
