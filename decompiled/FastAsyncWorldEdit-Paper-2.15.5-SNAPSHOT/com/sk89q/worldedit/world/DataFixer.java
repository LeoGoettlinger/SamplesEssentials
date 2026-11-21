package com.sk89q.worldedit.world;

import com.google.common.annotations.Beta;
import org.enginehub.linbus.tree.LinCompoundTag;

@Beta
public interface DataFixer {
   default <T> T fixUp(DataFixer.FixType<T> type, T original) {
      return this.fixUp(type, original, -1);
   }

   <T> T fixUp(DataFixer.FixType<T> type, T original, int srcVer);

   public static final class FixType<T> {
      private FixType() {
      }
   }

   public static final class FixTypes {
      public static final DataFixer.FixType<LinCompoundTag> CHUNK = new DataFixer.FixType<>();
      public static final DataFixer.FixType<LinCompoundTag> BLOCK_ENTITY = new DataFixer.FixType<>();
      public static final DataFixer.FixType<LinCompoundTag> ENTITY = new DataFixer.FixType<>();
      public static final DataFixer.FixType<String> BLOCK_STATE = new DataFixer.FixType<>();
      public static final DataFixer.FixType<String> BIOME = new DataFixer.FixType<>();
      public static final DataFixer.FixType<String> ITEM_TYPE = new DataFixer.FixType<>();

      private FixTypes() {
      }
   }
}
