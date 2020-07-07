package dev.itsmeow.snailmail;

import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.init.ModBlocks;
import dev.itsmeow.snailmail.init.ModContainers;
import dev.itsmeow.snailmail.init.ModEntities;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.NamedBlockItem;
import dev.itsmeow.snailmail.network.SetEnvelopeNamePacket;
import dev.itsmeow.snailmail.util.BiMultiMap;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        ModContainers.ENVELOPE_OPEN);
    }

    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        ModEntities.H.ENTITIES.values().forEach(c -> event.getRegistry().register(c.entityType));
    }

    public static class SnailBoxData extends WorldSavedData {

        private final BiMultiMap<UUID, BlockPos> snailBoxes = new BiMultiMap<>();

        public SnailBoxData() {
            super("SNAIL_BOXES");
        }

        public void addBox(UUID owner, BlockPos pos) {
            snailBoxes.put(owner, pos);
            this.markDirty();
        }

        public void removeBox(UUID owner, BlockPos pos) {
            snailBoxes.remove(owner, pos);
        }

        public void removeBoxRaw(BlockPos pos) {
            snailBoxes.removeValueFromAll(pos);
        }

        public Set<BlockPos> getBoxes(UUID owner) {
            return snailBoxes.getValues(owner);
        }

        public Set<BlockPos> getAllBoxes() {
            return snailBoxes.getValuesToKeys().keySet();
        }

        @Override
        public void read(CompoundNBT nbt) {
            for(String key : nbt.keySet()) {
                UUID uuid = UUID.fromString(key);
                if(uuid != null && nbt.contains(key, Constants.NBT.TAG_LIST)) {
                    ListNBT list = nbt.getList(key, Constants.NBT.TAG_INT_ARRAY);
                    for(int i = 0; i < list.size(); i++) {
                        int[] arr = list.getIntArray(i);
                        if(arr.length == 3) {
                            int x = arr[0];
                            int y = arr[1];
                            int z = arr[2];
                            snailBoxes.put(uuid, new BlockPos(x, y, z));
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
                    list.add(new IntArrayNBT(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
                });
                compound.put(key.toString(), list);
            });
            return compound;
        }

        public static SnailBoxData getData(MinecraftServer server) {
            server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(SnailBoxData::new, "SNAIL_BOXES");
            return null;
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

        public final ForgeConfigSpec.BooleanValue SHOW_BOX_COORDINATES;
        public final ForgeConfigSpec.BooleanValue LOCK_BOXES;
        public final ForgeConfigSpec.BooleanValue PROTECT_BOX_DESTROY;

        protected Configuration(ForgeConfigSpec.Builder builder) {
            SHOW_BOX_COORDINATES = builder.comment("Show coordinates of snailboxes when choosing").define("show_box_coordinates", true);
            LOCK_BOXES = builder.comment("Block snailboxes from being opened by non-owners").define("lock_boxes", true);
            PROTECT_BOX_DESTROY = builder.comment("Protect snailboxes from being destroyed by non-owners").define("protect_box_destroy", true);
        }
    }
}
