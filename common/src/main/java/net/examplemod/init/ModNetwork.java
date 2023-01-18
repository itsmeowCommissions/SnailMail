package net.examplemod.init;

import dev.architectury.networking.NetworkChannel;
import net.examplemod.SnailMail;
import net.examplemod.network.*;
import net.minecraft.resources.ResourceLocation;

public class ModNetwork {
    public static final NetworkChannel HANDLER = NetworkChannel.create(new ResourceLocation(SnailMail.MODID, "main_channel"));

    public static void init() {
        HANDLER.register(SetEnvelopeNamePacket.class, SetEnvelopeNamePacket::encode, SetEnvelopeNamePacket::decode, SetEnvelopeNamePacket.Handler::handle);
        HANDLER.register(SendEnvelopePacket.class, SendEnvelopePacket::encode, SendEnvelopePacket::decode, SendEnvelopePacket.Handler::handle);
        HANDLER.register(UpdateSnailBoxPacket.class, UpdateSnailBoxPacket::encode, UpdateSnailBoxPacket::decode, UpdateSnailBoxPacket.Handler::handle);
        HANDLER.register(OpenEnvelopeGUIPacket.class, OpenEnvelopeGUIPacket::encode, OpenEnvelopeGUIPacket::decode, OpenEnvelopeGUIPacket.Handler::handle);
        HANDLER.register(OpenSnailBoxGUIPacket.class, OpenSnailBoxGUIPacket::encode, OpenSnailBoxGUIPacket::decode, OpenSnailBoxGUIPacket.Handler::handle);
    }
}
