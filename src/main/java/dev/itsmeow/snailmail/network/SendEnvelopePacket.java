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
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.client.screen.IEnvelopePacketReceiver;
import dev.itsmeow.snailmail.entity.SnailManEntity;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.util.BoxData;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class SendEnvelopePacket {

    public static enum Type {
        TO_SERVER,
        NO_ENVELOPE,
        NO_STAMP,
        NO_ADDRESS,
        INVALID_ADDRESS,
        NO_BOXES,
        SELECT_BOX,
        SUCCESS,
        BOX_NO_EXIST,
        WAIT
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
                                    reply(ctx, Type.WAIT);
                                    // move to another thread so as not to block server main thread
                                    new Thread(() -> {
                                        GameProfile prof = sender.getServer().getPlayerProfileCache().getGameProfileForUsername(to);
                                        // back to server main thread!
                                        ctx.get().enqueueWork(() -> {
                                            if(prof != null && prof.getId() != null) {
                                                UUID uuid = prof.getId();
                                                SnailBoxData data = SnailBoxData.getData(sender.getServer());
                                                Set<Location> boxPos = data.getBoxes(uuid);
                                                Set<BoxData> boxes = new HashSet<BoxData>();
                                                for(Location pos : boxPos) {
                                                    boxes.add(new BoxData(data.getNameForPos(pos), pos, data.isPublic(pos), false));
                                                }
                                                Set<Location> boxPosMember = data.getMemberBoxes(uuid);
                                                for(Location pos : boxPosMember) {
                                                    boxes.add(new BoxData(data.getNameForPos(pos), pos, data.isPublic(pos), true));
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
                                                        Location selected = null;
                                                        if(hashes.size() == 1) {
                                                            selected = hashes.toArray(new Location[1])[0];
                                                        } else {
                                                            Set<Location> hashesM = new HashSet<>(boxPosMember);
                                                            hashesM.removeIf(p -> p.hashCode() != hashWanted);
                                                            if(hashesM.size() == 1) {
                                                                selected = hashesM.toArray(new Location[1])[0];
                                                            }
                                                        }
                                                        if(selected != null) {
                                                            SnailBoxBlockEntity fromTe = ((SnailBoxContainer) sender.openContainer).getTile(sender);
                                                            final Location selectFinal = selected;
                                                            ServerWorld world = selectFinal.getWorld(sender.getServer());
                                                            BlockPos pos = selectFinal.toBP();
                                                            reply(ctx, Type.WAIT);
                                                            new Thread(() -> {
                                                                // may need to generate chunks
                                                                SnailMail.forceArea(world, pos, true);
                                                                // back to main thread
                                                                ctx.get().enqueueWork(() -> {
                                                                    if(world.isBlockPresent(pos)) {
                                                                        TileEntity teB = world.getTileEntity(pos);
                                                                        if(world.getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX && teB != null && teB instanceof SnailBoxBlockEntity) {
                                                                            deliver(fromTe, stack, selectFinal, fromTe.getLocation(), sender);
                                                                            reply(ctx, Type.SUCCESS);
                                                                        } else {
                                                                            reply(ctx, Type.BOX_NO_EXIST);
                                                                            SnailBoxData.getData(sender.getServer()).removeBoxRaw(selectFinal);
                                                                        }
                                                                    }
                                                                });
                                                            }).start();
                                                        }
                                                    }
                                                } else {
                                                    reply(ctx, Type.NO_BOXES);
                                                }
                                            } else {
                                                reply(ctx, Type.INVALID_ADDRESS);
                                            }
                                        });
                                    }).start();
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

    public static boolean deliver(SnailBoxBlockEntity fromTe, ItemStack stack, Location location, Location from, ServerPlayerEntity player) {
        LazyOptional<IItemHandler> hOpt = fromTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if(hOpt.isPresent()) {
            IItemHandler handlerRaw = hOpt.orElse(null);
            if(handlerRaw instanceof ItemStackHandler) {
                ItemStackHandler handler = (ItemStackHandler) handlerRaw;
                stack = stack.copy();
                // remove item
                handler.setStackInSlot(27, ItemStack.EMPTY);
                World fromW = from.getWorld(player.getServer());
                SnailManEntity snail = new SnailManEntity(fromW, location, stack, from);
                snail.onInitialSpawn(fromW, fromW.getDifficultyForLocation(from.toBP()), SpawnReason.MOB_SUMMONED, null, null);
                BlockPos pos = from.toBP().offset(fromTe.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING));
                snail.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                fromW.addEntity(snail);
                return true;
            }
        }
        return false;
    }

}
