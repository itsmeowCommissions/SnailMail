package dev.itsmeow.snailmail.network;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenEnvelopeGUIPacket {
    public OpenEnvelopeGUIPacket(){
    }


    public static void encode(OpenEnvelopeGUIPacket pkt, PacketBuffer buf) {

    }

    public static OpenEnvelopeGUIPacket decode(PacketBuffer buf) {

        return new OpenEnvelopeGUIPacket();
    }

    public static class Handler {
        public static void handle(OpenEnvelopeGUIPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity sender = ctx.get().getSender();
                    if(sender.openContainer instanceof SnailBoxContainer) {
                        ItemStack stack = sender.openContainer.getSlot(27).getStack();
                        if (!stack.isEmpty() && stack.getItem() == ModItems.ENVELOPE_OPEN) {
                            EnvelopeItem.openGUI(sender, stack);
                        }
                    }
                });
            }
            ctx.get().setPacketHandled(true);
        }
    }
}
