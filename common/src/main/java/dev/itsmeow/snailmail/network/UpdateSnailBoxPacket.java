package dev.itsmeow.snailmail.network;

import com.mojang.authlib.GameProfile;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.client.screen.SnailBoxMemberScreen;
import dev.itsmeow.snailmail.init.ModNetwork;
import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import dev.itsmeow.snailmail.util.RandomUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

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

    public static void encode(UpdateSnailBoxPacket pkt, FriendlyByteBuf buf) {
        buf.writeInt(pkt.type.ordinal());
        switch(pkt.type) {
        case NAME:
            buf.writeUtf(pkt.name, 35);
            break;
        case MEMBER:
            buf.writeUtf(pkt.memberUsername, 35);
            buf.writeBoolean(pkt.addMember);
            break;
        case PUBLIC:
            buf.writeBoolean(pkt.isPublic);
            break;
        }
    }

    public static UpdateSnailBoxPacket decode(FriendlyByteBuf buf) {
        Type type = Type.values()[buf.readInt()];
        switch(type) {
        case NAME:
            return new UpdateSnailBoxPacket(buf.readUtf(35));
        case MEMBER:
            return new UpdateSnailBoxPacket(buf.readUtf(35), buf.readBoolean());
        case PUBLIC:
            return new UpdateSnailBoxPacket(buf.readBoolean());
        }
        return null;
    }

    public static class Handler {
        public static void handle(UpdateSnailBoxPacket msg, Supplier<NetworkManager.PacketContext> ctx) {
            if(ctx.get().getEnvironment() == Env.SERVER) {
                ctx.get().queue(() -> {
                    ServerPlayer sender = (ServerPlayer) ctx.get().getPlayer();
                    if(sender.containerMenu instanceof SnailBoxMenu) {
                        SnailBoxBlockEntity te = ((SnailBoxMenu) sender.containerMenu).getTile(sender);
                        if(te != null && te.getOwner().equals(UUIDUtil.getOrCreatePlayerUUID(sender.getGameProfile()))) {
                            switch(msg.type) {
                            case NAME:
                                String newName = RandomUtil.filterAllowedCharacters(msg.name, true);
                                if(newName.length() <= 35) {
                                    te.setName(newName);
                                }
                                break;
                            case MEMBER:
                                String username = msg.memberUsername;
                                // move to another thread so as not to block server main thread
                                new Thread(() -> {
                                    Optional<GameProfile> profile = sender.getServer().getProfileCache().get(username);
                                    // back to server main thread!
                                    ctx.get().queue(() -> {
                                        UUID uuid = null;
                                        if((profile.isEmpty() || profile.get().getId() == null) && UUID.fromString(username) != null) {
                                            uuid = UUID.fromString(username);
                                            if(!uuid.equals(te.getOwner())) {
                                                if(msg.addMember) {
                                                    te.addMember(uuid);
                                                } else {
                                                    te.removeMember(uuid);
                                                }
                                            }
                                        } else if(profile.isPresent() && profile.get().getId() != null) {
                                            uuid = profile.get().getId();
                                            if(!uuid.equals(te.getOwner())) {
                                                if(msg.addMember) {
                                                    te.addMember(profile.get().getId());
                                                } else {
                                                    te.removeMember(profile.get().getId());
                                                }
                                            }
                                        } else if(msg.addMember) {
                                            reply(ctx, "FAILED_ID_VERIFY", false);
                                        }
                                        if(uuid != null && (!uuid.equals(te.getOwner()) || !msg.addMember)) {
                                            reply(ctx, username, msg.addMember);
                                        }
                                    });
                                }).start();
                                break;
                            case PUBLIC:
                                te.setPublic(msg.isPublic);
                                break;
                            }
                        }

                    }
                });
            } else if(ctx.get().getEnvironment() == Env.CLIENT && msg.type == Type.MEMBER) {
                ctx.get().queue(() -> {
                    if(Minecraft.getInstance().screen instanceof SnailBoxMemberScreen) {
                        SnailBoxMemberScreen screen = (SnailBoxMemberScreen) Minecraft.getInstance().screen;
                        String name = msg.memberUsername;
                        if(!name.equals("FAILED_ID_VERIFY")) {
                            if(msg.addMember) {
                                screen.parent.getMenu().memberUsers.add(name);
                            } else {
                                screen.parent.getMenu().memberUsers.remove(name);
                            }
                            screen.refreshList();
                        } else {
                            screen.failedAdd();
                        }
                    }
                });
            }
        }

    }

    private static void reply(Supplier<NetworkManager.PacketContext> ctx, String name, boolean added) {
        ModNetwork.HANDLER.sendToPlayer((ServerPlayer) ctx.get().getPlayer(), new UpdateSnailBoxPacket(name, added));
    }

}
