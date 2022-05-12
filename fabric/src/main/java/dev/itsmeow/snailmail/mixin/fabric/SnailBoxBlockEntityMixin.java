package dev.itsmeow.snailmail.mixin.fabric;

import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.block.entity.fabric.SnailBoxInterfaceFabric;
import dev.itsmeow.snailmail.init.ModItems;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SnailBoxBlockEntity.class)
public abstract class SnailBoxBlockEntityMixin extends BlockEntity implements SnailBoxInterfaceFabric {

    @Unique
    private final SimpleContainer container = new SimpleContainer(SnailBoxBlockEntity.SLOT_COUNT) {
        @Override
        public boolean canPlaceItem(int i, ItemStack itemStack) {
            if(i == 27) {
                return itemStack.getItem() == ModItems.ENVELOPE_OPEN.get();
            } else {
                return itemStack.getItem() == ModItems.ENVELOPE_CLOSED.get();
            }
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SnailBoxBlockEntityMixin.this.setChanged();
        }
    };
    @Unique
    private final InventoryStorage containerWrapper = InventoryStorage.of(container, null);

    public SnailBoxBlockEntityMixin(BlockEntityType<?> arg) {
        super(arg);
    }

    @Override
    public InventoryStorage getItemHandler() {
        return containerWrapper;
    }

    @Override
    public SimpleContainer getItemContainer() {
        return container;
    }

    @Override
    public void deserializeHandlerNBT(CompoundTag nbt) {
        container.clearContent();
        ContainerHelper.loadAllItems(nbt, container.items);
    }

    @Override
    public CompoundTag serializeHandlerNBT() {
        return ContainerHelper.saveAllItems(new CompoundTag(), container.items);
    }

}
