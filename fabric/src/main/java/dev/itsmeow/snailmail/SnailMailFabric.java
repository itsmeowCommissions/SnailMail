package dev.itsmeow.snailmail;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.itsmeow.snailmail.block.entity.fabric.SnailBoxInterfaceFabric;
import dev.itsmeow.snailmail.init.ModBlockEntities;
import dev.itsmeow.snailmail.util.ConfigInterface;
import dev.itsmeow.snailmail.util.fabric.SnailMailCommonConfigImpl;
import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.NumberConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class SnailMailFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        SnailMail.construct();
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> ((SnailBoxInterfaceFabric) blockEntity).getItemHandler(), ModBlockEntities.SNAIL_BOX.get());
        SnailMailCommonConfigImpl.CONFIG_WRAPPER = new SnailMailFabricConfiguration();
    }

    public static final class SnailMailFabricConfiguration implements ConfigInterface {

        public PropertyMirror<Boolean> lock_boxes;
        public PropertyMirror<Boolean> protect_box_destroy;
        public PropertyMirror<Boolean> op_bypass_lock;
        public PropertyMirror<Integer> bypass_lock_op_level;
        public final FabricCommonConfig config;

        public SnailMailFabricConfiguration() {
            config = new FabricCommonConfig(builder -> {
                lock_boxes = PropertyMirror.create(ConfigTypes.BOOLEAN);
                protect_box_destroy = PropertyMirror.create(ConfigTypes.BOOLEAN);
                op_bypass_lock = PropertyMirror.create(ConfigTypes.BOOLEAN);
                NumberConfigType<Integer> rangeType = ConfigTypes.INTEGER.withMinimum(1).withMaximum(4);
                bypass_lock_op_level = PropertyMirror.create(rangeType);
                builder = builder.beginValue("lock_boxes", ConfigTypes.BOOLEAN, true).withComment("Block snailboxes from being opened by non-owners").finishValue(lock_boxes::mirror);
                builder = builder.beginValue("protect_box_destroy", ConfigTypes.BOOLEAN, true).withComment("Protect snailboxes from being destroyed by non-owners").finishValue(protect_box_destroy::mirror);
                builder = builder.beginValue("op_bypass_lock", ConfigTypes.BOOLEAN, true).withComment("If the op level defined in bypass_lock_op_level can bypass locked boxes").finishValue(op_bypass_lock::mirror);
                builder = builder.beginValue("bypass_lock_op_level", rangeType, 3).withComment("Op level to bypass snailbox locks").finishValue(bypass_lock_op_level::mirror);
                return builder;
            });
        }

        @Override
        public boolean lockBoxes() {
            return lock_boxes.getValue();
        }

        @Override
        public boolean protectBoxDestroy() {
            return protect_box_destroy.getValue();
        }

        @Override
        public boolean opBypassLock() {
            return op_bypass_lock.getValue();
        }

        @Override
        public int bypassLockOpLevel() {
            return bypass_lock_op_level.getValue();
        }
    }

    public static class FabricCommonConfig {

        public static final Set<FabricCommonConfig> INSTANCES = new HashSet<>();
        protected static final Logger LOGGER = LogManager.getLogger();
        protected final JanksonValueSerializer janksonSerializer = new JanksonValueSerializer(false);
        protected final Function<ConfigTreeBuilder, ConfigTreeBuilder> init;
        private ConfigBranch builtConfig;
        private boolean initialized = false;
        protected String name;

        private ConfigTreeBuilder builder = ConfigTree.builder();

        public FabricCommonConfig(Function<ConfigTreeBuilder, ConfigTreeBuilder> init) {
            this.name = SnailMail.MODID + "-common";
            this.init = init;
            builder = builder.withName(name);
            LifecycleEvent.SERVER_BEFORE_START.register(state -> this.createOrLoad());
            INSTANCES.add(this);
        }

        public ConfigBranch getBranch() {
            if(builtConfig == null) {
                return this.init();
            }
            return builtConfig;
        }

        protected ConfigBranch init() {
            if(!initialized) {
                this.initialized = true;
                builder = init.apply(builder);
                this.builtConfig = builder.build();
            }
            return this.builtConfig;
        }

        public String getConfigName() {
            return name;
        }

        public File getConfigFile() {
            return new File(FabricLoader.getInstance().getConfigDir().toFile(), getConfigName() + ".json5");
        }

        public void createOrLoad() {
            setupConfigFile(this.getConfigFile(), this.init(), janksonSerializer);
        }

        public void saveBranch(File configFile, ConfigBranch branch) {
            try {
                FiberSerialization.serialize(branch, Files.newOutputStream(configFile.toPath()), janksonSerializer);
                LOGGER.info("Successfully wrote menu edits to config file '{}'", configFile.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setupConfigFile(File configFile, ConfigBranch configNode, JanksonValueSerializer serializer) {
            boolean recreate = false;
            while (true) {
                try {
                    if (!configFile.exists() || recreate) {
                        FiberSerialization.serialize(configNode, Files.newOutputStream(configFile.toPath()), serializer);
                        LOGGER.info("Successfully created the config file in '{}'", configFile.toString());
                        break;
                    } else {
                        try {
                            FiberSerialization.deserialize(configNode, Files.newInputStream(configFile.toPath()), serializer);
                            FiberSerialization.serialize(configNode, Files.newOutputStream(configFile.toPath()), serializer);
                            LOGGER.info("Successfully loaded '{}'", configFile.toString());
                            break;
                        } catch (ValueDeserializationException e) {
                            String fileName = (getConfigName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".json5");
                            LOGGER.error("Found a syntax error in the config!");
                            if (configFile.renameTo(new File(configFile.getParent(), fileName))) {
                                LOGGER.info("Config file successfully renamed to '{}'.", fileName);
                            }
                            recreate = true;
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

}
