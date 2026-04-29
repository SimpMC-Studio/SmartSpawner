package github.nighter.smartspawner.api;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.data.GeneratedLoot;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.data.SpawnerDataModifier;
import github.nighter.smartspawner.api.impl.SpawnerDataModifierImpl;
import github.nighter.smartspawner.spawner.config.SpawnerSettingsConfig;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the SmartSpawnerAPI interface.
 */
public class SmartSpawnerAPIImpl implements SmartSpawnerAPI {

    private final SmartSpawner plugin;
    private final SpawnerItemFactory itemFactory;
    private final Random random = new Random();

    public SmartSpawnerAPIImpl(SmartSpawner plugin) {
        this.plugin = plugin;
        this.itemFactory = new SpawnerItemFactory(plugin);
    }

    @Override
    public ItemStack createSpawnerItem(EntityType entityType) {
        return itemFactory.createSmartSpawnerItem(entityType);
    }

    @Override
    public ItemStack createSpawnerItem(EntityType entityType, int amount) {
        return itemFactory.createSmartSpawnerItem(entityType, amount);
    }

    @Override
    public ItemStack createVanillaSpawnerItem(EntityType entityType) {
        return itemFactory.createVanillaSpawnerItem(entityType);
    }

    @Override
    public ItemStack createVanillaSpawnerItem(EntityType entityType, int amount) {
        return itemFactory.createVanillaSpawnerItem(entityType, amount);
    }

    @Override
    public ItemStack createItemSpawnerItem(Material itemMaterial) {
        return itemFactory.createItemSpawnerItem(itemMaterial);
    }

    @Override
    public ItemStack createItemSpawnerItem(Material itemMaterial, int amount) {
        return itemFactory.createItemSpawnerItem(itemMaterial, amount);
    }

    @Override
    public boolean isSmartSpawner(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        // A SmartSpawner is a spawner that is NOT vanilla and NOT an item spawner
        return !isVanillaSpawner(item) && !isItemSpawner(item);
    }

