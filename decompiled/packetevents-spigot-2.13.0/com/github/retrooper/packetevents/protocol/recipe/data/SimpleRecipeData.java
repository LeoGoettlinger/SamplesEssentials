package com.github.retrooper.packetevents.protocol.recipe.data;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.recipe.CraftingCategory;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import org.jetbrains.annotations.ApiStatus.Obsolete;

@Obsolete
public class SimpleRecipeData implements RecipeData {
   private final CraftingCategory category;

   public SimpleRecipeData(CraftingCategory category) {
      this.category = category;
   }

   public static SimpleRecipeData read(PacketWrapper<?> wrapper) {
      CraftingCategory category = wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)
         ? wrapper.readEnum(CraftingCategory.values())
         : CraftingCategory.MISC;
      return new SimpleRecipeData(category);
   }

   public static void write(PacketWrapper<?> wrapper, SimpleRecipeData data) {
      if (wrapper.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19_3)) {
         wrapper.writeEnum(data.getCategory());
      }
   }

   public CraftingCategory getCategory() {
      return this.category;
   }
}
