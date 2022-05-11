package dev.itsmeow.snailmail;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import dev.itsmeow.snailmail.network.OpenEnvelopeGUIPacket;
import org.apache.commons.lang3.tuple.Pair;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModContainers;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.item.NamedBlockItem;
import dev.itsmeow.snailmail.network.SendEnvelopePacket;
import dev.itsmeow.snailmail.network.SetEnvelopeNamePacket;
import dev.itsmeow.snailmail.network.UpdateSnailBoxPacket;
import dev.itsmeow.snailmail.util.BiMultiMap;
import dev.itsmeow.snailmail.util.Location;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

@Mod.EventBusSubscriber(modid = SnailMail.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = SnailMail.MODID)
public class SnailMail {

    public static final String MODID = "snailmail";
    public static ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModItems.ENVELOPE_CLOSED);
        }
    };
    public static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
    .named(new ResourceLocation(MODID, "main_channel"))
    .clientAcceptedVersions(PROTOCOL_VERSION::equals)
    .serverAcceptedVersions(PROTOCOL_VERSION::equals)
    .networkProtocolVersion(() -> PROTOCOL_VERSION)
    .simpleChannel();
    public static int packets = 0;

    public SnailMail() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Configuration.initSpec());
    }

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        HANDLER.registerMessage(packets++, SetEnvelopeNamePacket.class, SetEnvelopeNamePacket::encode, SetEnvelopeNamePacket::decode, SetEnvelopeNamePacket.Handler::handle);
        HANDLER.registerMessage(packets++, SendEnvelopePacket.class, SendEnvelopePacket::encode, SendEnvelopePacket::decode, SendEnvelopePacket.Handler::handle);
        HANDLER.registerMessage(packets++, UpdateSnailBoxPacket.class, UpdateSnailBoxPacket::encode, UpdateSnailBoxPacket::decode, UpdateSnailBoxPacket.Handler::handle);
        HANDLER.registerMessage(packets++, OpenEnvelopeGUIPacket.class, OpenEnvelopeGUIPacket::encode, OpenEnvelopeGUIPacket::decode, OpenEnvelopeGUIPacket.Handler::handle);
    }

    @SubscribeEvent
    public static void loadComplete(final FMLLoadCompleteEvent event) {

    }

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ModBlocks.SNAIL_BOX);
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
        new NamedBlockItem(ModBlocks.SNAIL_BOX, new Item.Properties().group(ITEM_GROUP)),
        ModItems.ENVELOPE_OPEN,
        ModItems.ENVELOPE_CLOSED,
        ModItems.STAMP);
    }

    @SubscribeEvent
    public static void registerBlockEntities(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(ModBlockEntities.SNAIL_BOX);
    }

    @SubscribeEvent
    public static void registerContainerTypes(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
        ModContainers.SNAIL_BOX,
        ModContainers.ENVELOPE);
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        ModEntities.H.ENTITIES.values().forEach(c -> {
            event.getRegistry().register(c.entityType);
            c.registerAttributes();
        });
    }

    public static class SnailBoxData extends WorldSavedData {

        private final BiMultiMap<UUID, Location> snailBoxes = new BiMultiMap<>();
        private final BiMultiMap<UUID, Location> members = new BiMultiMap<>();
        private final Map<Location, String> names = new HashMap<>();
        private final Map<Location, Boolean> publicM = new HashMap<>();

        public SnailBoxData() {
            super("SNAIL_BOXES");
        }

        public void updateAll(UUID owner, String name, boolean publicBox, Set<UUID> membersIn, Location pos) {
            snailBoxes.put(owner, pos);
            names.put(pos, name);
            publicM.put(pos, publicBox);
            for(UUID member : membersIn) {
                members.put(member, pos);
            }
            this.markDirty();
        }

        public void update(UUID owner, String name, boolean forceName, Location pos) {
            if(forceName || !names.containsKey(pos)) {
                names.put(pos, name);
            }
            snailBoxes.putIfAbsent(owner, pos);
            this.markDirty();
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
            this.markDirty();
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
            this.markDirty();
        }

        public boolean isPublic(Location pos) {
            return publicM.containsKey(pos) && publicM.get(pos);
        }

        public boolean setPublic(Location pos, boolean value) {
            // need to store as object type due to unboxing null
            Boolean val = publicM.put(pos, value);
            this.markDirty();
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
            this.markDirty();
        }

        public void removeMember(UUID uuid, Location location) {
            members.remove(uuid, location);
            this.markDirty();
        }

        public Set<UUID> getMembers(Location location) {
            return members.getKeys(location);
        }

        @Override
        public void read(CompoundNBT nbt) {
            for(String key : nbt.keySet()) {
                UUID uuid = UUID.fromString(key);
                if(uuid != null && nbt.contains(key, Constants.NBT.TAG_LIST)) {
                    ListNBT list = nbt.getList(key, Constants.NBT.TAG_COMPOUND);
                    for(int i = 0; i < list.size(); i++) {
                        CompoundNBT comp = list.getCompound(i);
                        Location pos = Location.read(comp);
                        snailBoxes.put(uuid, pos);
                        names.put(pos, comp.getString("name"));
                        publicM.put(pos, comp.getBoolean("public"));
                        ListNBT mList = comp.getList("members", Constants.NBT.TAG_STRING);
                        for(int j = 0; j < mList.size(); j++) {
                            members.put(UUID.fromString(mList.getString(j)), pos);
                        }
                    }
                }
            }
        }

        @Override
        public CompoundNBT write(CompoundNBT compound) {
            snailBoxes.getKeysToValues().keySet().forEach(key -> {
                ListNBT list = new ListNBT();
                snailBoxes.getValues(key).forEach(pos -> {
                    CompoundNBT comp = new CompoundNBT();
                    pos.write(comp);
                    comp.putString("name", names.get(pos));
                    comp.putBoolean("public", publicM.get(pos));
                    ListNBT list2 = new ListNBT();
                    for(UUID member : this.getMembers(pos)) {
                        list2.add(StringNBT.valueOf(member.toString()));
                    }
                    comp.put("members", list2);
                    list.add(comp);
                });
                compound.put(key.toString(), list);
            });
            return compound;
        }

        public static SnailBoxData getData(MinecraftServer server) {
            ServerWorld world = server.getWorld(World.OVERWORLD);
            DimensionSavedDataManager data = world.getSavedData();
            SnailBoxData a = data.getOrCreate(SnailBoxData::new, "SNAIL_BOXES");
            return a;
        }

    }

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

    public static void forceArea(ServerWorld world, BlockPos pos, boolean type) {
        world.forceChunk(pos.getX() >> 4, pos.getZ() >> 4, type);
        world.forceChunk(pos.getX() >> 4 + 1, pos.getZ() >> 4, type);
        world.forceChunk(pos.getX() >> 4 - 1, pos.getZ() >> 4, type);
        world.forceChunk(pos.getX() >> 4, pos.getZ() >> 4 + 1, type);
        world.forceChunk(pos.getX() >> 4, pos.getZ() >> 4 - 1, type);
    }

    public static boolean deliverTo(SnailBoxBlockEntity te, ItemStack envelope, boolean failed) {
        LazyOptional<IItemHandler> hOpt = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (hOpt.isPresent()) {
            if(hOpt.orElse(null) instanceof ItemStackHandler) {
                ItemStackHandler handler = (ItemStackHandler) hOpt.orElse(null);
                Optional<ItemStack> iOpt = EnvelopeItem.convert(envelope);
                if(iOpt.isPresent()) {
                    ItemStack newEnvelope = iOpt.get();
                    if(failed) {
                        if(!newEnvelope.hasTag()) {
                            newEnvelope.setTag(new CompoundNBT());
                        }
                        newEnvelope.getTag().putBoolean("delivery_failed", true);
                    }
                    ItemStack result = newEnvelope;
                    for(int i = 0; i < 27 && !result.isEmpty(); i++) {
                        result = handler.insertItem(i, newEnvelope, true);
                    }
                    if(result.isEmpty()) {
                        result = newEnvelope;
                        for(int i = 0; i < 27 && !result.isEmpty(); i++) {
                            result = handler.insertItem(i, newEnvelope, false);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
