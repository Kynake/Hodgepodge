package com.mitchej123.hodgepodge.mixins.late.extrautilities;

import static com.mitchej123.hodgepodge.util.StringLiterals.EXU_SPIKE_OVERCHANT_KEY;
import static com.mitchej123.hodgepodge.util.StringLiterals.EXU_SPIKE_REPAIR_COST_KEY;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mitchej123.hodgepodge.config.FixesConfig;
import com.rwtema.extrautils.block.BlockSpike;

@SuppressWarnings("UnusedMixin")
@Mixin(BlockSpike.class)
public abstract class MixinBlockSpike_CleanupNBT {

    @WrapOperation(
            method = "getDrops",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"))

    private void hodgepodge$cleanupNBT(ItemStack stack, NBTTagCompound nbt, Operation<Void> original) {
        if (nbt == null || !nbt.hasKey("ench")) {
            original.call(stack, nbt);
            return;
        }

        NBTTagList enchants = nbt.getTagList("ench", 10);
        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);
            if (!FixesConfig.fixExtraUtilitiesRepairCost) {
                enchant.removeTag(EXU_SPIKE_REPAIR_COST_KEY);
            }
            if (!FixesConfig.fixExtraUtilitiesOverchant) {
                enchant.removeTag(EXU_SPIKE_OVERCHANT_KEY);
            }
        }

        original.call(stack, nbt);
    }
}
