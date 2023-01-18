package net.examplemod.client.screen;

import net.examplemod.network.SendEnvelopePacket;

public interface IEnvelopePacketReceiver {
    void receivePacket(SendEnvelopePacket packet);
}
