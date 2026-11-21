package ch.njol.skript.aliases;

import ch.njol.skript.entity.EntityData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

public class AliasesMap {
   private AliasesMap.MaterialEntry[] materialEntries;

   public AliasesMap() {
      this.clear();
   }

   private AliasesMap.MaterialEntry getEntry(ItemData item) {
      AliasesMap.MaterialEntry entry = this.materialEntries[item.getType().ordinal()];

      assert entry != null;

      return entry;
   }

   public void addAlias(AliasesMap.AliasData data) {
      AliasesMap.MaterialEntry entry = this.getEntry(data.getItem());
      if (data.getItem().isDefault()) {
         entry.defaultItem = data;
      } else {
         entry.items.add(data);
      }
   }

   public AliasesMap.Match matchAlias(ItemData item) {
      AliasesMap.MaterialEntry entry = this.getEntry(item);
      if (entry.defaultItem == null && entry.items.isEmpty()) {
         return new AliasesMap.Match(MatchQuality.DIFFERENT, null);
      } else {
         MatchQuality maxQuality = MatchQuality.DIFFERENT;
         AliasesMap.AliasData bestMatch = null;

         for (AliasesMap.AliasData data : entry.items) {
            MatchQuality quality = item.matchAlias(data.getItem());
            if (quality.isBetter(maxQuality)) {
               maxQuality = quality;
               bestMatch = data;
            }
         }

         if (!maxQuality.isBetter(MatchQuality.SAME_MATERIAL)) {
            AliasesMap.AliasData defaultItem = entry.defaultItem;
            if (defaultItem != null) {
               return new AliasesMap.Match(item.matchAlias(defaultItem.getItem()), defaultItem);
            } else if (bestMatch != null) {
               return new AliasesMap.Match(MatchQuality.SAME_MATERIAL, bestMatch);
            } else {
               throw new AssertionError();
            }
         } else {
            assert bestMatch != null;

            return new AliasesMap.Match(maxQuality, bestMatch);
         }
      }
   }

   public AliasesMap.Match exactMatch(ItemData item) {
      AliasesMap.MaterialEntry entry = this.getEntry(item);
      if (entry.defaultItem == null && entry.items.isEmpty()) {
         return new AliasesMap.Match(MatchQuality.DIFFERENT, null);
      } else {
         for (AliasesMap.AliasData data : entry.items) {
            if (item.matchAlias(data.getItem()) == MatchQuality.EXACT) {
               return new AliasesMap.Match(MatchQuality.EXACT, data);
            }
         }

         return new AliasesMap.Match(MatchQuality.DIFFERENT, null);
      }
   }

   public void clear() {
      this.materialEntries = new AliasesMap.MaterialEntry[Material.values().length];

      for (int i = 0; i < this.materialEntries.length; i++) {
         this.materialEntries[i] = new AliasesMap.MaterialEntry();
      }
   }

   public static class AliasData {
      private final ItemData item;
      private final MaterialName name;
      private final String minecraftId;
      @Nullable
      private final EntityData<?> relatedEntity;

      public AliasData(ItemData item, MaterialName name, String minecraftId, @Nullable EntityData<?> relatedEntity) {
         this.item = item;
         this.name = name;
         this.minecraftId = minecraftId;
         this.relatedEntity = relatedEntity;
      }

      public ItemData getItem() {
         return this.item;
      }

      public MaterialName getName() {
         return this.name;
      }

      public String getMinecraftId() {
         return this.minecraftId;
      }

      @Nullable
      public EntityData<?> getRelatedEntity() {
         return this.relatedEntity;
      }
   }

   public static class Match {
      private final MatchQuality quality;
      @Nullable
      private final AliasesMap.AliasData data;

      public Match(MatchQuality quality, @Nullable AliasesMap.AliasData data) {
         this.quality = quality;
         this.data = data;
      }

      public MatchQuality getQuality() {
         return this.quality;
      }

      @Nullable
      public AliasesMap.AliasData getData() {
         return this.data;
      }
   }

   private static class MaterialEntry {
      @Nullable
      public AliasesMap.AliasData defaultItem;
      public final List<AliasesMap.AliasData> items = new ArrayList<>();

      public MaterialEntry() {
      }
   }
}
