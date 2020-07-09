package dev.itsmeow.snailmail.client.screen;

import dev.itsmeow.snailmail.network.SendEnvelopePacket;

public interface IEnvelopePacketReceiver {

    public void receivePacket(SendEnvelopePacket packet);

}
