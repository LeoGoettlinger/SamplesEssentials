package com.sk89q.worldedit.session;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

public record Placement(PlacementType placementType, BlockVector3 offset) {
   public BlockVector3 getPlacementPosition(RegionSelector selector, Actor actor) throws IncompleteRegionException {
      return this.placementType.getPlacementPosition(selector, actor).add(this.offset);
   }

   public boolean canBeUsedBy(Actor actor) {
      return this.placementType.canBeUsedBy(actor);
   }

   public Component getInfo() {
      return this.offset.equals(BlockVector3.ZERO)
         ? TranslatableComponent.of(this.placementType.getTranslationKey())
         : TranslatableComponent.of(
            this.placementType.getTranslationKeyWithOffset(),
            TextComponent.of(this.offset.getX()),
            TextComponent.of(this.offset.getY()),
            TextComponent.of(this.offset.getZ())
         );
   }
}
