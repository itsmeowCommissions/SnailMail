package net.examplemod.network;

import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.examplemod.SnailMail;
import net.examplemod.block.entity.SnailBoxBlockEntity;
import net.examplemod.client.screen.IEnvelopePacketReceiver;
import net.examplemod.entity.SnailManEntity;
import net.examplemod.init.ModBlocks;
import net.examplemod.init.ModEntities;
import net.examplemod.init.ModItems;
import net.examplemod.init.ModNetwork;
import net.examplemod.item.EnvelopeItem;
import net.examplemod.menu.SnailBoxMenu;
import net.examplemod.util.BoxData;
import net.examplemod.util.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SendEnvelopePacket {
    public enum Type {
        TO_SERVER,
        NO_ENVELOPE,
        NO_STAMP,
        NO_ADDRESS,
        INVALID_ADDRESS,
        NO_BOXES,
        SELECT_BOX,
        SUCCESS,
        BOX_NO_EXIST,
        WAIT,
        REOPEN
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

    public static void encode(SendEnvelopePacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.type.ordinal());
        if(pkt.boxes != null) {
            buf.writeInt(pkt.boxes.length);
            for(BoxData box : pkt.boxes) {
                box.write(buf);
            }
        }
    }

    public static SendEnvelopePacket decode(FriendlyByteBuf buf) {
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
        public static void handle(SendEnvelopePacket msg, Supplier<NetworkManager.PacketContext> ctx) {
            if(ctx.get().getEnvironment() == Env.SERVER && msg.type == Type.TO_SERVER) {
                ctx.get().queue(() -> {
                    ServerPlayer sender = (ServerPlayer) ctx.get().getPlayer();
                    if(sender.containerMenu instanceof SnailBoxMenu) {
                        ItemStack stack = sender.containerMenu.getSlot(27).getItem();
                        if(!stack.isEmpty() && stack.getItem() == ModItems.ENVELOPE_OPEN.get()) {
                            if(EnvelopeItem.isStamped(stack)) {
                                String to = EnvelopeItem.getString(stack, "AddressedTo");
                                if(!to.isEmpty()) {
                                    reply(ctx, Type.WAIT);
                                    // move to another thread so as not to block server main thread
                                    new Thread(() -> {
                                        Optional<GameProfile> prof = sender.getServer().getProfileCache().get(to);
                                        // back to server main thread!
                                        ctx.get().queue(() -> {
                                            if(prof.isPresent() && prof.get().getId() != null) {
                                                UUID uuid = prof.get().getId();
                                                SnailMail.SnailBoxSavedData data = SnailMail.SnailBoxSavedData.getOrCreate(sender.getLevel());
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
                                                            SnailBoxBlockEntity fromTe = ((SnailBoxMenu) sender.containerMenu).getTile(sender);
                                                            final Location selectFinal = selected;
                                                            ServerLevel world = selectFinal.getWorld(sender.getServer());
                                                            BlockPos pos = selectFinal.toBP();
                                                            reply(ctx, Type.WAIT);
                                                            new Thread(() -> {
                                                                // may need to generate chunks
                                                                SnailMail.forceArea(world, pos, true);
                                                                // back to main thread
                                                                ctx.get().queue(() -> {
                                                                    if(world.isLoaded(pos)) {
                                                                        BlockEntity teB = world.getBlockEntity(pos);
                                                                        if(world.getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX.get() && teB != null && teB instanceof SnailBoxBlockEntity) {
                                                                            deliver(fromTe, stack, selectFinal, fromTe.getLocation(), sender);
                                                                            reply(ctx, Type.SUCCESS);
                                                                        } else {
                                                                            reply(ctx, Type.BOX_NO_EXIST);
                                                                            SnailMail.SnailBoxSavedData.getOrCreate(sender.getLevel()).removeBoxRaw(selectFinal);
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
                    } else {
                        reply(ctx, Type.REOPEN);
                    }
                });
            }
            if(ctx.get().getEnvironment() == Env.CLIENT && msg.type != Type.TO_SERVER) {
                ctx.get().queue(() -> {
                    if(msg.type == Type.REOPEN && Minecraft.getInstance().player.containerMenu instanceof SnailBoxMenu) {
                        ModNetwork.HANDLER.sendToServer(new OpenSnailBoxGUIPacket(((SnailBoxMenu) Minecraft.getInstance().player.containerMenu).pos));
                    } else {
                        if (Minecraft.getInstance().screen instanceof IEnvelopePacketReceiver) {
                            ((IEnvelopePacketReceiver) Minecraft.getInstance().screen).receivePacket(msg);
                        }
                    }
                });
            }
        }

        private static void reply(Supplier<NetworkManager.PacketContext> ctx, Type response) {
            ModNetwork.HANDLER.sendToPlayer((ServerPlayer) ctx.get().getPlayer(), new SendEnvelopePacket(response));
        }

        private static void reply(Supplier<NetworkManager.PacketContext> ctx, Type response, BoxData... boxData) {
            ModNetwork.HANDLER.sendToPlayer((ServerPlayer) ctx.get().getPlayer(), new SendEnvelopePacket(response, boxData));
        }

    }

    public static boolean deliver(SnailBoxBlockEntity fromTe, ItemStack stack, Location location, Location from, ServerPlayer player) {
        stack = stack.copy();
        if(!SnailBoxBlockEntity.setEnvelopeServer(fromTe, ItemStack.EMPTY)) {
            return false;
        }
        ServerLevel fromW = from.getWorld(player.getServer());
        try {
            SnailManEntity snail = new SnailManEntity(ModEntities.SNAIL_MAN.get(), fromW, location, stack, from);
            snail.finalizeSpawn(fromW, fromW.getCurrentDifficultyAt(from.toBP()), MobSpawnType.MOB_SUMMONED, null, null);
            BlockPos pos = from.toBP().relative(fromTe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));
            snail.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
            fromW.addFreshEntity(snail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
