package com.mitchej123.hodgepodge.mixins.late.extrautilities;

import static com.mitchej123.hodgepodge.util.StringLiterals.EXU_SPIKE_OVERCHANT_KEY;

import java.util.ArrayList;
import java.util.List;

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
public abstract class MixinBlockSpike_Overchant {

    @WrapOperation(
            method = "onBlockPlacedBy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getEnchantmentTagList()Lnet/minecraft/nbt/NBTTagList;"))

    private NBTTagList hodgepodge$injectOverchant(ItemStack stack, Operation<NBTTagList> original) {
        NBTTagList enchants = original.call(stack);
        if (enchants == null || stack.stackTagCompound == null) return null;

        int[] overchants = stack.stackTagCompound.getIntArray("overchants");
        if (overchants == null || overchants.length == 0) return enchants;

        enchants = (NBTTagList) enchants.copy();
        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);

            for (int overchantID : overchants) {
                if (enchant.hasKey("id") && enchant.getShort("id") == (short) overchantID) {
                    enchant.setBoolean(EXU_SPIKE_OVERCHANT_KEY, true);
                    break;
                }
            }
        }

        return enchants;
    }

    @WrapOperation(
            method = "getDrops",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;setTagCompound(Lnet/minecraft/nbt/NBTTagCompound;)V"))

    private void hodgepodge$restoreOverchant(ItemStack stack, NBTTagCompound nbt, Operation<Void> original) {
        if (nbt == null || !nbt.hasKey("ench")) {
            original.call(stack, nbt);
            return;
        }

        NBTTagList enchants = nbt.getTagList("ench", 10);
        List<Integer> overchants = new ArrayList<>(enchants.tagCount());
        for (int i = 0; i < enchants.tagCount(); i++) {
            NBTTagCompound enchant = enchants.getCompoundTagAt(i);

            if (enchant.hasKey(EXU_SPIKE_OVERCHANT_KEY)) {
                enchant.removeTag(EXU_SPIKE_OVERCHANT_KEY);
                if (!enchant.hasKey("id")) continue;
                overchants.add((int) enchant.getShort("id"));
            }
        }

        if (!overchants.isEmpty()) {
            int[] overchantArray = new int[overchants.size()];
            for (int i = 0; i < overchants.size(); i++) {
                overchantArray[i] = overchants.get(i);
            }
            nbt.setIntArray("overchants", overchantArray);
        }

        original.call(stack, nbt);
    }
}
