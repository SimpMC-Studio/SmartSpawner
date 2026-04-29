package github.nighter.smartspawner.api.data;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Represents the result of a loot generation cycle.
 * Contains the generated items and experience.
 */
public record GeneratedLoot(List<ItemStack> items, int experience) {
}
