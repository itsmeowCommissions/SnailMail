package dev.itsmeow.snailmail.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity.SnailBoxContainer;
import dev.itsmeow.snailmail.client.screen.SnailBoxMemberScreen;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateSnailBoxPacket {

    public enum Type {
        NAME,
        MEMBER,
        PUBLIC
    }
    public String name = "";
    public String memberUsername = "";
    public boolean addMember;
    public boolean isPublic;
    public final Type type;

    public UpdateSnailBoxPacket(String name) {
        this.type = Type.NAME;
        this.name = name;
    }

    public UpdateSnailBoxPacket(String memberUsername, boolean add) {
        this.type = Type.MEMBER;
        this.memberUsername = memberUsername;
        this.addMember = add;
    }

    public UpdateSnailBoxPacket(boolean isPublic) {
        this.type = Type.PUBLIC;
        this.isPublic = isPublic;
    }

    public static void encode(UpdateSnailBoxPacket pkt, PacketBuffer buf) {
        buf.writeInt(pkt.type.ordinal());
        switch(pkt.type) {
        case NAME:
            buf.writeString(pkt.name, 35);
            break;
        case MEMBER:
            buf.writeString(pkt.memberUsername, 35);
            buf.writeBoolean(pkt.addMember);
            break;
        case PUBLIC:
            buf.writeBoolean(pkt.isPublic);
            break;
        }
    }

    public static UpdateSnailBoxPacket decode(PacketBuffer buf) {
        Type type = Type.values()[buf.readInt()];
        switch(type) {
        case NAME:
            return new UpdateSnailBoxPacket(buf.readString(35));
        case MEMBER:
            return new UpdateSnailBoxPacket(buf.readString(35), buf.readBoolean());
        case PUBLIC:
            return new UpdateSnailBoxPacket(buf.readBoolean());
        }
        return null;
    }

    public static class Handler {
        public static void handle(UpdateSnailBoxPacket msg, Supplier<NetworkEvent.Context> ctx) {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity sender = ctx.get().getSender();
                    if(sender.openContainer instanceof SnailBoxContainer) {
                        SnailBoxBlockEntity te = ((SnailBoxContainer) sender.openContainer).getTile(sender);
                        if(te != null && te.getOwner().equals(PlayerEntity.getUUID(sender.getGameProfile()))) {
                            switch(msg.type) {
                            case NAME:
                                String newName = RandomUtil.filterAllowedCharacters(msg.name, true);
                                if(newName.length() <= 35) {
                                    te.setName(newName);
                                }
                                break;
                            case MEMBER:
                                String username = msg.memberUsername;
                                GameProfile profile = sender.getServer().getPlayerProfileCache().getGameProfileForUsername(username);
                                UUID uuid = null;
                                if((profile == null || profile.getId() == null) && UUID.fromString(username) != null) {
                                    uuid = UUID.fromString(username);
                                    if(msg.addMember) {
                                        te.addMember(uuid);
                                    } else {
                                        te.removeMember(uuid);
                                    }
                                } else if(profile != null && profile.getId() != null) {
                                    uuid = profile.getId();
                                    if(msg.addMember) {
                                        te.addMember(profile.getId());
                                    } else {
                                        te.removeMember(profile.getId());
                                    }
                                } else if(msg.addMember) {
                                    reply(ctx, "FAILED_ID_VERIFY", false);
                                }
                                if(uuid != null && (!uuid.equals(te.getOwner()) || !msg.addMember)) {
                                    reply(ctx, username, msg.addMember);
                                }
                                break;
                            case PUBLIC:
                                te.setPublic(msg.isPublic);
                                break;
                            }
                        }

                    }
                });
            } else if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT && msg.type == Type.MEMBER) {
                ctx.get().enqueueWork(() -> {
                    if(Minecraft.getInstance().currentScreen instanceof SnailBoxMemberScreen) {
                        SnailBoxMemberScreen screen = (SnailBoxMemberScreen) Minecraft.getInstance().currentScreen;
                        String name = msg.memberUsername;
                        if(!name.equals("FAILED_ID_VERIFY")) {
                            if(msg.addMember) {
                                screen.parent.getContainer().memberUsers.add(name);
                            } else {
                                screen.parent.getContainer().memberUsers.remove(name);
                            }
                            screen.refreshList();
                        } else {
                            screen.failedAdd();
                        }
                    }
                });
            }
            ctx.get().setPacketHandled(true);
        }

    }

    private static void reply(Supplier<NetworkEvent.Context> ctx, String name, boolean added) {
        SnailMail.HANDLER.sendTo(new UpdateSnailBoxPacket(name, added), ctx.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    }

}
