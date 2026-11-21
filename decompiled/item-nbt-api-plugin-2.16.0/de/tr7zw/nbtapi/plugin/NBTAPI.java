package de.tr7zw.nbtapi.plugin;

import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.GameprofileTest;
import de.tr7zw.nbtapi.plugin.tests.NBTFileTest;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.plugin.tests.blocks.BlockNBTTest;
import de.tr7zw.nbtapi.plugin.tests.chunks.ChunkNBTPersistentTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.CompoundDifferenceTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.EnumTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.EqualsTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.ForEachTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.GetterSetterTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.InterfaceTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.IteratorTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.ListTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.LongArrayTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.MergeTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.ModernSubCompoundsTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.RemovingKeys;
import de.tr7zw.nbtapi.plugin.tests.compounds.ResolveTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.StreamTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.SubCompoundsTest;
import de.tr7zw.nbtapi.plugin.tests.compounds.TypeTest;
import de.tr7zw.nbtapi.plugin.tests.entities.EntityCustomNbtPersistentTest;
import de.tr7zw.nbtapi.plugin.tests.entities.EntityTest;
import de.tr7zw.nbtapi.plugin.tests.items.ComponentsTest;
import de.tr7zw.nbtapi.plugin.tests.items.DirectApplyMetaTest;
import de.tr7zw.nbtapi.plugin.tests.items.DirectApplyTest;
import de.tr7zw.nbtapi.plugin.tests.items.EmptyItemTest;
import de.tr7zw.nbtapi.plugin.tests.items.ItemConversionTest;
import de.tr7zw.nbtapi.plugin.tests.items.ItemJsonTest;
import de.tr7zw.nbtapi.plugin.tests.items.ItemMergingTest;
import de.tr7zw.nbtapi.plugin.tests.items.ItemStackConversionTest;
import de.tr7zw.nbtapi.plugin.tests.items.LegacyItemTest;
import de.tr7zw.nbtapi.plugin.tests.items.MetaTest;
import de.tr7zw.nbtapi.plugin.tests.items.NBTModifyItemTest;
import de.tr7zw.nbtapi.plugin.tests.items.SmuggleTest;
import de.tr7zw.nbtapi.plugin.tests.proxy.SimpleProxyTest;
import de.tr7zw.nbtapi.plugin.tests.tiles.TileTest;
import de.tr7zw.nbtapi.plugin.tests.tiles.TilesCustomNBTPersistentTest;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import de.tr7zw.nbtapi.utils.VersionChecker;
import de.tr7zw.nbtapi.utils.nmsmappings.ClassWrapper;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public class NBTAPI extends JavaPlugin {
   private int moduleRegistry;
   private boolean compatible = true;
   private List<Test> apiTests = new ArrayList<>();
   private static NBTAPI instance;

   public static NBTAPI getInstance() {
      return instance;
   }

   public void onLoad() {
      this.getConfig().options().copyDefaults(true);
      this.getConfig().addDefault("bStats.enabled", true);
      this.getConfig().addDefault("updateCheck.enabled", true);
      this.getConfig().addDefault("silentquickstart", false);
      this.saveConfig();
      if (!this.getConfig().getBoolean("bStats.enabled")) {
         this.getLogger().info("bStats disabled");
         MinecraftVersion.disableBStats();
      }

      if (!this.getConfig().getBoolean("updateCheck.enabled")) {
         this.getLogger().info("Update check disabled");
         MinecraftVersion.disableUpdateCheck();
      } else {
         MinecraftVersion.enableUpdateCheck();
      }

      this.apiTests.add(new GetterSetterTest());
      this.apiTests.add(new TypeTest());
      this.apiTests.add(new RemovingKeys());
      this.apiTests.add(new EnumTest());
      this.apiTests.add(new InterfaceTest());
      this.apiTests.add(new ResolveTest());
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new ListTest());
      }

      this.apiTests.add(new SubCompoundsTest());
      this.apiTests.add(new ModernSubCompoundsTest());
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new MergeTest());
      }

      this.apiTests.add(new ForEachTest());
      this.apiTests.add(new StreamTest());
      this.apiTests.add(new EqualsTest());
      this.apiTests.add(new CompoundDifferenceTest());
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new IteratorTest());
      }

      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_16_R1)) {
         this.apiTests.add(new LongArrayTest());
      }

      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new ItemConversionTest());
         this.apiTests.add(new ItemStackConversionTest());
      }

      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4)) {
         this.apiTests.add(new LegacyItemTest());
      }

      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4) && !MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
         this.apiTests.add(new ItemJsonTest());
      }

      this.apiTests.add(new ComponentsTest());
      this.apiTests.add(new EmptyItemTest());
      this.apiTests.add(new SmuggleTest());
      this.apiTests.add(new MetaTest());
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new ItemMergingTest());
         this.apiTests.add(new DirectApplyTest());
         this.apiTests.add(new DirectApplyMetaTest());
         this.apiTests.add(new NBTModifyItemTest());
      }

      this.apiTests.add(new SimpleProxyTest());
      this.apiTests.add(new EntityTest());
      this.apiTests.add(new EntityCustomNbtPersistentTest());
      this.apiTests.add(new TileTest());
      this.apiTests.add(new TilesCustomNBTPersistentTest());
      this.apiTests.add(new ChunkNBTPersistentTest());
      this.apiTests.add(new BlockNBTTest());
      this.apiTests.add(new NBTFileTest());
      if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_8_R3)) {
         this.apiTests.add(new GameprofileTest());
      }
   }

   public void onEnable() {
      this.moduleRegistry = this.prepareModuleRegistry();
      instance = this;
      if (this.getConfig().getBoolean("silentquickstart")) {
         VersionChecker.hideOk = true;
      } else {
         MinecraftVersion.hasGsonSupport();
         this.getLogger().info("Checking bindings...");
         MinecraftVersion.getVersion();
         boolean classUnlinked = false;

         for (ClassWrapper c : ClassWrapper.values()) {
            if (c.isEnabled() && c.getClazz() == null) {
               if (!classUnlinked) {
                  this.getLogger().info("Classes:");
               }

               this.getLogger().warning(c.name() + " did not find it's class!");
               this.compatible = false;
               classUnlinked = true;
            }
         }

         if (!classUnlinked) {
            this.getLogger().info("All Classes were able to link!");
         }

         boolean methodUnlinked = false;

         for (ReflectionMethod method : ReflectionMethod.values()) {
            if (method.isCompatible() && !method.isLoaded()) {
               if (!methodUnlinked) {
                  this.getLogger().info("Methods:");
               }

               this.getLogger().warning(method.name() + " did not find the method!");
               this.compatible = false;
               methodUnlinked = true;
            }
         }

         if (!methodUnlinked) {
            this.getLogger().info("All Methods were able to link!");
         }

         this.getLogger().info("Running NBT reflection test...");
         Map<Test, Exception> results = new HashMap<>();

         for (Test test : this.apiTests) {
            try {
               test.test();
               results.put(test, null);
            } catch (Exception var7) {
               results.put(test, var7);
               this.getLogger().log(Level.WARNING, "Error during '" + test.getClass().getSimpleName() + "' test!", (Throwable)var7);
            } catch (Throwable var8) {
               this.getLogger().log(Level.SEVERE, "Servere error during '" + test.getClass().getSimpleName() + "' test!");
               this.getLogger().warning("WARNING! This version of Item-NBT-API seems to be broken with your Spigot version! Canceled the other tests!");
               throw var8;
            }
         }

         for (Entry<Test, Exception> entry : results.entrySet()) {
            if (entry.getValue() != null) {
               this.compatible = false;
               this.getLogger().info(entry.getKey().getClass().getSimpleName() + ": " + entry.getValue().getMessage());
            }
         }

         String checkMessage = "Plugins that don't check properly may throw Exeptions, crash or have unexpected behaviors!";
         if (this.compatible) {
            NbtApiException.confirmedBroken = false;
            this.getLogger().info("Success! This version of NBT-API is compatible with your server.");
         } else {
            NbtApiException.confirmedBroken = true;
            this.getLogger().warning("WARNING! This version of NBT-API seems to be broken with your Spigot version! " + checkMessage);
            if (MinecraftVersion.getVersion() == MinecraftVersion.MC1_7_R4) {
               this.getLogger().warning("1.7.10 is only partally supported! Some thing will not work/are not yet avaliable in 1.7.10!");
            }
         }
      }
   }

   public boolean isCompatible() {
      return this.compatible;
   }
}
