package dev.itsmeow.snailmail.menu;

import dev.itsmeow.snailmail.block.SnailBoxBlock;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import dev.itsmeow.snailmail.block.entity.fabric.SnailBoxInterfaceFabric;
import dev.itsmeow.snailmail.menu.slots.StampSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class EnvelopeMenuFabric extends EnvelopeMenu {

    private final SimpleContainer items;

    public EnvelopeMenuFabric(int id, Container playerInventory, SimpleContainer items) {
        this(id, playerInventory, items, BlockPos.ZERO, "", "");
    }

    public EnvelopeMenuFabric(int id, Container playerInventory, SimpleContainer items, BlockPos returnPos, String toName, String fromName) {
        super(id, playerInventory, items.getContainerSize(), returnPos, toName, fromName);
        this.items = items;
        addOwnSlots();
        addPlayerSlots(playerInventory);
    }

    protected void addOwnSlots() {
        for (int i = 0; i < 27; i++) {
            int yCoord = i / 9 * 18; // 9 slots fit per row, 18 is size of the slot texture
            int xCoord = i % 9 * 18; // 0, 1*18, 2*18, 3*18, loop per row
            this.addSlot(new Slot(items, i, 8 + xCoord, 26 + yCoord));
        }
        this.addSlot(new StampSlot(items, 27, 8 + 8 * 18, 6));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level.isClientSide()){
            player.getServer().execute(() -> {
                BlockEntity blockEntity = player.level.getBlockEntity(returnPos);
                if (!(blockEntity instanceof SnailBoxBlockEntity)) {
                    blockEntity = player.level.getBlockEntity(SnailBoxBlock.lastClickedBox.get(player.getUUID()));
                }
                if (blockEntity instanceof SnailBoxBlockEntity) {
                    SnailBoxBlockEntity b = (SnailBoxBlockEntity) blockEntity;
                    ItemStack envelope = SnailBoxBlockEntity.getEnvelope(b);
                    if (!envelope.hasTag()) {
                        envelope.setTag(new CompoundTag());
                    }
                    envelope.getTag().put("item_storage", ContainerHelper.saveAllItems(new CompoundTag(), items.items));
                    SnailBoxBlockEntity.setEnvelope(b, envelope);
                    ((SnailBoxInterfaceFabric)b).getItemContainer().setChanged();
                    b.setChanged();
                }
            });
        }
    }
}
