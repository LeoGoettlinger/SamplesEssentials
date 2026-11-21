package com.sk89q.worldedit.regions.selector;

import com.fastasyncworldedit.core.regions.selector.FuzzyRegionSelector;
import com.fastasyncworldedit.core.regions.selector.PolyhedralRegionSelector;
import com.sk89q.worldedit.regions.RegionSelector;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public enum RegionSelectorType {
   CUBOID(CuboidRegionSelector.class),
   EXTENDING_CUBOID(ExtendingCuboidRegionSelector.class),
   CYLINDER(CylinderRegionSelector.class),
   SPHERE(SphereRegionSelector.class),
   ELLIPSOID(EllipsoidRegionSelector.class),
   POLYGON(Polygonal2DRegionSelector.class),
   CONVEX_POLYHEDRON(ConvexPolyhedralRegionSelector.class),
   POLYHEDRAL(PolyhedralRegionSelector.class),
   FUZZY(FuzzyRegionSelector.class);

   private static final Map<Class<? extends RegionSelector>, RegionSelectorType> VALUE_MAP = new HashMap<>();
   private final Class<? extends RegionSelector> selectorClass;

   private RegionSelectorType(Class<? extends RegionSelector> selectorClass) {
      this.selectorClass = selectorClass;
   }

   @Nullable
   public static RegionSelectorType getForSelector(RegionSelector selector) {
      return VALUE_MAP.get(selector.getClass());
   }

   public Class<? extends RegionSelector> getSelectorClass() {
      return this.selectorClass;
   }

   public RegionSelector createSelector() {
      try {
         return this.getSelectorClass().newInstance();
      } catch (IllegalAccessException | InstantiationException var2) {
         throw new RuntimeException("Could not create selector", var2);
      }
   }

   static {
      for (RegionSelectorType type : values()) {
         VALUE_MAP.put(type.getSelectorClass(), type);
      }
   }
}
