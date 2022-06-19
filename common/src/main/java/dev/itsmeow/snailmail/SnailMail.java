package dev.itsmeow.snailmail;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.ExplosionEvent;
import dev.architectury.registry.CreativeTabRegistry;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.*;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.util.BiMultiMap;
import dev.itsmeow.snailmail.util.Location;
import dev.itsmeow.snailmail.util.SnailMailCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class SnailMail {

    public static final String MODID = "snailmail";
    public static CreativeModeTab ITEM_GROUP = CreativeTabRegistry.create(new ResourceLocation(MODID, "main"), () -> new ItemStack(ModItems.ENVELOPE_CLOSED.get()));;

    public static void construct() {
        ModEntities.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModNetwork.init();
        BlockEvent.PLACE.register((level, pos, state, entity) -> {
            if(entity instanceof Player && !level.isClientSide()) {
                UUID uuid = UUIDUtil.getOrCreatePlayerUUID(((Player) entity).getGameProfile());
                BlockEntity teB = level.getBlockEntity(pos);
                if(teB != null && teB instanceof SnailBoxBlockEntity) {
                    Set<Location> box = SnailBoxSavedData.getOrCreate(level).getBoxes(uuid);
                    int size = box == null ? 0 : box.size();
                    ((SnailBoxBlockEntity) teB).initializeOwner(uuid, ((Player) entity).getGameProfile().getName() + " Snailbox #" + (size + 1), false);
                }
            }
            return EventResult.pass();
        });
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if(state.getBlock() == ModBlocks.SNAIL_BOX.get()) {
                BlockEntity teB = level.getBlockEntity(pos);
                if(teB instanceof SnailBoxBlockEntity) {
                    if(SnailMailCommonConfig.protectBoxDestroy() && !((SnailBoxBlockEntity) teB).canAccess(player)) {
                        return EventResult.interruptFalse();
                    } else {
                        SnailBoxSavedData.getOrCreate(level).removeBoxRaw(new Location(level, pos));
                    }
                }
            }
            return EventResult.pass();
        });
        ExplosionEvent.DETONATE.register((level, explosion, affected) -> {
            ArrayList<BlockPos> list = new ArrayList<>();
            explosion.getToBlow().forEach(pos -> {
                if(level.getBlockState(pos).getBlock() == ModBlocks.SNAIL_BOX.get()) {
                    list.add(pos);
                }
            });
            for(BlockPos pos : list) {
                explosion.getToBlow().remove(pos);
            }
        });
        ChunkEvent.LOAD_DATA.register((chunk, level, nbt) -> {
            ChunkPos cPos = chunk.getPos();
            Location[] posL = SnailBoxSavedData.getOrCreate(level).getAllBoxes().toArray(new Location[0]);
            for(int i = 0; i < posL.length; i++) {
                Location loc = posL[i];
                if(loc.getDimension().equals(level.dimension())) {
                    if(cPos.getMinBlockX() <= loc.getX() && cPos.getMaxBlockX() >= loc.getX()) {
                        if(cPos.getMinBlockZ() <= loc.getZ() && cPos.getMaxBlockZ() >= loc.getZ()) {
                            BlockState state = chunk.getBlockState(loc.toBP());
                            if(state.getBlock() != ModBlocks.SNAIL_BOX.get()) {
                                SnailBoxSavedData.getOrCreate(level).removeBoxRaw(loc);
                            }
                        }
                    }
                }
            }
        });
    }

    public static class SnailBoxSavedData extends SavedData {

        private final BiMultiMap<UUID, Location> snailBoxes;
        private final BiMultiMap<UUID, Location> members;
        private final Map<Location, String> names;
        private final Map<Location, Boolean> publicM;

        public SnailBoxSavedData() {
            snailBoxes = new BiMultiMap<>();
            members = new BiMultiMap<>();
            names = new HashMap<>();
            publicM = new HashMap<>();
        }

        public SnailBoxSavedData(BiMultiMap<UUID, Location> snailBoxes, BiMultiMap<UUID, Location> members, Map<Location, String> names, Map<Location, Boolean> publicM) {
            this.snailBoxes = snailBoxes;
            this.members = members;
            this.names = names;
            this.publicM = publicM;
        }

        public void updateAll(UUID owner, String name, boolean publicBox, Set<UUID> membersIn, Location pos) {
            snailBoxes.put(owner, pos);
            names.put(pos, name);
            publicM.put(pos, publicBox);
            for(UUID member : membersIn) {
                members.put(member, pos);
            }
            this.setDirty();
        }

        public void update(UUID owner, String name, boolean forceName, Location pos) {
            if(forceName || !names.containsKey(pos)) {
                names.put(pos, name);
            }
            snailBoxes.putIfAbsent(owner, pos);
            this.setDirty();
        }

        public void moveBox(Location oldLoc, Location newLoc) {
            UUID owner = this.getOwner(oldLoc);
            String name = names.remove(oldLoc);
            boolean publicB = this.isPublic(oldLoc);
            Set<UUID> memberL = members.getKeys(oldLoc);
            this.removeBoxRaw(oldLoc);
            this.updateAll(owner, name, publicB, memberL, newLoc);
        }

        public void removeBoxRaw(Location pos) {
            snailBoxes.removeValueFromAll(pos);
            names.remove(pos);
            members.removeValueFromAll(pos);
            publicM.remove(pos);
            this.setDirty();
        }

        public Set<Location> getBoxes(UUID owner) {
            return snailBoxes.getValues(owner);
        }

        public Set<Location> getMemberBoxes(UUID member) {
            return members.getValues(member);
        } 

        public Set<Location> getAllBoxes() {
            return snailBoxes.getValuesToKeys().keySet();
        }

        public String getNameForPos(Location pos) {
            return names.get(pos);
        }

        public void setNameForPos(Location pos, String name) {
            names.put(pos, name);
            this.setDirty();
        }

        public boolean isPublic(Location pos) {
            return publicM.containsKey(pos) && publicM.get(pos);
        }

        public boolean setPublic(Location pos, boolean value) {
            // need to store as object type due to unboxing null
            Boolean val = publicM.put(pos, value);
            this.setDirty();
            return val != null && val;
        }

        public boolean isMemberOf(Location pos, UUID uuid) {
            return members.getValuesToKeys().containsEntry(pos, uuid);
        }

        public UUID getOwner(Location location) {
            UUID[] uuid = snailBoxes.getValuesToKeys().get(location).toArray(new UUID[0]);
            return uuid.length == 1 ? uuid[0] : null;
        }

        public void addMember(UUID uuid, Location location) {
            members.put(uuid, location);
            this.setDirty();
        }

        public void removeMember(UUID uuid, Location location) {
            members.remove(uuid, location);
            this.setDirty();
        }

        public Set<UUID> getMembers(Location location) {
            return members.getKeys(location);
        }

        @Override
        public CompoundTag save(CompoundTag compound) {
            snailBoxes.getKeysToValues().keySet().forEach(key -> {
                ListTag list = new ListTag();
                snailBoxes.getValues(key).forEach(pos -> {
                    CompoundTag comp = new CompoundTag();
                    pos.write(comp);
                    comp.putString("name", names.get(pos));
                    comp.putBoolean("public", publicM.get(pos));
                    ListTag list2 = new ListTag();
                    for(UUID member : this.getMembers(pos)) {
                        list2.add(StringTag.valueOf(member.toString()));
                    }
                    comp.put("members", list2);
                    list.add(comp);
                });
                compound.put(key.toString(), list);
            });
            return compound;
        }

        public static SnailBoxSavedData getOrCreate(Level level) {
            return level.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(compoundTag -> {
                BiMultiMap<UUID, Location> snailBoxes = new BiMultiMap<>();
                BiMultiMap<UUID, Location> members = new BiMultiMap<>();
                Map<Location, String> names = new HashMap<>();
                Map<Location, Boolean> publicM = new HashMap<>();
                for(String key : compoundTag.getAllKeys()) {
                    UUID uuid = UUID.fromString(key);
                    if(uuid != null && compoundTag.contains(key, Tag.TAG_LIST)) {
                        ListTag list = compoundTag.getList(key, Tag.TAG_COMPOUND);
                        for(int i = 0; i < list.size(); i++) {
                            CompoundTag comp = list.getCompound(i);
                            Location pos = Location.read(comp);
                            snailBoxes.put(uuid, pos);
                            names.put(pos, comp.getString("name"));
                            publicM.put(pos, comp.getBoolean("public"));
                            ListTag mList = comp.getList("members", Tag.TAG_STRING);
                            for(int j = 0; j < mList.size(); j++) {
                                members.put(UUID.fromString(mList.getString(j)), pos);
                            }
                        }
                    }
                }
                return new SnailBoxSavedData(snailBoxes, members, names, publicM);
            }, SnailBoxSavedData::new, "SNAIL_BOXES");
        }

    }

    public static void forceArea(ServerLevel world, BlockPos pos, boolean type) {
        world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, type);
        world.setChunkForced(pos.getX() >> 4 + 1, pos.getZ() >> 4, type);
        world.setChunkForced(pos.getX() >> 4 - 1, pos.getZ() >> 4, type);
        world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4 + 1, type);
        world.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4 - 1, type);
    }

    public static boolean deliverTo(SnailBoxBlockEntity te, ItemStack envelope, boolean failed) {
        if (SnailBoxBlockEntity.hasCapability(te)) {
            Optional<ItemStack> iOpt = EnvelopeItem.convert(envelope);
            if(iOpt.isPresent()) {
                ItemStack newEnvelope = iOpt.get();
                if(failed) {
                    if(!newEnvelope.hasTag()) {
                        newEnvelope.setTag(new CompoundTag());
                    }
                    newEnvelope.getTag().putBoolean("delivery_failed", true);
                }
                return SnailBoxBlockEntity.tryInsert(te, newEnvelope);
            }
        }
        return false;
    }
}
