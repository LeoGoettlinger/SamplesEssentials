package com.sk89q.worldedit.regions;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public interface RegionSelector {
   @Nullable
   World getWorld();

   void setWorld(@Nullable World world);

   boolean selectPrimary(BlockVector3 position, SelectorLimits limits);

   boolean selectSecondary(BlockVector3 position, SelectorLimits limits);

   void explainPrimarySelection(Actor actor, LocalSession session, BlockVector3 position);

   void explainSecondarySelection(Actor actor, LocalSession session, BlockVector3 position);

   void explainRegionAdjust(Actor actor, LocalSession session);

   BlockVector3 getPrimaryPosition() throws IncompleteRegionException;

   Region getRegion() throws IncompleteRegionException;

   Region getIncompleteRegion();

   boolean isDefined();

   @Deprecated
   default int getArea() {
      return (int)this.getVolume();
   }

   @NonAbstractForCompatibility(
      delegateName = "getArea",
      delegateParams = {}
   )
   default long getVolume() {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getArea();
   }

   void learnChanges();

   void clear();

   String getTypeName();

   @Deprecated
   default List<String> getInformationLines() {
      return Lists.newArrayList();
   }

   default List<Component> getSelectionInfoLines() {
      return this.getInformationLines().stream().map(line -> TextComponent.of(line, TextColor.LIGHT_PURPLE)).collect(Collectors.toList());
   }

   default List<BlockVector3> getVertices() throws IncompleteRegionException {
      return Collections.singletonList(this.getPrimaryPosition());
   }
}
