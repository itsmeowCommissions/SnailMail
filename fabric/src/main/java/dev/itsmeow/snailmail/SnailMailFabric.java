package dev.itsmeow.snailmail;

import net.fabricmc.api.ModInitializer;

public class SnailMailFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SnailMail.construct();
    }
}
