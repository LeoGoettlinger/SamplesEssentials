package github.nighter.smartspawner.api;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.data.SpawnerDataModifier;
import github.nighter.smartspawner.api.gui.GuiLayoutRegistry;
import github.nighter.smartspawner.api.gui.GuiLayoutRegistryImpl;
import github.nighter.smartspawner.api.gui.SpawnerGuiLayoutProvider;
import github.nighter.smartspawner.api.impl.SpawnerDataModifierImpl;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.interactions.destroy.SpawnerRemovalService;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

public class SmartSpawnerAPIImpl implements SmartSpawnerAPI {
   private final SmartSpawner plugin;
   private final SpawnerItemFactory itemFactory;
   private final SpawnerManager spawnerManager;
   private final SpawnerRemovalService spawnerRemovalService;
   private final GuiLayoutRegistryImpl guiLayoutRegistry;
   private final NamespacedKey vanillaSpawnerKey;
   private final NamespacedKey itemSpawnerMaterialKey;
   private volatile SpawnerGuiLayoutProvider spawnerGuiLayoutProvider;

   public SmartSpawnerAPIImpl(SmartSpawner plugin) {
      this.plugin = plugin;
      this.itemFactory = new SpawnerItemFactory(plugin);
      this.spawnerManager = plugin.getSpawnerManager();
      this.spawnerRemovalService = plugin.getSpawnerRemovalService();
      this.guiLayoutRegistry = plugin.getGuiLayoutRegistry();
      this.vanillaSpawnerKey = new NamespacedKey(plugin, "vanilla_spawner");
      this.itemSpawnerMaterialKey = new NamespacedKey(plugin, "item_spawner_material");
   }

   @Override
   public ItemStack createSpawnerItem(EntityType entityType) {
      return this.itemFactory.createSmartSpawnerItem(entityType);
   }

   @Override
   public ItemStack createSpawnerItem(EntityType entityType, int amount) {
      return this.itemFactory.createSmartSpawnerItem(entityType, amount);
   }

   @Override
   public ItemStack createVanillaSpawnerItem(EntityType entityType) {
      return this.itemFactory.createVanillaSpawnerItem(entityType);
   }

   @Override
   public ItemStack createVanillaSpawnerItem(EntityType entityType, int amount) {
      return this.itemFactory.createVanillaSpawnerItem(entityType, amount);
   }

   @Override
   public ItemStack createItemSpawnerItem(Material itemMaterial) {
      return this.itemFactory.createItemSpawnerItem(itemMaterial);
   }

   @Override
   public ItemStack createItemSpawnerItem(Material itemMaterial, int amount) {
      return this.itemFactory.createItemSpawnerItem(itemMaterial, amount);
   }

   @Override
   public boolean isSmartSpawner(ItemStack item) {
      return item != null && item.getType() == Material.SPAWNER ? !this.hasVanillaSpawnerKey(item) && !this.hasItemSpawnerKey(item) : false;
   }

   @Override
   public boolean isVanillaSpawner(ItemStack item) {
      return item != null && item.getType() == Material.SPAWNER ? this.hasVanillaSpawnerKey(item) : false;
   }

   @Override
   public boolean isItemSpawner(ItemStack item) {
      return item != null && item.getType() == Material.SPAWNER ? this.hasItemSpawnerKey(item) : false;
   }

   @Override
   public EntityType getSpawnerEntityType(ItemStack item) {
      if (item != null && item.getType() == Material.SPAWNER && item.hasItemMeta()) {
         if (item.getItemMeta() instanceof BlockStateMeta blockMeta) {
            return blockMeta.getBlockState() instanceof CreatureSpawner cs ? cs.getSpawnedType() : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   @Override
   public Material getItemSpawnerMaterial(ItemStack item) {
      if (!this.isItemSpawner(item)) {
         return null;
      } else {
         String materialName = (String)item.getPersistentDataContainer().get(this.itemSpawnerMaterialKey, PersistentDataType.STRING);
         if (materialName == null) {
            return null;
         } else {
            try {
               return Material.valueOf(materialName);
            } catch (IllegalArgumentException var4) {
               return null;
            }
         }
      }
   }

   @Override
   public SpawnerDataDTO getSpawnerByLocation(Location location) {
      if (location == null) {
         return null;
      } else {
         SpawnerData spawnerData = this.spawnerManager.getSpawnerByLocation(location);
         return spawnerData != null ? convertToDTO(spawnerData) : null;
      }
   }

   @Override
   public SpawnerDataDTO getSpawnerById(String spawnerId) {
      if (spawnerId == null) {
         return null;
      } else {
         SpawnerData spawnerData = this.spawnerManager.getSpawnerById(spawnerId);
         return spawnerData != null ? convertToDTO(spawnerData) : null;
      }
   }

   @Override
   public List<SpawnerDataDTO> getAllSpawners() {
      return this.spawnerManager.getAllSpawners().stream().map(SmartSpawnerAPIImpl::convertToDTO).collect(Collectors.toList());
   }

   @Override
   public SpawnerDataModifier getSpawnerModifier(String spawnerId) {
      if (spawnerId == null) {
         return null;
      } else {
         SpawnerData spawnerData = this.spawnerManager.getSpawnerById(spawnerId);
         return spawnerData != null ? new SpawnerDataModifierImpl(spawnerData) : null;
      }
   }

   @Override
   public CompletableFuture<Boolean> removeSpawner(String spawnerId) {
      if (spawnerId == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         SpawnerData spawnerData = this.spawnerManager.getSpawnerById(spawnerId);
         return spawnerData == null ? CompletableFuture.completedFuture(false) : this.spawnerRemovalService.removeSpawner(spawnerData);
      }
   }

   @Override
   public CompletableFuture<Boolean> removeSpawner(Location location) {
      if (location == null) {
         return CompletableFuture.completedFuture(false);
      } else {
         SpawnerData spawnerData = this.spawnerManager.getSpawnerByLocation(location);
         return spawnerData == null ? CompletableFuture.completedFuture(false) : this.spawnerRemovalService.removeSpawner(spawnerData);
      }
   }

   @Override
   public GuiLayoutRegistry getLayoutRegistry() {
      return this.guiLayoutRegistry;
   }

   @Override
   public void setSpawnerLayoutProvider(SpawnerGuiLayoutProvider provider) {
      this.spawnerGuiLayoutProvider = provider;
      this.plugin.getGuiLayoutConfig().setProvider(provider);
   }

   @Override
   public void clearSpawnerLayoutProvider() {
      this.spawnerGuiLayoutProvider = null;
      this.plugin.getGuiLayoutConfig().setProvider(null);
   }

   private boolean hasVanillaSpawnerKey(ItemStack spawnerItem) {
      return spawnerItem.getPersistentDataContainer().has(this.vanillaSpawnerKey, PersistentDataType.BOOLEAN);
   }

   private boolean hasItemSpawnerKey(ItemStack spawnerItem) {
      return spawnerItem.getPersistentDataContainer().has(this.itemSpawnerMaterialKey, PersistentDataType.STRING);
   }

   public SpawnerGuiLayoutProvider getSpawnerLayoutProvider() {
      return this.spawnerGuiLayoutProvider;
   }

   public static SpawnerDataDTO convertToDTO(SpawnerData spawnerData) {
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
