package net.ely.auction.mixin;

import net.ely.auction.AuctionState;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraFeatureRenderer.class)
public class ArmorRenderMixin {

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getEquippedStack(Lnet/minecraft/entity/EquipmentSlot;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack redirectEquippedStack(LivingEntity entity, EquipmentSlot slot) {
        ItemStack originalStack = entity.getEquippedStack(slot);

        if (AuctionState.isActive && slot == EquipmentSlot.CHEST) {
            if (originalStack.isOf(Items.IRON_CHESTPLATE)) {
                if (hasEnchantment(originalStack, Enchantments.UNBREAKING, 3) && hasEnchantment(originalStack, Enchantments.MENDING, 1)) {
                    return new ItemStack(Items.ELYTRA);
                }
            }
        }
        return originalStack;
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
