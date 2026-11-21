package com.sk89q.worldedit.extent.clipboard.io.sponge;

import com.sk89q.worldedit.world.DataFixer;
import javax.annotation.Nullable;

public final class VersionedDataFixer {
   private final int dataVersion;
   @Nullable
   private final DataFixer fixer;

   public VersionedDataFixer(int dataVersion, @Nullable DataFixer fixer) {
      this.dataVersion = dataVersion;
      this.fixer = fixer;
   }

   public boolean isActive() {
      return this.fixer != null;
   }

   public <T> T fixUp(DataFixer.FixType<T> type, T original) {
      return !this.isActive() ? original : this.fixer.fixUp(type, original, this.dataVersion);
   }
}
