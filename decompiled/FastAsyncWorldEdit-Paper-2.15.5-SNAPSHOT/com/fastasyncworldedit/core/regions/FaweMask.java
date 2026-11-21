package com.fastasyncworldedit.core.regions;

import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.regions.Region;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FaweMask implements IDelegateRegion {
   private final Region region;

   public FaweMask(@Nonnull Region region) {
      this.region = Objects.requireNonNull(region);
   }

   @Override
   public Region getRegion() {
      return this.region;
   }

   public boolean isValid(Player player, FaweMaskManager.MaskType type) {
      return false;
   }

   public boolean isValid(Player player, FaweMaskManager.MaskType type, boolean notify) {
      return this.isValid(player, type);
   }

   @Override
   public Region clone() {
      throw new UnsupportedOperationException("Clone not supported");
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.REMOVING_BLOCKS;
   }
}
