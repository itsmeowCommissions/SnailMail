package net.examplemod.util.fabric;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class SnailMailCommonConfigImpl {
    public static boolean lockBoxes() {
        return Configuration.get().LOCK_BOXES.get();
    }

    public static boolean protectBoxDestroy() {
        return Configuration.get().PROTECT_BOX_DESTROY.get();
    }

    public static boolean opBypassLock() {
        return Configuration.get().OP_BYPASS_LOCK.get();
    }

    public static int bypassLockOpLevel() {
        return Configuration.get().BYPASS_LOCK_OP_LEVEL.get();
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
        public final ForgeConfigSpec.BooleanValue OP_BYPASS_LOCK;
        public final ForgeConfigSpec.IntValue BYPASS_LOCK_OP_LEVEL;

        protected Configuration(ForgeConfigSpec.Builder builder) {
            LOCK_BOXES = builder.comment("Block snailboxes from being opened by non-owners").define("lock_boxes", true);
            PROTECT_BOX_DESTROY = builder.comment("Protect snailboxes from being destroyed by non-owners").define("protect_box_destroy", true);
            OP_BYPASS_LOCK = builder.comment("If the op level defined in bypass_lock_op_level can bypass locked boxes").define("op_bypass_lock", true);
            BYPASS_LOCK_OP_LEVEL = builder.comment("Op level to bypass snailbox locks").defineInRange("bypass_lock_op_level", 3, 1, 4);
        }
    }
}
