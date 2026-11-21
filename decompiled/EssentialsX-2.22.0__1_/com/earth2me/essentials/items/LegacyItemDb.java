package com.earth2me.essentials.items;

import com.earth2me.essentials.ManagedFile;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ess3.api.IEssentials;
import net.ess3.api.TranslatableException;
import net.ess3.provider.PersistentDataProvider;
import net.ess3.provider.PotionMetaProvider;
import net.ess3.provider.SpawnEggProvider;
import net.ess3.provider.SpawnerItemProvider;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class LegacyItemDb extends AbstractItemDb {
   private final transient Map<String, Integer> items = new HashMap<>();
   private final transient Map<LegacyItemDb.ItemData, List<String>> names = new HashMap<>();
   private final transient Map<LegacyItemDb.ItemData, String> primaryName = new HashMap<>();
   private final transient Map<Integer, LegacyItemDb.ItemData> legacyIds = new HashMap<>();
   private final transient Map<String, Short> durabilities = new HashMap<>();
   private final transient Map<String, String> nbtData = new HashMap<>();
   private final transient ManagedFile file;
   private final transient Pattern splitPattern = Pattern.compile("((.*)[:+',;.](\\d+))");
   private final transient Pattern csvSplitPattern = Pattern.compile("(\"([^\"]*)\"|[^,]*)(,|$)");

   public LegacyItemDb(final IEssentials ess) {
      super(ess);
      this.file = new ManagedFile("items.csv", ess);
   }

   @Override
   public void reloadConfig() {
      List<String> lines = this.file.getLines();
      if (!lines.isEmpty()) {
         this.ready = false;
         this.durabilities.clear();
         this.items.clear();
         this.names.clear();
         this.primaryName.clear();

         for (String line : lines) {
            if (line.length() <= 0 || line.charAt(0) != '#') {
               String itemName = null;
               int numeric = -1;
               short data = 0;
               String nbt = null;
               int col = 0;
               Matcher matcher = this.csvSplitPattern.matcher(line);

               while (matcher.find()) {
                  String match = matcher.group(1);
                  if (StringUtil.stripToNull(match) != null) {
                     match = StringUtil.strip(match.trim(), "\"");
                     switch (col) {
                        case 0:
                           itemName = match.toLowerCase(Locale.ENGLISH);
                           break;
                        case 1:
                           numeric = Integer.parseInt(match);
                           break;
                        case 2:
                           data = Short.parseShort(match);
                           break;
                        case 3:
                           nbt = StringUtil.stripToNull(match);
                           break;
                        default:
                           continue;
                     }

                     col++;
                  }
               }

               if (itemName != null && numeric >= 0) {
                  this.durabilities.put(itemName, data);
                  this.items.put(itemName, numeric);
                  if (nbt != null) {
                     this.nbtData.put(itemName, nbt);
                  }

                  LegacyItemDb.ItemData itemData = new LegacyItemDb.ItemData(numeric, data);
                  if (this.names.containsKey(itemData)) {
                     List<String> nameList = this.names.get(itemData);
                     nameList.add(itemName);
                  } else {
                     List<String> nameList = new ArrayList<>();
                     nameList.add(itemName);
                     this.names.put(itemData, nameList);
                     this.primaryName.put(itemData, itemName);
                  }

                  this.legacyIds.put(numeric, itemData);
               }
            }
         }

         for (List<String> nameList : this.names.values()) {
            nameList.sort(LegacyItemDb.LengthCompare.INSTANCE);
         }

         this.ess.getLogger().info(String.format("Loaded %s items from items.csv.", this.listNames().size()));
         this.ready = true;
      }
   }

   @Override
   public ItemStack get(final String id, final boolean useResolvers) throws Exception {
      if (useResolvers) {
         ItemStack resolved = this.tryResolverDeserialize(id);
         if (resolved != null) {
            return resolved;
         }
      }

      int itemid = 0;
      short metaData = 0;
      Matcher parts = this.splitPattern.matcher(id);
      String itemname;
      if (parts.matches()) {
         itemname = parts.group(2);
         metaData = Short.parseShort(parts.group(3));
      } else {
         itemname = id;
      }

      if (NumberUtil.isInt(itemname)) {
         itemid = Integer.parseInt(itemname);
      } else if (NumberUtil.isInt(id)) {
         itemid = Integer.parseInt(id);
      } else {
         itemname = itemname.toLowerCase(Locale.ENGLISH);
      }

      if (itemid < 1 && this.items.containsKey(itemname)) {
         itemid = this.items.get(itemname);
         if (this.durabilities.containsKey(itemname) && metaData == 0) {
            metaData = this.durabilities.get(itemname);
         }
      }

      if (itemid < 1) {
         Material matFromName = EnumUtil.getMaterial(itemname.toUpperCase());
         if (matFromName != null) {
            itemid = matFromName.getId();
         }
      }

      if (itemid < 1) {
         throw new TranslatableException("unknownItemName", itemname);
      } else {
         LegacyItemDb.ItemData data = this.legacyIds.get(itemid);
         if (data == null) {
            throw new TranslatableException("unknownItemId", itemid);
         } else {
            Material mat = this.getFromLegacy(itemid, (byte)metaData);
            ItemStack retval = new ItemStack(mat);
            if (this.nbtData.containsKey(itemname)) {
               String nbt = this.nbtData.get(itemname);
               if (nbt.startsWith("*")) {
                  nbt = this.nbtData.get(nbt.substring(1));
               }

               retval = this.ess.getServer().getUnsafe().modifyItemStack(retval, nbt);
            }

            Material MOB_SPAWNER = EnumUtil.getMaterial("SPAWNER", "MOB_SPAWNER");
            if (mat == MOB_SPAWNER) {
               if (metaData == 0) {
                  metaData = EntityType.PIG.getTypeId();
               }

               try {
                  retval = this.ess.provider(SpawnerItemProvider.class).setEntityType(retval, EntityType.fromId(metaData));
                  this.ess.provider(PersistentDataProvider.class).set(retval, "convert", "true");
               } catch (IllegalArgumentException var14) {
                  throw new Exception("Can't spawn entity ID " + metaData + " from mob spawners.");
               }
            } else if (mat.name().contains("MONSTER_EGG")) {
               EntityType type;
               try {
                  type = EntityType.fromId(metaData);
               } catch (IllegalArgumentException var13) {
                  throw new Exception("Can't spawn entity ID " + metaData + " from spawn eggs.");
               }

               retval = this.ess.provider(SpawnEggProvider.class).createEggItem(type);
            } else if (mat.name().endsWith("POTION") && VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_11_R01)) {
               retval = this.ess.provider(PotionMetaProvider.class).createPotionItem(mat, metaData);
            } else {
               retval.setDurability(metaData);
            }

            retval.setAmount(mat.getMaxStackSize());
            return retval;
         }
      }
   }

   @Override
   public List<String> nameList(final ItemStack item) {
      LegacyItemDb.ItemData itemData = new LegacyItemDb.ItemData(item.getType().getId(), item.getDurability());
      List<String> nameList = this.names.get(itemData);
      if (nameList == null) {
         itemData = new LegacyItemDb.ItemData(item.getType().getId(), (short)0);
         nameList = this.names.get(itemData);
         if (nameList == null) {
            return null;
         }
      }

      return Collections.unmodifiableList(nameList);
   }

   @Override
   public String name(final ItemStack item) {
      LegacyItemDb.ItemData itemData = new LegacyItemDb.ItemData(item.getType().getId(), item.getDurability());
      String name = this.primaryName.get(itemData);
      if (name == null) {
         itemData = new LegacyItemDb.ItemData(item.getType().getId(), (short)0);
         name = this.primaryName.get(itemData);
      }

      return name;
   }

   @Override
   public int getLegacyId(final Material material) throws Exception {
      for (Entry<String, Integer> entry : this.items.entrySet()) {
         if (material.name().toLowerCase(Locale.ENGLISH).equalsIgnoreCase(entry.getKey())) {
            return entry.getValue();
         }
      }

      throw new Exception("Itemid not found for material: " + material.name());
   }

   @Override
   public Collection<String> listNames() {
      Collection<String> values = new ArrayList<>(this.primaryName.values());
      values.addAll(this.getResolverNames());
      return values;
   }

   static class ItemData {
      private final int itemNo;
      private final short itemData;

      ItemData(final int itemNo, final short itemData) {
         this.itemNo = itemNo;
         this.itemData = itemData;
      }

      public int getItemNo() {
         return this.itemNo;
      }

      public short getItemData() {
         return this.itemData;
      }

      @Override
      public int hashCode() {
         return 31 * this.itemNo ^ this.itemData;
      }

      @Override
      public boolean equals(final Object o) {
         if (o == null) {
            return false;
         } else if (!(o instanceof LegacyItemDb.ItemData)) {
            return false;
         } else {
            LegacyItemDb.ItemData pairo = (LegacyItemDb.ItemData)o;
            return this.itemNo == pairo.getItemNo() && this.itemData == pairo.getItemData();
         }
      }
   }

   static class LengthCompare implements Comparator<String> {
      private static final LegacyItemDb.LengthCompare INSTANCE = new LegacyItemDb.LengthCompare();

      public int compare(final String s1, final String s2) {
         return s1.length() - s2.length();
      }
   }
}
