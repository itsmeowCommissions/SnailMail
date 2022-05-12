package dev.itsmeow.snailmail;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.*;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.util.BiMultiMap;
import dev.itsmeow.snailmail.util.Location;
import me.shedaniel.architectury.event.events.BlockEvent;
import me.shedaniel.architectury.event.events.ChunkEvent;
import me.shedaniel.architectury.event.events.ExplosionEvent;
import me.shedaniel.architectury.registry.CreativeTabs;
import me.shedaniel.architectury.utils.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.*;

public class SnailMail {

    public static final String MODID = "snailmail";
    public static CreativeModeTab ITEM_GROUP = CreativeTabs.create(new ResourceLocation(MODID, "main"), () -> new ItemStack(ModItems.ENVELOPE_CLOSED.get()));;

    public static void construct() {
        ModEntities.init();
        ModBlocks.init();
        ModItems.init();
        ModBlockEntities.init();
        ModMenus.init();
        ModNetwork.init();
        BlockEvent.PLACE.register((level, pos, state, entity) -> {
            if(entity instanceof Player && !level.isClientSide()) {
                UUID uuid = Player.createPlayerUUID(((Player) entity).getGameProfile());
                BlockEntity teB = level.getBlockEntity(pos);
                if(teB != null && teB instanceof SnailBoxBlockEntity) {
                    Set<Location> box = SnailBoxSavedData.getData(level.getServer()).getBoxes(uuid);
                    int size = box == null ? 0 : box.size();
                    ((SnailBoxBlockEntity) teB).initializeOwner(uuid, ((Player) entity).getGameProfile().getName() + " Snailbox #" + (size + 1), false);
                }
            }
            return InteractionResult.PASS;
        });
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if(state.getBlock() == ModBlocks.SNAIL_BOX.get()) {
                UUID uuid = Player.createPlayerUUID(player.getGameProfile());
                BlockEntity teB = level.getBlockEntity(pos);
                if(teB != null && teB instanceof SnailBoxBlockEntity) {
                    UUID owner = ((SnailBoxBlockEntity) teB).getOwner();
                    if(owner != null && !uuid.equals(owner) /*TODO && Configuration.get().PROTECT_BOX_DESTROY.get()*/) {
                        return InteractionResult.FAIL;
                    } else {
                        SnailBoxSavedData.getData(level.getServer()).removeBoxRaw(new Location(level, pos));
                    }
                }
            }
            return InteractionResult.PASS;
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
            Location[] posL = SnailBoxSavedData.getData(level.getServer()).getAllBoxes().toArray(new Location[0]);
            for(int i = 0; i < posL.length; i++) {
                Location loc = posL[i];
                if(loc.getDimension().equals(level.dimension())) {
                    if(cPos.getMinBlockX() <= loc.getX() && cPos.getMaxBlockX() >= loc.getX()) {
                        if(cPos.getMinBlockZ() <= loc.getZ() && cPos.getMaxBlockZ() >= loc.getZ()) {
                            BlockState state = chunk.getBlockState(loc.toBP());
                            if(state.getBlock() != ModBlocks.SNAIL_BOX.get()) {
                                SnailBoxSavedData.getData(level.getServer()).removeBoxRaw(loc);
                            }
                        }
                    }
                }
            }
        });
    }

    public static class SnailBoxSavedData extends SavedData {

        private final BiMultiMap<UUID, Location> snailBoxes = new BiMultiMap<>();
        private final BiMultiMap<UUID, Location> members = new BiMultiMap<>();
        private final Map<Location, String> names = new HashMap<>();
        private final Map<Location, Boolean> publicM = new HashMap<>();

        public SnailBoxSavedData() {
            super("SNAIL_BOXES");
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
        public void load(CompoundTag nbt) {
            for(String key : nbt.getAllKeys()) {
                UUID uuid = UUID.fromString(key);
                if(uuid != null && nbt.contains(key, NbtType.LIST)) {
                    ListTag list = nbt.getList(key, NbtType.COMPOUND);
                    for(int i = 0; i < list.size(); i++) {
                        CompoundTag comp = list.getCompound(i);
                        Location pos = Location.read(comp);
                        snailBoxes.put(uuid, pos);
                        names.put(pos, comp.getString("name"));
                        publicM.put(pos, comp.getBoolean("public"));
                        ListTag mList = comp.getList("members", NbtType.STRING);
                        for(int j = 0; j < mList.size(); j++) {
                            members.put(UUID.fromString(mList.getString(j)), pos);
                        }
                    }
                }
            }
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

        public static SnailBoxSavedData getData(MinecraftServer server) {
            ServerLevel world = server.getLevel(Level.OVERWORLD);
            DimensionDataStorage data = world.getDataStorage();
            SnailBoxSavedData a = data.computeIfAbsent(SnailBoxSavedData::new, "SNAIL_BOXES");
            return a;
        }

    }
/* TODO config

    public static class Configuration {
        public static ForgeConfigSpec SPEC = null;
        protected static Configuration INSTANCE = null;

        public static Configuration get() {
            return INSTANCE;
        }

        public static ForgeConfigSpec initSpec() {
            final Pair<Configuration, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Configuration::new);
            SPEC = specPair.getRight();
            INSTANCE = specPair.getLeft();
            return specPair.getRight();
        }

        public final ForgeConfigSpec.BooleanValue LOCK_BOXES;
        public final ForgeConfigSpec.BooleanValue PROTECT_BOX_DESTROY;

        protected Configuration(ForgeConfigSpec.Builder builder) {
            LOCK_BOXES = builder.comment("Block snailboxes from being opened by non-owners").define("lock_boxes", true);
            PROTECT_BOX_DESTROY = builder.comment("Protect snailboxes from being destroyed by non-owners").define("protect_box_destroy", true);
        }
    }
*/
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