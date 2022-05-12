package dev.itsmeow.snailmail.client;

import net.fabricmc.api.ClientModInitializer;

public class SnailMailClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SnailMailClient.clientInit();
    }
}
