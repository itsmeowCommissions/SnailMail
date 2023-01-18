package net.examplemod.item.fabric;

import net.examplemod.init.ModItems;
import net.examplemod.item.EnvelopeItem;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class EnvelopeItemUtil {
    public static Pair<SimpleContainer, InventoryStorage> getStorage(ItemStack stack) {
        SimpleContainer container = null;
        InventoryStorage storage = null;
        if(stack.getItem() instanceof EnvelopeItem) {
            EnvelopeItem envelopeItem = (EnvelopeItem) stack.getItem();
            container = new SimpleContainer(envelopeItem.isOpen() ? 28 : 27) {
                @Override
                public void setChanged() {
                    super.setChanged();
                    CompoundTag data = stack.getOrCreateTag();
                    SimpleContainer container = new SimpleContainer(envelopeItem.isOpen() ? 28 : 27);
                    data.put("item_storage", ContainerHelper.saveAllItems(new CompoundTag(), container.items));
                    stack.setTag(data);
                }

                @Override
                public boolean canPlaceItem(int i, ItemStack itemStack) {
                    if(i == 27) {
                        return itemStack.getCount() == 1 && this.getItem(i).isEmpty() && itemStack.getItem() == ModItems.STAMP.get();
                    }
                    return true;
                }
            };
            if(stack.hasTag() && stack.getTag().contains("item_storage", NbtType.COMPOUND)) {
                CompoundTag data = stack.getTag().getCompound("item_storage");
                ContainerHelper.loadAllItems(data, container.items);
            } else {
                CompoundTag data = new CompoundTag();
                data.put("item_storage", ContainerHelper.saveAllItems(new CompoundTag(), container.items));
                stack.setTag(data);
            }
            storage = InventoryStorage.of(container, null);
        }
        return Pair.of(container, storage);
    }
}
