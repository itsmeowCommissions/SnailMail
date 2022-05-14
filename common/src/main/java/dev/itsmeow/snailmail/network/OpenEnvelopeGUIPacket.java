package dev.itsmeow.snailmail.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class OpenEnvelopeGUIPacket {

    private BlockPos pos;

    public OpenEnvelopeGUIPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(OpenEnvelopeGUIPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static OpenEnvelopeGUIPacket decode(FriendlyByteBuf buf) {
        return new OpenEnvelopeGUIPacket(buf.readBlockPos());
    }

    public static class Handler {
        public static void handle(OpenEnvelopeGUIPacket msg, Supplier<NetworkManager.PacketContext> ctx) {
            if(ctx.get().getEnvironment() == Env.SERVER) {
                ctx.get().queue(() -> {
                    ServerPlayer sender = (ServerPlayer) ctx.get().getPlayer();
                    if (sender.distanceToSqr(msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()) <= 25D) {
                        BlockEntity target = sender.level.getBlockEntity(msg.pos);
                        if(target instanceof SnailBoxBlockEntity) {
                            ItemStack stack = SnailBoxBlockEntity.getEnvelope((SnailBoxBlockEntity) target);
                            if(stack.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                                EnvelopeItem.openGUI(sender, stack, msg.pos);
                            }
                        }
                    }
                });
            }
        }
    }
}