package net.examplemod.client;

import net.examplemod.SnailMailFabricLikeClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SnailMailClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SnailMailFabricLikeClient.init();
    }
}
