package dev.itsmeow.snailmail.block.entity;

import com.mojang.authlib.GameProfile;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.itsmeow.snailmail.SnailMail.SnailBoxSavedData;
import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.menu.SnailBoxMenu;
import dev.itsmeow.snailmail.util.Location;
import me.shedaniel.architectury.registry.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SnailBoxBlockEntity extends BlockEntity {

    public static final int SLOT_COUNT = 28;
    public static final Component TITLE = new TranslatableComponent("container.snailmail.snail_box");

    public SnailBoxBlockEntity() {
        super(ModBlockEntities.SNAIL_BOX.get());
    }

    protected SnailBoxSavedData data() {
        return SnailBoxSavedData.getData(this.getLevel().getServer());
    }

    public boolean isPublic() {
        return data().isPublic(this.getLocation());
    }

    public boolean isMember(UUID uuid) {
        return data().isMemberOf(this.getLocation(), uuid);
    }

    public UUID getOwner() {
        return data().getOwner(this.getLocation());
    }

    public void initializeOwner(UUID uuid, String name, boolean forceName) {
        data().update(uuid, name, forceName, this.getLocation());
        data().setPublic(this.getLocation(), false);
    }

    public void addMember(UUID uuid) {
        data().addMember(uuid, this.getLocation());
    }

    public void removeMember(UUID uuid) {
        data().removeMember(uuid, this.getLocation());
    }

    public void setPublic(boolean publicB) {
        data().setPublic(this.getLocation(), publicB);
    }

    public Set<UUID> getMembers() {
        return data().getMembers(this.getLocation());
    }

    @Override
    public void setRemoved() {
        this.handleRemoved(this);
        super.setRemoved();
    }

    @ExpectPlatform
    private static void handleRemoved(SnailBoxBlockEntity blockEntity) {}

    public void setName(String name) {
        data().setNameForPos(this.getLocation(), name);
    }

    @Override
    public void setLevelAndPosition(Level newWorld, BlockPos newPos) {
        if(worldPosition != null && level != null && (newWorld != level || newPos != worldPosition)) {
            data().moveBox(this.getLocation(), new Location(newWorld, newPos));
        }
        super.setLevelAndPosition(newWorld, newPos);
    }

    @Override
    public void setPosition(BlockPos newPos) {
        if(worldPosition != null && level != null && newPos != worldPosition) {
            data().moveBox(this.getLocation(), new Location(level, newPos));
        }
        super.setPosition(newPos);
    }

    @Override
    public void clearCache() {
        // remove if air
        if(!this.getLevel().isClientSide && level.isLoaded(this.getBlockPos()) && level.getBlockState(this.getBlockPos()).getBlock() != ModBlocks.SNAIL_BOX.get()) {
            SnailBoxSavedData.getData(this.getLevel().getServer()).removeBoxRaw(this.getLocation());
        }
    }

    @Override
    public void load(BlockState state, CompoundTag compound) {
        super.load(state, compound);
        loadStorage(this, compound);
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        saveStorage(this, compound);
        return compound;
    }

    @ExpectPlatform
    public static void loadStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {}

    @ExpectPlatform
    public static void saveStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {}

    public void openGUI(ServerPlayer player) {
        // do it on another thread so as to not block if some usernames need to be retrieved from servers
        new Thread(() -> {
            MenuConstructor provider = getServerMenuProvider(this);
            MenuProvider namedProvider = new SimpleMenuProvider(provider, TITLE);
            MenuRegistry.openExtendedMenu(player, namedProvider, buf -> {
                buf.writeBlockPos(this.getBlockPos());
                String name = data().getNameForPos(this.getLocation());
                if(name == null) {
                    name = "";
                }
                buf.writeUtf(name, 35);
                buf.writeBoolean(Player.createPlayerUUID(player.getGameProfile()).equals(this.getOwner()));
                buf.writeBoolean(this.isPublic());
                Set<String> usernames = new HashSet<String>();

                for(UUID member : this.getMembers()) {
                    GameProfile profile = player.getServer().getProfileCache().get(member);
                    if(profile != null && profile.getName() != null && !profile.getName().isEmpty()) {
                        usernames.add(profile.getName());
                    } else {
                        usernames.add(member.toString());
                    }
                }
                buf.writeInt(usernames.size());
                for(String username : usernames) {
                    buf.writeUtf(username);
                }
            });
        }).start();
    }

    @ExpectPlatform
    public static void dropItems(SnailBoxBlockEntity blockEntity) {}

    @ExpectPlatform
    public static SnailBoxMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        return null;
    }

    @ExpectPlatform
    public static MenuConstructor getServerMenuProvider(SnailBoxBlockEntity te) {
        return null;
    }

    public Location getLocation() {
        return new Location(this.getLevel(), this.getBlockPos());
    }

    @ExpectPlatform
    public static ItemStack getEnvelope(SnailBoxBlockEntity blockEntity) {
        return null;
    }

    @ExpectPlatform
    public static void setEnvelope(SnailBoxBlockEntity blockEntity, ItemStack stack) {}

    @ExpectPlatform
    public static boolean setEnvelopeServer(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        return false;
    }

    @ExpectPlatform
    public static boolean hasCapability(SnailBoxBlockEntity te) {
        return false;
    }

    @ExpectPlatform
    public static boolean tryInsert(SnailBoxBlockEntity te, ItemStack newEnvelope) {
        return false;
    }

    public boolean canAccess(ServerPlayer sender) {
        return SnailBoxBlock.isAccessibleFor(this, Player.createPlayerUUID(sender.getGameProfile()));
    }
}
