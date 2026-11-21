package com.sk89q.worldedit.world.registry;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.io.ResourceLoader;
import java.io.IOException;
import java.net.URL;

public class BundledRegistries implements Registries {
   private static final BundledRegistries INSTANCE = new BundledRegistries();
   private static final RangeMap<Integer, String> VERSION_MAP;
   private final BundledBlockRegistry blockRegistry = new BundledBlockRegistry();
   private final BundledItemRegistry itemRegistry = new BundledItemRegistry();
   private final NullEntityRegistry entityRegistry = new NullEntityRegistry();
   private final NullBiomeRegistry biomeRegistry = new NullBiomeRegistry();
   private final NullBlockCategoryRegistry blockCategoryRegistry = new NullBlockCategoryRegistry();
   private final NullItemCategoryRegistry itemCategoryRegistry = new NullItemCategoryRegistry();

   static URL loadRegistry(String name) throws IOException {
      ResourceLoader resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader();
      int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion();
      String version = (String)VERSION_MAP.get(dataVersion);
      URL url = resourceLoader.getResource(BundledRegistries.class, name + "." + version + ".json");
      if (url == null) {
         url = resourceLoader.getResource(BundledRegistries.class, name + ".json");
      }

      if (url == null) {
         throw new IOException("Could not find " + name + ".json");
      } else {
         return url;
      }
   }

   protected BundledRegistries() {
   }

   @Override
   public BlockRegistry getBlockRegistry() {
      return this.blockRegistry;
   }

   @Override
   public ItemRegistry getItemRegistry() {
      return this.itemRegistry;
   }

   @Override
   public EntityRegistry getEntityRegistry() {
      return this.entityRegistry;
   }

   @Override
   public BiomeRegistry getBiomeRegistry() {
      return this.biomeRegistry;
   }

   @Override
   public BlockCategoryRegistry getBlockCategoryRegistry() {
      return this.blockCategoryRegistry;
   }

   @Override
   public ItemCategoryRegistry getItemCategoryRegistry() {
      return this.itemCategoryRegistry;
   }

   public static BundledRegistries getInstance() {
      return INSTANCE;
   }

   static {
      TreeRangeMap<Integer, String> versionMap = TreeRangeMap.create();
      versionMap.put(Range.atLeast(3463), "120");
      versionMap.put(Range.atLeast(3953), "121");
      versionMap.put(Range.atLeast(4080), "1212");
      versionMap.put(Range.atLeast(4082), "1213");
      versionMap.put(Range.atLeast(4189), "1214");
      VERSION_MAP = ImmutableRangeMap.copyOf(versionMap);
   }
}
