package github.nighter.smartspawner.api;

import github.nighter.smartspawner.api.data.GeneratedLoot;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.data.SpawnerDataModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Main API interface for SmartSpawner plugin.
 * This API allows other plugins to interact with SmartSpawner functionality.
 */
public interface SmartSpawnerAPI {

    /**
     * Creates a SmartSpawner item with the specified entity type.
     *
     * @param entityType the type of entity this spawner will spawn
     * @return an ItemStack representing the spawner
     */
    ItemStack createSpawnerItem(EntityType entityType);

    /**
     * Creates a SmartSpawner item with the specified entity type and a custom amount.
     *
     * @param entityType the type of entity this spawner will spawn
     * @param amount the amount of the item stack
     * @return an ItemStack representing the spawner
     */
    ItemStack createSpawnerItem(EntityType entityType, int amount);

    /**
     * Creates a vanilla spawner item without SmartSpawner features.
     *
     * @param entityType the type of entity this spawner will spawn
     * @return an ItemStack representing the vanilla spawner
     */
    ItemStack createVanillaSpawnerItem(EntityType entityType);

    /**
     * Creates a vanilla spawner item without SmartSpawner features.
     *
     * @param entityType the type of entity this spawner will spawn
     * @param amount the amount of the item stack
     * @return an ItemStack representing the vanilla spawner
     */
    ItemStack createVanillaSpawnerItem(EntityType entityType, int amount);

    /**
     * Creates an item spawner that spawns items instead of entities.
     *
     * @param itemMaterial the material type for the item spawner
     * @return an ItemStack representing the item spawner
     */
    ItemStack createItemSpawnerItem(Material itemMaterial);

    /**
     * Creates an item spawner that spawns items instead of entities.
     *
     * @param itemMaterial the material type for the item spawner
     * @param amount the amount of the item stack
     * @return an ItemStack representing the item spawner
     */
    ItemStack createItemSpawnerItem(Material itemMaterial, int amount);

    /**
     * Checks if an ItemStack is a SmartSpawner.
     *
     * @param item the ItemStack to check
     * @return true if the item is a SmartSpawner, false otherwise
     */
    boolean isSmartSpawner(ItemStack item);

    /**
     * Checks if an ItemStack is a vanilla spawner.
     *
     * @param item the ItemStack to check
     * @return true if the item is a vanilla spawner, false otherwise
     */
    boolean isVanillaSpawner(ItemStack item);

    /**
     * Checks if an ItemStack is an item spawner.
     *
     * @param item the ItemStack to check
     * @return true if the item is an item spawner, false otherwise
     */
    boolean isItemSpawner(ItemStack item);

    /**
     * Gets the entity type from a spawner item.
     *
     * @param item the spawner ItemStack
     * @return the EntityType of the spawner, or null if not a valid spawner
     */
    EntityType getSpawnerEntityType(ItemStack item);

    /**
     * Gets the item material from an item spawner.
     *
     * @param item the item spawner ItemStack
     * @return the Material that the item spawner spawns, or null if not a valid item spawner
     */
    Material getItemSpawnerMaterial(ItemStack item);

    /**
     * Gets spawner data by location.
     * The returned DTO is read-only. To modify spawner properties, use {@link #getSpawnerModifier(String)}.
     *
     * @param location the location of the spawner block
     * @return the spawner data DTO, or null if no spawner exists at that location
     */
    SpawnerDataDTO getSpawnerByLocation(Location location);

    /**
     * Gets spawner data by unique identifier.
     * The returned DTO is read-only. To modify spawner properties, use {@link #getSpawnerModifier(String)}.
     *
     * @param spawnerId the unique ID of the spawner
     * @return the spawner data DTO, or null if spawner with that ID doesn't exist
     */
    SpawnerDataDTO getSpawnerById(String spawnerId);

    /**
     * Gets all registered spawners in the server.
     * The returned DTOs are read-only. To modify spawner properties, use {@link #getSpawnerModifier(String)}.
     *
     * @return list of all spawner data DTOs
     */
    List<SpawnerDataDTO> getAllSpawners();

    /**
     * Creates a modifier for the specified spawner to change its properties.
     * Use this to modify spawner values and then call {@link SpawnerDataModifier#applyChanges()}
     * to recalculate and apply the changes.
     *
     * @param spawnerId the unique ID of the spawner
     * @return a spawner data modifier, or null if spawner doesn't exist
     */
    SpawnerDataModifier getSpawnerModifier(String spawnerId);

    // -------------------------------------------------------------------------
    // Daycare / external plugin support
    // -------------------------------------------------------------------------

    /**
     * Generates loot for the given entity type with the given mob count.
     * Intended for external plugins (e.g., SpawnerDaycare) that need to simulate
     * offline loot generation without a real placed spawner block.
     *
     * @param entityType the entity type whose loot table to use
     * @param mobCount   the number of mobs to simulate (affects item counts and exp)
     * @return the generated loot, never null
     */
    GeneratedLoot generateLootForType(EntityType entityType, int mobCount);

    /**
     * Creates an in-memory virtual spawner that is not persisted to disk and
     * does not correspond to any placed block.  Virtual spawners exist only for
     * the duration of a player session and must be removed via
     * {@link #removeVirtualSpawner(String)} when no longer needed.
     *
     * @param entityType the entity type of the virtual spawner
     * @param stackSize  the stack size (affects storage capacity and mob counts)
     * @return the unique ID assigned to the virtual spawner
     */
    String createVirtualSpawner(EntityType entityType, int stackSize);

    /**
     * Removes a virtual spawner created by {@link #createVirtualSpawner}.
     * Does nothing if the ID does not correspond to a virtual spawner.
     *
     * @param spawnerId the virtual spawner ID
     */
    void removeVirtualSpawner(String spawnerId);

    /**
     * Adds items to the virtual inventory of the specified spawner (real or virtual).
     *
     * @param spawnerId the spawner ID
     * @param items     the items to add
     */
    void addItemsToVirtualSpawner(String spawnerId, List<ItemStack> items);

    /**
     * Sets the stored experience of the specified spawner (real or virtual).
     *
     * @param spawnerId the spawner ID
     * @param exp       the new experience value
     */
    void setVirtualSpawnerExp(String spawnerId, int exp);

    /**
     * Returns the current stored experience of the specified spawner.
     *
     * @param spawnerId the spawner ID
     * @return the current stored experience, or 0 if the spawner does not exist
     */
    int getVirtualSpawnerExp(String spawnerId);

    /**
     * Returns a snapshot of all items currently stored in the spawner's virtual
     * inventory.  Keys are representative ItemStack templates (amount = 1) and
     * values are total stacked counts.
     *
     * @param spawnerId the spawner ID
     * @return item → count map, empty if spawner does not exist
     */
    Map<ItemStack, Long> getVirtualSpawnerItems(String spawnerId);

    /**
     * Clears all items from the virtual inventory of the specified spawner.
     *
     * @param spawnerId the spawner ID
     */
    void clearVirtualSpawnerItems(String spawnerId);

    /**
     * Opens the SmartSpawner main menu for the given player on the specified
     * spawner (real or virtual).  Must be called on the main server thread.
     *
     * @param player    the player to open the menu for
     * @param spawnerId the spawner ID
     */
    void openSpawnerMenu(Player player, String spawnerId);
}
