package dev.itsmeow.snailmail.network;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.item.EnvelopeItem.EnvelopeContainer;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetEnvelopeNamePacket {

    public static enum Type {
        FROM,
        TO
    }

    public final Type type;
    public String name = "";

    public SetEnvelopeNamePacket(Type type, String name) {
        this.name = name;
        this.type = type;
    }

    public static void encode(SetEnvelopeNamePacket pkt, PacketBuffer buf) {
        buf.writeBoolean(pkt.type == Type.FROM);
        buf.writeInt(pkt.name.length());
        buf.writeCharSequence(pkt.name, StandardCharsets.UTF_8);
    }

    public static SetEnvelopeNamePacket decode(PacketBuffer buf) {
        return new SetEnvelopeNamePacket(buf.readBoolean() ? Type.FROM : Type.TO, String.valueOf(buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8)));
    }



    public static class Handler {
        public static void handle(SetEnvelopeNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity playerIn = ctx.get().getSender();
                    if (!playerIn.world.isRemote()){
                        BlockPos pos = SnailBoxBlock.lastClickedBox.get(playerIn.getUniqueID());
                        if (playerIn.world.getTileEntity(pos) instanceof SnailBoxBlockEntity){
                            SnailBoxBlockEntity box = (SnailBoxBlockEntity) playerIn.world.getTileEntity(pos);
                            ItemStack stack = box.getItemHandler().getStackInSlot(27);

                            String s = RandomUtil.filterAllowedCharacters(msg.name, false);
                            if(s.length() <= 35) {
                                if(msg.type == Type.TO) {
                                    EnvelopeItem.putStringChecked(stack, "AddressedTo", s);
                                } else {
                                    EnvelopeItem.putStringChecked(stack, "AddressedFrom", s);
                                }
                                box.getItemHandler().setStackInSlot(27, stack);
                            }
                        }
                    }

                    /*
                    if(sender.openContainer instanceof EnvelopeContainer) {
                        Hand hand = null;
                        if(sender.getHeldItem(Hand.MAIN_HAND).getItem() == ModItems.ENVELOPE_OPEN) {
                            hand = Hand.MAIN_HAND;
                        } else if(sender.getHeldItem(Hand.OFF_HAND).getItem() == ModItems.ENVELOPE_OPEN) {
                            hand = Hand.OFF_HAND;
                        }
                        if(hand != null) {
                            String s = RandomUtil.filterAllowedCharacters(msg.name, false);
                            if(s.length() <= 35) {
                                ItemStack stack = sender.getHeldItem(hand);
                                if(msg.type == Type.TO) {
                                    EnvelopeItem.setToName(sender, hand, stack, s);
                                } else {
                                    EnvelopeItem.setFromName(sender, hand, stack, s);
                                }
                            }
                        }
                    }
                    */
                });
            }
            ctx.get().setPacketHandled(true);
        }
        

    }

}
