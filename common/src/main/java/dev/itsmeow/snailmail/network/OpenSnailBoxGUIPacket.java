package dev.itsmeow.snailmail.network;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Supplier;

public class OpenSnailBoxGUIPacket {

    private BlockPos pos;

    public OpenSnailBoxGUIPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(OpenSnailBoxGUIPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
    }

    public static OpenSnailBoxGUIPacket decode(FriendlyByteBuf buf) {
        return new OpenSnailBoxGUIPacket(buf.readBlockPos());
    }

    public static class Handler {
        public static void handle(OpenSnailBoxGUIPacket msg, Supplier<NetworkManager.PacketContext> ctx) {
            if(ctx.get().getEnvironment() == Env.SERVER) {
                ctx.get().queue(() -> {
                    ServerPlayer sender = (ServerPlayer) ctx.get().getPlayer();
                    if (sender.distanceToSqr(msg.pos.getX(), msg.pos.getY(), msg.pos.getZ()) <= 25D) {
                        BlockEntity target = sender.level.getBlockEntity(msg.pos);
                        if(target instanceof SnailBoxBlockEntity && SnailBoxBlockEntity.getEnvelope((SnailBoxBlockEntity) target).getItem() == ModItems.ENVELOPE_OPEN.get() && ((SnailBoxBlockEntity) target).canAccess(sender)) {
                            ((SnailBoxBlockEntity) target).openGUI(sender);
                        } else {
                            sender.closeContainer();
                        }
                    } else {
                        sender.closeContainer();
                    }
                });
            }
        }
    }
}