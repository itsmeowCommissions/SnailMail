package dev.itsmeow.snailmail.init;

import java.util.function.Supplier;

import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.block.entity.SnailBoxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class ModBlockEntities {

    public static final TileEntityType<SnailBoxBlockEntity> SNAIL_BOX = create("snail_box", SnailBoxBlockEntity::new, ModBlocks.SNAIL_BOX);

    private static <T extends TileEntity> TileEntityType<T> create(String name, Supplier<? extends T> factory, Block... allowed) {
        TileEntityType<T> type = TileEntityType.Builder.<T>create(factory, allowed).build(null);
        type.setRegistryName(SnailMail.MODID, name);
        return type;
    }
}