    @Override
    public boolean isVanillaSpawner(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "vanilla_spawner"),
                org.bukkit.persistence.PersistentDataType.BOOLEAN);
    }

    @Override
    public boolean isItemSpawner(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "item_spawner_material"),
                org.bukkit.persistence.PersistentDataType.STRING);
    }

    @Override
    public EntityType getSpawnerEntityType(ItemStack item) {
        if (item == null || item.getType() != Material.SPAWNER || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockMeta)) {
            return null;
        }

        BlockState blockState = blockMeta.getBlockState();
        if (!(blockState instanceof CreatureSpawner cs)) {
            return null;
        }

        return cs.getSpawnedType();
    }

    @Override
    public Material getItemSpawnerMaterial(ItemStack item) {
        if (!isItemSpawner(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        String materialName = meta.getPersistentDataContainer().get(
                new org.bukkit.NamespacedKey(plugin, "item_spawner_material"),
                org.bukkit.persistence.PersistentDataType.STRING);

        if (materialName == null) {
            return null;
        }

        try {
            return Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public SpawnerDataDTO getSpawnerByLocation(Location location) {
        if (location == null) {
            return null;
        }

        SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerByLocation(location);
        return spawnerData != null ? convertToDTO(spawnerData) : null;
    }

    @Override
    public SpawnerDataDTO getSpawnerById(String spawnerId) {
        if (spawnerId == null) {
            return null;
        }

        SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        return spawnerData != null ? convertToDTO(spawnerData) : null;
    }

    @Override
    public List<SpawnerDataDTO> getAllSpawners() {
        return plugin.getSpawnerManager().getAllSpawners().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SpawnerDataModifier getSpawnerModifier(String spawnerId) {
        if (spawnerId == null) {
            return null;
        }

        SpawnerData spawnerData = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        return spawnerData != null ? new SpawnerDataModifierImpl(spawnerData) : null;
    }

    // -------------------------------------------------------------------------
    // Daycare / external plugin support
    // -------------------------------------------------------------------------

    @Override
    public GeneratedLoot generateLootForType(EntityType entityType, int mobCount) {
        if (entityType == null || mobCount <= 0) {
            return new GeneratedLoot(List.of(), 0);
        }

        SpawnerSettingsConfig cfg = plugin.getSpawnerSettingsConfig();
        EntityLootConfig lootConfig = cfg != null ? cfg.getLootConfig(entityType) : null;
        if (lootConfig == null) {
            return new GeneratedLoot(List.of(), 0);
        }

        int totalExp = lootConfig.experience() * mobCount;
        List<LootItem> validItems = lootConfig.getAllItems().stream()
                .filter(LootItem::isAvailable)
                .toList();

        if (validItems.isEmpty()) {
            return new GeneratedLoot(List.of(), totalExp);
        }

        Map<ItemStack, Integer> consolidated = new HashMap<>();
        for (LootItem lootItem : validItems) {
            int successfulDrops = 0;
            for (int i = 0; i < mobCount; i++) {
                if (random.nextDouble() * 100 <= lootItem.chance()) {
                    successfulDrops++;
                }
            }
            if (successfulDrops > 0) {
                ItemStack proto = lootItem.createItemStack(random);
                if (proto != null) {
                    int totalAmount = 0;
                    for (int i = 0; i < successfulDrops; i++) {
                        totalAmount += lootItem.generateAmount(random);
                    }
                    if (totalAmount > 0) {
                        consolidated.merge(proto, totalAmount, Integer::sum);
                    }
                }
            }
        }

        List<ItemStack> finalLoot = new ArrayList<>(consolidated.size());
        for (Map.Entry<ItemStack, Integer> entry : consolidated.entrySet()) {
            int remaining = entry.getValue();
            ItemStack template = entry.getKey();
            int maxStack = template.getMaxStackSize();
            while (remaining > 0) {
                ItemStack stack = template.clone();
                stack.setAmount(Math.min(remaining, maxStack));
                finalLoot.add(stack);
                remaining -= stack.getAmount();
            }
        }

        return new GeneratedLoot(finalLoot, totalExp);
    }

    @Override
    public String createVirtualSpawner(EntityType entityType, int stackSize) {
        String id = "virtual_daycare_" + UUID.randomUUID();
        // Use null-world location so SpawnerHologram.createHologram() skips creation
        Location dummyLoc = new Location(null, 0, 0, 0);
        SpawnerData spawner = new SpawnerData(id, dummyLoc, entityType, plugin);
        // Apply the actual stack size; setStackSize validates against maxStackSize from config
        if (stackSize > 1) {
            spawner.setStackSize(stackSize);
        }
        plugin.getSpawnerManager().addVirtualSpawner(id, spawner);
        return id;
    }

    @Override
    public void removeVirtualSpawner(String spawnerId) {
        if (spawnerId == null) return;
        plugin.getSpawnerManager().removeVirtualSpawner(spawnerId);
    }

    @Override
    public void addItemsToVirtualSpawner(String spawnerId, List<ItemStack> items) {
        if (spawnerId == null || items == null || items.isEmpty()) return;
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        if (data == null) return;
        data.getVirtualInventory().addItems(items);
    }

    @Override
    public void setVirtualSpawnerExp(String spawnerId, int exp) {
        if (spawnerId == null) return;
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        if (data == null) return;
        data.setSpawnerExpData(exp);
    }

    @Override
    public int getVirtualSpawnerExp(String spawnerId) {
        if (spawnerId == null) return 0;
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        return data != null ? data.getSpawnerExp() : 0;
    }

    @Override
    public Map<ItemStack, Long> getVirtualSpawnerItems(String spawnerId) {
        if (spawnerId == null) return Map.of();
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        if (data == null) return Map.of();

        Map<VirtualInventory.ItemSignature, Long> consolidated = data.getVirtualInventory().getConsolidatedItems();
        Map<ItemStack, Long> result = new HashMap<>(consolidated.size());
        for (Map.Entry<VirtualInventory.ItemSignature, Long> entry : consolidated.entrySet()) {
            result.put(entry.getKey().getTemplate(), entry.getValue());
        }
        return result;
    }

    @Override
    public void clearVirtualSpawnerItems(String spawnerId) {
        if (spawnerId == null) return;
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        if (data == null) return;
        data.getVirtualInventory().clear();
    }

    @Override
    public void openSpawnerMenu(Player player, String spawnerId) {
        if (player == null || spawnerId == null) return;
        SpawnerData data = plugin.getSpawnerManager().getSpawnerById(spawnerId);
        if (data == null) return;
        plugin.getSpawnerMenuUI().openSpawnerMenu(player, data, false);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private SpawnerDataDTO convertToDTO(SpawnerData spawnerData) {
        return new SpawnerDataDTO(
                spawnerData.getSpawnerId(),
                spawnerData.getSpawnerLocation(),
                spawnerData.getEntityType(),
                spawnerData.getSpawnedItemMaterial(),
                spawnerData.getStackSize(),
                spawnerData.getMaxStackSize(),
                spawnerData.getBaseMaxStoragePages(),
                spawnerData.getBaseMinMobs(),
                spawnerData.getBaseMaxMobs(),
                spawnerData.getBaseMaxStoredExp(),
                spawnerData.getSpawnDelay()
        );
    }
}
