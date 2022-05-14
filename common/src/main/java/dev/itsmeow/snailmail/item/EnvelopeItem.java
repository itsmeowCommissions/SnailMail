package dev.itsmeow.snailmail.item;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.menu.MenuRegistry;
import dev.itsmeow.snailmail.SnailMail;
import dev.itsmeow.snailmail.init.ModItems;
import dev.itsmeow.snailmail.menu.EnvelopeMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class EnvelopeItem extends Item {

    private final boolean isOpen;
    private static final Component title = new TranslatableComponent("container.snailmail.envelope");
    public static final int SLOT_COUNT = 28;

    public EnvelopeItem(boolean isOpen) {
        super(new Item.Properties().tab(SnailMail.ITEM_GROUP).stacksTo(1));
        this.isOpen = isOpen;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(stack.hasTag() && stack.getTag().contains("AddressedTo", Tag.TAG_STRING) && !stack.getTag().getString("AddressedTo").isEmpty()) {
            tooltip.add(new TranslatableComponent("tooltip.snailmail.to", new TextComponent(stack.getTag().getString("AddressedTo")).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY))).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GREEN).withItalic(true)));
        }
        if(stack.hasTag() && stack.getTag().contains("AddressedFrom", Tag.TAG_STRING) && !stack.getTag().getString("AddressedFrom").isEmpty()) {
            tooltip.add(new TranslatableComponent("tooltip.snailmail.from", new TextComponent(stack.getTag().getString("AddressedFrom")).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY))).setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED).withItalic(true)));
        }
        if(stack.hasTag() && stack.getTag().contains("delivery_failed", Tag.TAG_BYTE) && stack.getTag().getBoolean("delivery_failed")) {
            tooltip.add(new TranslatableComponent("tooltip.snailmail.delivery_failed").setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED).withBold(true)));
        }
        if(stack.getItem() == ModItems.ENVELOPE_OPEN.get()) {
            if(isStamped(stack)) {
                tooltip.add(new TranslatableComponent("tooltip.snailmail.stamped").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD).withItalic(true)));
            }
        }
    }

    @ExpectPlatform
    public static boolean isStamped(ItemStack stack) {
        return false;
    }

    public static String getString(ItemStack stack, String key) {
        if(stack.hasTag()) {
            return stack.getTag().getString(key);
        }
        return "";
    }

    public static void putStringChecked(ItemStack stack, String key, String value) {
        if(!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        stack.getTag().putString(key, value);
    }

    /**
     * Converts between open and closed envelope. On open to closed conversion,
     * destroys stamp slot.
     * 
     * @param stack - Stack to convert
     * @return Optional of converted stack, or empty optional if capabilities could not be found
     */
    public static Optional<ItemStack> convert(ItemStack stack) {
        boolean fromOpen = stack.getItem() == ModItems.ENVELOPE_OPEN.get();
        if(fromOpen || stack.getItem() == ModItems.ENVELOPE_CLOSED.get()) {
            return doConvert(stack, fromOpen);
        }
        return Optional.empty();
    }

    @ExpectPlatform
    public static Optional<ItemStack> doConvert(ItemStack stack, boolean fromOpen) {
        return null;
    }

    @ExpectPlatform
    public static void emptyEnvelope(ItemStack stack, Player playerIn) {}

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if(!stack.isEmpty() && hasItems(stack) && playerIn instanceof ServerPlayer && stack.getItem() == ModItems.ENVELOPE_CLOSED.get()) {
            Optional<ItemStack> open = convert(stack);
            if(open.isPresent()) {
                emptyEnvelope(stack, playerIn);
                return InteractionResultHolder.success(new ItemStack(ModItems.ENVELOPE_OPEN.get()));
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @ExpectPlatform
    public static boolean hasItems(ItemStack stack) {
        return false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @ExpectPlatform
    public static EnvelopeMenu getClientMenu(int id, Inventory playerInventory, FriendlyByteBuf extra) {
        return null;
    }

    @ExpectPlatform
    public static MenuConstructor getServerMenuProvider(ItemStack stack) {
        return null;
    }

    public static void openGUI(ServerPlayer player, ItemStack stack, BlockPos pos) {
        MenuConstructor provider = getServerMenuProvider(stack);
        MenuProvider namedProvider = new SimpleMenuProvider(provider, title);
        MenuRegistry.openExtendedMenu(player, namedProvider, buf -> {
            buf.writeBlockPos(pos);
            if(stack.hasTag()) {
                if(stack.getTag().contains("AddressedTo", Tag.TAG_STRING)) {
                    buf.writeUtf(stack.getTag().getString("AddressedTo"), 35);
                }
                if(stack.getTag().contains("AddressedFrom", Tag.TAG_STRING)) {
                    buf.writeUtf(stack.getTag().getString("AddressedFrom"), 35);
                }
            }
        });
    }

}
