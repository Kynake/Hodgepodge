package com.mitchej123.hodgepodge.mixins.late.extrautilities;

import static com.mitchej123.hodgepodge.util.StringLiterals.EXU_SPIKE_REPAIR_COST_KEY;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rwtema.extrautils.block.BlockSpike;

@SuppressWarnings("UnusedMixin")
@Mixin(BlockSpike.class)
public abstract class MixinBlockSpike_RepairCost {

    @WrapOperation(
            method = "onBlockPlacedBy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getEnchantmentTagList()Lnet/minecraft/nbt/NBTTagList;"))

    private NBTTagList hodgepodge$injectRepairCost(ItemStack stack, Operation<NBTTagList> original) {
        NBTTagList enchants = original.call(stack);
        if (enchants == null || stack.stackTagCompound == null) return null;

        if (enchants.tagCount() == 0 || !stack.stackTagCompound.hasKey("RepairCost")) return enchants;

        int repairCost = stack.stackTagCompound.getInteger("RepairCost");

        enchants = (NBTTagList) enchants.copy();
        NBTTagCompound firstEnchant = enchants.getCompoundTagAt(0);
        firstEnchant.setInteger(EXU_SPIKE_REPAIR_COST_KEY, repairCost);

        return enchants;
    }

    @WrapOperation(
            method = "getDrops",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"))

    private void hodgepodge$restoreRepairCost(ItemStack stack, NBTTagCompound nbt, Operation<Void> original) {
        if (nbt == null || !nbt.hasKey("ench")) {
            original.call(stack, nbt);
            return;
        }

        NBTTagList enchants = nbt.getTagList("ench", 10);
        if (enchants.tagCount() < 1) {
            original.call(stack, nbt);
            return;
        }

        NBTTagCompound firstEnchant = enchants.getCompoundTagAt(0);
        if (firstEnchant.hasKey(EXU_SPIKE_REPAIR_COST_KEY)) {
            int repairCost = firstEnchant.getInteger(EXU_SPIKE_REPAIR_COST_KEY);
            if (repairCost != 0) {
                nbt.setInteger("RepairCost", repairCost);
            }
            firstEnchant.removeTag(EXU_SPIKE_REPAIR_COST_KEY);
        }

        original.call(stack, nbt);
    }
}
