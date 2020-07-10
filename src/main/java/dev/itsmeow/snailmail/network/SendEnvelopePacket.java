package dev.itsmeow.snailmail.network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.SnailMail.SnailBoxData;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.client.screen.IEnvelopePacketReceiver;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.util.BoxData;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class SendEnvelopePacket {

    public static enum Type {
        TO_SERVER,
        NO_ENVELOPE,
        NO_STAMP,
        NO_ADDRESS,
        INVALID_ADDRESS,
        NO_BOXES,
        SELECT_BOX,
        SUCCESS
    }

    public Type type;
    public BoxData[] boxes;

    public SendEnvelopePacket(Type type) {
        this.type = type;
    }

    public SendEnvelopePacket(Type type, BoxData... boxes) {
        this.type = type;
        this.boxes = boxes;
    }

    public static void encode(SendEnvelopePacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.type.ordinal());
        if(pkt.boxes != null) {
            buf.writeInt(pkt.boxes.length);
            for(BoxData box : pkt.boxes) {
                box.write(buf);
            }
        }
    }

    public static SendEnvelopePacket decode(PacketBuffer buf) {
        Type type = Type.values()[buf.readInt()];
        BoxData[] boxL = null;
        if(buf.readableBytes() > 0) {
            int len = buf.readInt();
            boxL = new BoxData[len];
            for(int i = 0; i < len; i++) {
                boxL[i] = BoxData.read(buf);
            }
        }
        return new SendEnvelopePacket(type, boxL);
    }

    public static class Handler {
        public static void handle(SendEnvelopePacket msg, Supplier<NetworkEvent.Context> ctx) {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER && msg.type == Type.TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity sender = ctx.get().getSender();
                    if(sender.openContainer instanceof SnailBoxContainer) {
                        ItemStack stack = sender.openContainer.getSlot(27).getStack();
                        if(!stack.isEmpty() && stack.getItem() == ModItems.ENVELOPE_OPEN) {
                            if(EnvelopeItem.isStamped(stack)) {
                                String to = EnvelopeItem.getString(stack, "AddressedTo");
                                if(!to.isEmpty()) {
                                    GameProfile prof = sender.getServer().getPlayerProfileCache().getGameProfileForUsername(to);
                                    if(prof != null && prof.getId() != null) {
                                        UUID uuid = prof.getId();
                                        SnailBoxData data = SnailBoxData.getData(sender.getServer());
                                        Set<Location> boxPos = data.getBoxes(uuid);
                                        Set<BoxData> boxes = new HashSet<BoxData>();
                                        for(Location pos : boxPos) {
                                            boxes.add(new BoxData(data.getNameForPos(pos), pos, data.isPublic(pos)));
                                        }
                                        if(boxes.size() > 0) {
                                            if(msg.boxes == null) {
                                                List<BoxData> boxSorted = boxes.stream().sorted((element1, element2) -> {
                                                    return element1.name.length() > element2.name.length() ? 1 : (element1.name.length() < element2.name.length() ? -1 : element1.name.compareTo(element2.name));
                                                }).collect(Collectors.toList());
                                                reply(ctx, Type.SELECT_BOX, boxSorted.toArray(new BoxData[0]));
                                            } else if(msg.boxes.length == 1) {
                                                int hashWanted = msg.boxes[0].posHash;
                                                Set<Location> hashes = new HashSet<>(boxPos);
                                                hashes.removeIf(p -> p.hashCode() != hashWanted);
                                                if(hashes.size() == 1) {
                                                    Location selected = hashes.toArray(new Location[1])[0];
                                                    System.out.println(selected.getX() + " " + selected.getY() + " " + selected.getZ() + " - " + selected.getDimension().getRegistryName().toString());
                                                    // need to verify box still exists in the world before replying success + delivery
                                                    reply(ctx, Type.SUCCESS);
                                                }
                                            }
                                        } else {
                                            reply(ctx, Type.NO_BOXES);
                                        }
                                    } else {
                                        reply(ctx, Type.INVALID_ADDRESS);
                                    }
                                } else {
                                    reply(ctx, Type.NO_ADDRESS);
                                }
                            } else {
                                reply(ctx, Type.NO_STAMP);
                            }
                        } else {
                            reply(ctx, Type.NO_ENVELOPE);
                        }
                    }
                });
            }
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT && msg.type != Type.TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    if(Minecraft.getInstance().currentScreen instanceof IEnvelopePacketReceiver) {
                        ((IEnvelopePacketReceiver) Minecraft.getInstance().currentScreen).receivePacket(msg);
                    }
                });
            }
            ctx.get().setPacketHandled(true);
        }

        private static void reply(Supplier<NetworkEvent.Context> ctx, Type response) {
            SnailMail.HANDLER.sendTo(new SendEnvelopePacket(response), ctx.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        }

        private static void reply(Supplier<NetworkEvent.Context> ctx, Type response, BoxData... boxData) {
            SnailMail.HANDLER.sendTo(new SendEnvelopePacket(response, boxData), ctx.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        }

    }

}
