package dev.itsmeow.snailmail.network;

import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.util.RandomUtil;
import me.shedaniel.architectury.networking.NetworkManager;
import me.shedaniel.architectury.utils.Env;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SetEnvelopeNamePacket {

    public enum Type {
        FROM,
        TO
    }

    public final Type type;
    public String name = "";

    public SetEnvelopeNamePacket(Type type, String name) {
        this.name = name;
        this.type = type;
    }

    public static void encode(SetEnvelopeNamePacket pkt, FriendlyByteBuf buf) {
        buf.writeBoolean(pkt.type == Type.FROM);
        buf.writeInt(pkt.name.length());
        buf.writeCharSequence(pkt.name, StandardCharsets.UTF_8);
    }

    public static SetEnvelopeNamePacket decode(FriendlyByteBuf buf) {
        return new SetEnvelopeNamePacket(buf.readBoolean() ? Type.FROM : Type.TO, String.valueOf(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8)));
    }

    public static class Handler {
        public static void handle(SetEnvelopeNamePacket msg, Supplier<NetworkManager.PacketContext> ctx) {
            if(ctx.get().getEnvironment() == Env.SERVER) {
                ctx.get().queue(() -> {
                    ServerPlayer sender = (ServerPlayer) ctx.get().getPlayer();
                    BlockPos pos = SnailBoxBlock.lastClickedBox.get(sender.getUUID());
                    if (sender.level.getBlockEntity(pos) instanceof SnailBoxBlockEntity){
                        SnailBoxBlockEntity box = (SnailBoxBlockEntity) sender.level.getBlockEntity(pos);
                        ItemStack stack = SnailBoxBlockEntity.getEnvelope(box);
                        String s = RandomUtil.filterAllowedCharacters(msg.name, false);
                        if(s.length() <= 35) {
                            if(msg.type == Type.TO) {
                                EnvelopeItem.putStringChecked(stack, "AddressedTo", s);
                            } else {
                                EnvelopeItem.putStringChecked(stack, "AddressedFrom", s);
                            }
                            SnailBoxBlockEntity.setEnvelope(box, stack);
                        }
                    }
                });
            }
        }
    }

}
