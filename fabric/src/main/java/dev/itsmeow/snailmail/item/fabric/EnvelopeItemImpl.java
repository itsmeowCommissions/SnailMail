package dev.itsmeow.snailmail.item.fabric;

import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.item.EnvelopeItem;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import dev.itsmeow.snailmail.menu.EnvelopeMenuFabric;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class EnvelopeItemImpl {

    public static boolean isStamped(ItemStack stack) {
        InventoryStorage storage = EnvelopeItemUtil.getStorage(stack).getRight();
        if(storage != null) {
            ItemStack stampSlot = storage.getSlot(27).getResource().toStack();
            if(!stampSlot.isEmpty() && stampSlot.getItem() == ModItems.STAMP.get()) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ItemStack> doConvert(ItemStack stack, boolean fromOpen) {
        ItemStack newStack = new ItemStack(fromOpen ? ModItems.ENVELOPE_CLOSED.get() : ModItems.ENVELOPE_OPEN.get());
        newStack.getOrCreateTag().put("item_storage", stack.hasTag() && stack.getTag().contains("item_storage", Tag.TAG_COMPOUND) ? stack.getTag().getCompound("item_storage") : new CompoundTag());
        copyTagString(stack, newStack, "AddressedTo");
        copyTagString(stack, newStack, "AddressedFrom");
        return Optional.of(newStack);
    }

    protected static void copyTagString(ItemStack original, ItemStack newStack, String key) {
        if(original.hasTag() && original.getTag().contains(key, Tag.TAG_STRING)) {
            EnvelopeItem.putStringChecked(newStack, key, original.getTag().getString(key));
        }
    }

    public static boolean hasItems(ItemStack stack) {
        return true;
    }

    public static EnvelopeMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        SimpleContainer container = new SimpleContainer(EnvelopeItem.SLOT_COUNT) {
            @Override
            public boolean canPlaceItem(int i, ItemStack itemStack) {
                if(i == 27) {
                    return itemStack.getCount() == 1 && this.getItem(i).isEmpty() && itemStack.getItem() == ModItems.STAMP.get();
                }
                return true;
            }
        };
        if(extra.readableBytes() > 0) {
            try {
                BlockPos returnPos = extra.readBlockPos();
                String toName = "";
                if(extra.readableBytes() > 0) {
                    toName = extra.readUtf(35);
                }
                String fromName = "";
                if(extra.readableBytes() > 0) {
                    fromName = extra.readUtf(35);
                }
                return new EnvelopeMenuFabric(id, playerInventory, container, returnPos, toName, fromName);
            } catch(IndexOutOfBoundsException e) {
            }
        }
        return new EnvelopeMenuFabric(id, playerInventory, container);
    }

    public static MenuConstructor getServerMenuProvider(ItemStack stack) {
        return (id, playerInventory, serverPlayer) -> new EnvelopeMenuFabric(id, playerInventory, EnvelopeItemUtil.getStorage(stack).getLeft());
    }

    public static void emptyEnvelope(ItemStack envelope, Player player) {
        try(Transaction transaction = Transaction.openOuter()) {
            PlayerInventoryStorage inventory = PlayerInventoryStorage.of(player);
            for (SingleSlotStorage<ItemVariant> slot : EnvelopeItemUtil.getStorage(envelope).getRight().getSlots()) {
                if (slot.isResourceBlank()) continue;
                inventory.offerOrDrop(slot.getResource(), slot.getAmount(), transaction);
            }
            transaction.commit();
        }
    }

}
