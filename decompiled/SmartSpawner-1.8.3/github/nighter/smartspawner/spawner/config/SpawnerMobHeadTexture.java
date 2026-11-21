package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class SpawnerMobHeadTexture {
   private static final Map<String, SkullMeta> CUSTOM_TEXTURE_META_CACHE = new ConcurrentHashMap<>();
   private static final ItemStack DEFAULT_SPAWNER_BLOCK = new ItemStack(Material.SPAWNER);

   public static ItemStack getSpawnerHead(SpawnerData spawner, Consumer<ItemMeta> metaModifier) {
      return getHead(spawner == null ? null : spawner.getHead(), metaModifier);
   }

   public static ItemStack getCustomHead(EntityType entityType, Player player, Consumer<ItemMeta> metaModifier) {
      return getCustomHead(entityType, metaModifier);
   }

   public static ItemStack getCustomHead(EntityType entityType) {
      return getCustomHead(entityType, (Consumer<ItemMeta>)null);
   }

   public static ItemStack getCustomHead(EntityType entityType, Consumer<ItemMeta> metaModifier) {
      SmartSpawner plugin = SmartSpawner.getInstance();
      return entityType != null && plugin != null && plugin.getSpawnerSettingsConfig() != null
         ? getHead(plugin.getSpawnerSettingsConfig().getBaseHead(entityType), metaModifier)
         : getHead(null, metaModifier);
   }

   public static ItemStack getItemSpawnerHead(Material itemMaterial, Player player, Consumer<ItemMeta> metaModifier) {
      return getItemSpawnerHead(itemMaterial, metaModifier);
   }

   public static ItemStack getItemSpawnerHead(Material itemMaterial, Consumer<ItemMeta> metaModifier) {
      SmartSpawner plugin = SmartSpawner.getInstance();
      return itemMaterial != null && plugin != null && plugin.getItemSpawnerSettingsConfig() != null
         ? getHead(plugin.getItemSpawnerSettingsConfig().getBaseHead(itemMaterial), metaModifier)
         : getHead(null, metaModifier);
   }

   public static ItemStack getHead(SpawnerHead head, Consumer<ItemMeta> metaModifier) {
      if (head == null) {
         return modified(DEFAULT_SPAWNER_BLOCK.clone(), metaModifier);
      } else {
         return head.hasTexture() ? getCustomHeadFromTexture(head.texture(), metaModifier) : modified(new ItemStack(head.material()), metaModifier);
      }
   }

   private static ItemStack modified(ItemStack item, Consumer<ItemMeta> metaModifier) {
      if (metaModifier != null) {
         item.editMeta(metaModifier);
      }

      return item;
   }

   public static ItemStack getCustomHeadFromTexture(String texture, Consumer<ItemMeta> metaModifier) {
      if (texture != null && !texture.trim().isEmpty()) {
         String trimmedTexture = texture.trim();
         SkullMeta baseMeta = CUSTOM_TEXTURE_META_CACHE.get(trimmedTexture);
         if (baseMeta == null) {
            try {
               PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
               PlayerTextures textures = profile.getTextures();
               URL url = new URL("http://textures.minecraft.net/texture/" + trimmedTexture);
               textures.setSkin(url);
               profile.setTextures(textures);
               ItemStack tempHead = new ItemStack(Material.PLAYER_HEAD);
               SkullMeta tempMeta = (SkullMeta)tempHead.getItemMeta();
               tempMeta.setOwnerProfile(profile);
               baseMeta = tempMeta.clone();
               CUSTOM_TEXTURE_META_CACHE.put(trimmedTexture, baseMeta);
            } catch (Exception var9) {
               var9.printStackTrace();
               return modified(new ItemStack(Material.PLAYER_HEAD), metaModifier);
            }
         }

         ItemStack head = new ItemStack(Material.PLAYER_HEAD);
         SkullMeta meta = baseMeta.clone();
         if (metaModifier != null) {
            metaModifier.accept(meta);
         }

         head.setItemMeta(meta);
         return head;
      } else {
         return modified(new ItemStack(Material.PLAYER_HEAD), metaModifier);
      }
   }

   public static void clearCache() {
      CUSTOM_TEXTURE_META_CACHE.clear();
   }

   public static void prewarmCache() {
      SmartSpawner plugin = SmartSpawner.getInstance();
      if (plugin != null) {
         Scheduler.runTaskAsync(
            () -> {
               SpawnerSettingsConfig settingsConfig = plugin.getSpawnerSettingsConfig();
               if (settingsConfig != null) {
                  EntityType[] commonTypes = new EntityType[]{
                     EntityType.ZOMBIE,
                     EntityType.SKELETON,
                     EntityType.CREEPER,
                     EntityType.SPIDER,
                     EntityType.ENDERMAN,
                     EntityType.BLAZE,
                     EntityType.SLIME,
                     EntityType.MAGMA_CUBE,
                     EntityType.GHAST,
                     EntityType.PIG,
                     EntityType.COW,
                     EntityType.CHICKEN,
                     EntityType.SHEEP,
                     EntityType.IRON_GOLEM,
                     EntityType.WITHER_SKELETON,
                     EntityType.ZOGLIN,
                     EntityType.HOGLIN,
                     EntityType.CAVE_SPIDER
                  };

                  for (EntityType type : commonTypes) {
                     try {
                        SpawnerHead head = settingsConfig.getBaseHead(type);
                        if (head.hasTexture()) {
                           getCustomHeadFromTexture(head.texture(), null);
                        }
                     } catch (Exception var8) {
                     }
                  }
               }
            }
         );
      }
   }
}
