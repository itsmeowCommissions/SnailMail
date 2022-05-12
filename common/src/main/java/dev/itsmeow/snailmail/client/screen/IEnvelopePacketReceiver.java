package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.network.SendEnvelopePacket;

public interface IEnvelopePacketReceiver {

    void receivePacket(SendEnvelopePacket packet);

}
