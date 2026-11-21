package com.earth2me.essentials.items;

import com.earth2me.essentials.ManagedFile;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.ess3.api.IEssentials;
import net.ess3.api.TranslatableException;
import net.ess3.provider.PersistentDataProvider;
import net.ess3.provider.PotionMetaProvider;
import net.ess3.provider.SpawnerItemProvider;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class FlatItemDb extends AbstractItemDb {
   private static final Gson gson = new Gson();
   private final transient Map<String, FlatItemDb.ItemData> items = new HashMap<>();
   private final transient Map<String, String> itemAliases = new HashMap<>();
   private final transient Set<String> allAliases = new HashSet<>();
   private transient ManagedFile file = null;

   public FlatItemDb(final IEssentials ess) {
      super(ess);
   }

   @Override
   public void reloadConfig() {
      if (this.file == null) {
         this.file = new ManagedFile("items.json", this.ess);
      }

      this.rebuild();
      this.ess.getLogger().info(String.format("Loaded %s items from items.json.", this.listNames().size()));
   }

   private void rebuild() {
      this.reset();
      String json = this.file.getLines().stream().filter(line -> !line.startsWith("#")).collect(Collectors.joining());
      this.loadJSON(String.join("\n", json));
      this.ready = true;
   }

   private void reset() {
      this.ready = false;
      this.items.clear();
      this.itemAliases.clear();
      this.allAliases.clear();
   }

   private void loadJSON(final String source) {
      JsonObject map = new JsonParser().parse(source).getAsJsonObject();

      for (Entry<String, JsonElement> entry : map.entrySet()) {
         String key = entry.getKey();
         JsonElement element = entry.getValue();
         boolean valid = false;
         if (element.isJsonObject()) {
            FlatItemDb.ItemData data = (FlatItemDb.ItemData)gson.fromJson(element, FlatItemDb.ItemData.class);
            this.items.put(key, data);
            valid = true;
         } else {
            try {
               String target = element.getAsString();
               this.itemAliases.put(key, target);
               valid = true;
            } catch (Exception var9) {
            }
         }

         if (valid) {
            this.allAliases.add(key);
         } else {
            this.ess.getLogger().warning(String.format("Failed to add item: \"%s\": %s", key, element.toString()));
         }
      }
   }

   @Override
   public ItemStack get(String id, final boolean useResolvers) throws Exception {
      if (useResolvers) {
         ItemStack resolved = this.tryResolverDeserialize(id);
         if (resolved != null) {
            return resolved;
         }
      }

      id = id.toLowerCase();
      String[] split = id.split("(?<!^minecraft):");
      FlatItemDb.ItemData data = this.getByName(split[0]);
      if (data == null) {
         throw new TranslatableException("unknownItemName", id);
      } else {
         Material material = data.getMaterial();
         if (!material.isItem()) {
            throw new TranslatableException("unableToSpawnItem", id);
         } else {
            ItemStack stack = new ItemStack(material);
            stack.setAmount(material.getMaxStackSize());
            FlatItemDb.ItemData.EssentialPotionData potionData = data.getPotionData();
            if (potionData != null && stack.getItemMeta() instanceof PotionMeta) {
               this.ess.provider(PotionMetaProvider.class).setBasePotionType(stack, potionData.getType(), potionData.isExtended(), potionData.isUpgraded());
            }

            ItemMeta meta = stack.getItemMeta();
            if (split.length > 1 && meta instanceof Damageable) {
               Damageable damageMeta = (Damageable)meta;
               damageMeta.setDamage(Integer.parseInt(split[1]));
            }

            stack.setItemMeta(meta);
            EntityType entity = data.getEntity();
            if (entity != null && material.toString().contains("SPAWNER")) {
               this.ess.provider(SpawnerItemProvider.class).setEntityType(stack, entity);
               this.ess.provider(PersistentDataProvider.class).set(stack, "convert", "true");
            }

            return stack;
         }
      }
   }

   private FlatItemDb.ItemData getByName(String name) {
      name = name.toLowerCase();
      if (this.items.containsKey(name)) {
         return this.items.get(name);
      } else {
         return this.itemAliases.containsKey(name) ? this.items.get(this.itemAliases.get(name)) : null;
      }
   }

   @Override
   public List<String> nameList(final ItemStack item) {
      List<String> names = new ArrayList<>();
      String primaryName = this.name(item);
      names.add(primaryName);

      for (Entry<String, String> entry : this.itemAliases.entrySet()) {
         if (entry.getValue().equalsIgnoreCase(primaryName)) {
            names.add(entry.getKey());
         }
      }

      return names;
   }

   @Override
   public String name(final ItemStack item) {
      FlatItemDb.ItemData data = this.lookup(item);

      for (Entry<String, FlatItemDb.ItemData> entry : this.items.entrySet()) {
         if (entry.getValue().equals(data)) {
            return entry.getKey();
         }
      }

      return null;
   }

   @Deprecated
   @Override
   public int getLegacyId(final Material material) {
      throw new UnsupportedOperationException("Legacy IDs aren't supported on this version.");
   }

   private FlatItemDb.ItemData lookup(final ItemStack is) {
      Material type = is.getType();
      if (MaterialUtil.isPotion(type) && is.getItemMeta() instanceof PotionMeta) {
         PotionMetaProvider provider = this.ess.provider(PotionMetaProvider.class);
         return new FlatItemDb.ItemData(
            type, new FlatItemDb.ItemData.EssentialPotionData(provider.getBasePotionType(is), provider.isUpgraded(is), provider.isExtended(is))
         );
      } else if (type.toString().contains("SPAWNER")) {
         EntityType entity = this.ess.provider(SpawnerItemProvider.class).getEntityType(is);
         return new FlatItemDb.ItemData(type, entity);
      } else {
         return new FlatItemDb.ItemData(type);
      }
   }

   @Override
   public Collection<String> listNames() {
      Set<String> names = new HashSet<>(this.allAliases);
      names.addAll(this.getResolverNames());
      return names;
   }

   public static class ItemData {
      private Material material;
      private String[] fallbacks = null;
      private FlatItemDb.ItemData.EssentialPotionData potionData = null;
      private EntityType entity = null;

      ItemData(final Material material) {
         this.material = material;
      }

      ItemData(final Material material, final FlatItemDb.ItemData.EssentialPotionData potionData) {
         this.material = material;
         this.potionData = potionData;
      }

      ItemData(final Material material, final EntityType entity) {
         this.material = material;
         this.entity = entity;
      }

      @Override
      public int hashCode() {
         return 31 * this.material.hashCode() ^ this.potionData.hashCode();
      }

      @Override
      public boolean equals(final Object o) {
         if (o == null) {
            return false;
         } else if (!(o instanceof FlatItemDb.ItemData)) {
            return false;
         } else {
            FlatItemDb.ItemData that = (FlatItemDb.ItemData)o;
            return this.getMaterial() == that.getMaterial() && this.potionDataEquals(that) && this.entityEquals(that);
         }
      }

      public Material getMaterial() {
         if (this.material == null && this.fallbacks != null) {
            this.material = EnumUtil.getMaterial(this.fallbacks);
            this.fallbacks = null;
         }

         return this.material;
      }

      public FlatItemDb.ItemData.EssentialPotionData getPotionData() {
         return this.potionData;
      }

      public EntityType getEntity() {
         return this.entity;
      }

      private boolean potionDataEquals(final FlatItemDb.ItemData o) {
         if (this.potionData == null && o.getPotionData() == null) {
            return true;
         } else {
            return this.potionData != null && o.getPotionData() != null ? this.potionData.equals(o.getPotionData()) : false;
         }
      }

      private boolean entityEquals(final FlatItemDb.ItemData o) {
         if (this.entity == null && o.getEntity() == null) {
            return true;
         } else {
            return this.entity != null && o.getEntity() != null ? this.entity.equals(o.getEntity()) : false;
         }
      }

      public static class EssentialPotionData {
         private PotionType type;
         private String fallbackType;
         private final boolean upgraded;
         private final boolean extended;

         EssentialPotionData(PotionType type, boolean upgraded, boolean extended) {
            this.type = type;
            this.upgraded = upgraded;
            this.extended = extended;
         }

         public PotionType getType() {
            if (this.type == null && this.fallbackType != null) {
               this.type = EnumUtil.valueOf(PotionType.class, this.fallbackType);
               this.fallbackType = null;
            }

            return this.type;
         }

         public boolean isUpgraded() {
            return this.upgraded;
         }

         public boolean isExtended() {
            return this.extended;
         }

         @Override
         public boolean equals(Object o) {
            if (this == o) {
               return true;
            } else if (o != null && this.getClass() == o.getClass()) {
               FlatItemDb.ItemData.EssentialPotionData that = (FlatItemDb.ItemData.EssentialPotionData)o;
               return this.upgraded == that.upgraded && this.extended == that.extended && this.getType() == that.getType();
            } else {
               return false;
            }
         }

         @Override
         public int hashCode() {
            return Objects.hash(this.getType(), this.upgraded, this.extended);
         }
      }
   }
}
