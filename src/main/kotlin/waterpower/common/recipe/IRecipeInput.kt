/**
 * Copyright (c) Huang Yuhui, 2017
 *
 * "WaterPower" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package waterpower.common.recipe

import net.minecraft.item.ItemStack

interface IRecipeInput {
    fun matches(input: ItemStack): Boolean
    fun getAmount(): Int
    fun getInputs(): List<ItemStack>
}