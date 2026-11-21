package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableSetMultimap;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.MultiKeyConverter;
import org.enginehub.piston.inject.Key;

public class RegionFactoryConverter {
   public static void register(CommandManager commandManager) {
      commandManager.registerConverter(
         Key.of(RegionFactory.class),
         MultiKeyConverter.<RegionFactory>builder(
               ImmutableSetMultimap.builder()
                  .put(new CuboidRegionFactory(), "cuboid")
                  .put(new SphereRegionFactory(), "sphere")
                  .putAll(new CylinderRegionFactory(1.0), new String[]{"cyl", "cylinder"})
                  .build()
            )
            .errorMessage(arg -> "Not a known region type: " + arg)
            .build()
      );
   }

   private RegionFactoryConverter() {
   }
}
