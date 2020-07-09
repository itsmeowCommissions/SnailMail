package dev.itsmeow.snailmail.network;

import java.util.function.Supplier;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetSnailBoxNamePacket {

    public String name = "";

    public SetSnailBoxNamePacket(String name) {
        this.name = name;
    }

    public static void encode(SetSnailBoxNamePacket pkt, PacketBuffer buf) {
        buf.writeString(pkt.name);
    }

    public static SetSnailBoxNamePacket decode(PacketBuffer buf) {
        return new SetSnailBoxNamePacket(buf.readString());
    }

    public static class Handler {
        public static void handle(SetSnailBoxNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity sender = ctx.get().getSender();
                    if(sender.openContainer instanceof SnailBoxContainer) {
                        ((SnailBoxContainer) sender.openContainer).setTileName(sender, RandomUtil.filterAllowedCharacters(msg.name, true));
                    }
                });
            }
            ctx.get().setPacketHandled(true);
        }

    }

}
