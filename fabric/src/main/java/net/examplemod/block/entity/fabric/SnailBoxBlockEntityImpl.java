package net.examplemod.block.entity.fabric;

import net.examplemod.block.entity.SnailBoxBlockEntity;
import net.examplemod.init.ModItems;
import net.examplemod.menu.SnailBoxMenu;
import net.examplemod.menu.SnailBoxMenuFabric;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class SnailBoxBlockEntityImpl {
    private static final String ITEM_TAG_KEY = "item_handler";

    public static void loadStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {
        ((SnailBoxInterfaceFabric) blockEntity).deserializeHandlerNBT(compoundTag.getCompound(ITEM_TAG_KEY));
    }

    public static void saveStorage(SnailBoxBlockEntity blockEntity, CompoundTag compoundTag) {
        compoundTag.put(ITEM_TAG_KEY, ((SnailBoxInterfaceFabric) blockEntity).serializeHandlerNBT());
    }

    public static void handleRemoved(SnailBoxBlockEntity blockEntity) {
    }

    public static SnailBoxMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        SimpleContainer container = new SimpleContainer(SnailBoxBlockEntity.SLOT_COUNT) {
            @Override
            public boolean canPlaceItem(int i, ItemStack itemStack) {
                if(i == 28) {
                    return itemStack.getItem() == ModItems.STAMP.get();
                }
                return super.canPlaceItem(i, itemStack);
            }
        };
        BlockPos pos = extra.readBlockPos();
        if(extra.readableBytes() > 0) {
            String name = extra.readUtf(35);
            boolean isOwner = extra.readBoolean();
            boolean isPublic = extra.readBoolean();
            int len = extra.readInt();
            Set<String> usernames = new HashSet<String>();
            for(int i = 0; i < len; i++) {
                usernames.add(extra.readUtf(35));
            }
            return new SnailBoxMenuFabric(id, playerInventory, container, pos, name, isOwner, isPublic, usernames);
        }
        return new SnailBoxMenuFabric(id, playerInventory, container, pos, "", false, false, new HashSet<>());
    }

    public static MenuConstructor getServerMenuProvider(SnailBoxBlockEntity te) {
        return (id, playerInventory, serverPlayer) -> new SnailBoxMenuFabric(id, playerInventory, te.getBlockPos(), ((SnailBoxInterfaceFabric) te).getItemContainer());
    }

    public static void dropItems(SnailBoxBlockEntity blockEntity) {
        try (Transaction transaction = Transaction.openOuter()) {
            for (int i = 0; i < ((SnailBoxInterfaceFabric) blockEntity).getItemHandler().getSlots().size(); ++i) {
                ((SnailBoxInterfaceFabric) blockEntity).getItemHandler().getSlot(i).iterator().forEachRemaining(view -> {
                    Containers.dropItemStack(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), view.getResource().toStack((int) view.getAmount()));
                });
            }
            transaction.commit();
        }

    }

    public static ItemStack getEnvelope(SnailBoxBlockEntity blockEntity) {
        return ((SnailBoxInterfaceFabric) blockEntity).getItemHandler().getSlot(27).getResource().toStack();
    }

    public static void setEnvelope(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        InventoryStorage handler = ((SnailBoxInterfaceFabric) blockEntity).getItemHandler();

        try(Transaction transaction = Transaction.openOuter()) {
            if (!handler.getSlot(27).isResourceBlank())
                handler.getSlot(27).extract(handler.getSlot(27).getResource(), handler.getSlot(27).getAmount(), transaction);
            if(!stack.isEmpty())
                handler.getSlot(27).insert(ItemVariant.of(stack), stack.getCount(), transaction);
            transaction.commit();
        }
    }

    public static boolean setEnvelopeServer(SnailBoxBlockEntity blockEntity, ItemStack stack) {
        setEnvelope(blockEntity, stack);
        return true;
    }

    public static boolean hasCapability(SnailBoxBlockEntity te) {
        return true;
    }

    public static boolean tryInsert(SnailBoxBlockEntity te, ItemStack newEnvelope) {
        InventoryStorage handler = ((SnailBoxInterfaceFabric) te).getItemHandler();
        try(Transaction transaction = Transaction.openOuter()) {
            long inserted = 0;
            for (int i = 0; i < 27 && inserted < newEnvelope.getCount(); i++) {
                inserted += handler.getSlot(i).simulateInsert(ItemVariant.of(newEnvelope), newEnvelope.getCount(), transaction);
            }
            if (inserted == newEnvelope.getCount()) {
                inserted = 0;
                for (int i = 0; i < 27 && inserted < newEnvelope.getCount(); i++) {
                    inserted += handler.getSlot(i).insert(ItemVariant.of(newEnvelope), newEnvelope.getCount(), transaction);
                }
                transaction.commit();
                return true;
            }
            transaction.commit();
        }
        return false;
    }
}
