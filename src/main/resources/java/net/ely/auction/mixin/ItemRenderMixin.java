package net.ely.auction.mixin;

import net.ely.auction.AuctionState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemStack.class)
public class ItemRenderMixin {

    @ModifyVariable(method = "getItem", at = @At("HEAD"), argsOnly = true)
    private net.minecraft.item.Item modifyDisplayedItem(net.minecraft.item.Item item) {
        if (!AuctionState.isActive) return item;

        if (item == Items.IRON_CHESTPLATE) {
            ItemStack stack = (ItemStack) (Object) this;
            if (hasEnchantment(stack, Enchantments.UNBREAKING, 3) && hasEnchantment(stack, Enchantments.MENDING, 1)) {
                return Items.ELYTRA; // Force le jeu à l'afficher comme une élytre
            }
        }
        return item;
    }

    private boolean hasEnchantment(ItemStack stack, net.minecraft.registry.RegistryKey<Enchantment> enchantKey, int minLevel) {
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null) return false;
        
        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            if (entry.getKey().isPresent() && entry.getKey().get().equals(enchantKey)) {
                return enchants.getLevel(entry) >= minLevel;
            }
        }
        return false;
    }
}
