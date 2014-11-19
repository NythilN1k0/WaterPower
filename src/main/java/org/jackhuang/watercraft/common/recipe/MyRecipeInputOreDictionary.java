package org.jackhuang.watercraft.common.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class MyRecipeInputOreDictionary implements IMyRecipeInput {
	public final String input;
	public final int amount;
	public final Integer meta;

	public MyRecipeInputOreDictionary(String input1) {
		this(input1, 1);
	}

	public MyRecipeInputOreDictionary(String input1, int amount1) {
		this(input1, amount1, null);
	}

	public MyRecipeInputOreDictionary(String input1, int amount1, Integer meta) {
		this.input = input1;
		this.amount = amount1;
		this.meta = meta;
	}

	@Override
	public boolean matches(ItemStack subject) {
		List<ItemStack> inputs = OreDictionary.getOres(this.input);

		for (ItemStack input1 : inputs) {
			if (input1.getItem() != null) {
				int metaRequired = this.meta == null ? input1.getItemDamage()
						: this.meta.intValue();

				if ((subject.getItem() == input1.getItem())
						&& ((subject.getItemDamage() == metaRequired) || (metaRequired == 32767))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getInputAmount() {
		return this.amount;
	}

	@Override
	public List<ItemStack> getInputs() {
		List<ItemStack> ores = OreDictionary.getOres(this.input);
		List ret = new ArrayList(ores.size());

		for (ItemStack stack : ores) {
			if (stack.getItem() != null)
				ret.add(stack);
		}

		return ret;
	}
}