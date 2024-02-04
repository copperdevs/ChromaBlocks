package easton.chromaconcrete;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.recipe.ArmorDyeRecipe;

public class ChromaItem extends BlockItem implements DyeableItem {

    ChromaItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

}