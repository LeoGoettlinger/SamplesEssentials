package com.viaversion.viaversion.protocols.v1_20_2to1_20_3.storage;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ScoreboardStorage {
   private static final int SIDEBAR_SLOT = 1;
   private static final int FIRST_TEAM_SIDEBAR_SLOT = 3;
   private static final int LAST_TEAM_SIDEBAR_SLOT = 18;
   private final Map<String, ScoreboardStorage.Objective> objectives = new HashMap<>();
   private final Int2ObjectMap<String> displaySlots = new Int2ObjectOpenHashMap<>();

   public void putObjective(String name, Tag displayName, int renderType, boolean numberFormatHidden) {
      this.objectives.put(name, new ScoreboardStorage.Objective(displayName, renderType, numberFormatHidden));
   }

   public void removeObjective(String name) {
      this.objectives.remove(name);
      this.displaySlots.values().removeIf(name::equals);
   }

   @Nullable
   public ScoreboardStorage.Objective objective(String name) {
      return this.objectives.get(name);
   }

   public void setNumberFormatHidden(String name, boolean numberFormatHidden) {
      ScoreboardStorage.Objective objective = this.objectives.get(name);
      if (objective != null) {
         this.objectives.put(name, new ScoreboardStorage.Objective(objective.displayName(), objective.renderType(), numberFormatHidden));
      }
   }

   @Nullable
   public String setDisplaySlot(int slot, String objectiveName) {
      return objectiveName.isEmpty() ? this.displaySlots.remove(slot) : this.displaySlots.put(slot, objectiveName);
   }

   public boolean isSidebar(String objectiveName) {
      for (Int2ObjectMap.Entry<String> entry : this.displaySlots.int2ObjectEntrySet()) {
         if (isSidebarSlot(entry.getIntKey()) && objectiveName.equals(entry.getValue())) {
            return true;
         }
      }

      return false;
   }

   private static boolean isSidebarSlot(int slot) {
      return slot == 1 || slot >= 3 && slot <= 18;
   }

   public record Objective(Tag displayName, int renderType, boolean numberFormatHidden) {
   }
}
