package com.sk89q.worldedit.extension.platform;

import com.fastasyncworldedit.core.extent.processor.PlacementStateProcessor;
import com.fastasyncworldedit.core.extent.processor.lighting.RelighterFactory;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.Registries;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.enginehub.piston.CommandManager;

public interface Platform extends Keyed {
   ResourceLoader getResourceLoader();

   Registries getRegistries();

   int getDataVersion();

   DataFixer getDataFixer();

   boolean isValidMobType(String type);

   void reload();

   int schedule(long delay, long period, Runnable task);

   @Nullable
   default Watchdog getWatchdog() {
      return null;
   }

   List<? extends World> getWorlds();

   @Nullable
   Player matchPlayer(Player player);

   @Nullable
   World matchWorld(World world);

   void registerCommands(CommandManager commandManager);

   @Deprecated
   default void registerGameHooks() {
      this.setGameHooksEnabled(true);
   }

   void setGameHooksEnabled(boolean enabled);

   LocalConfiguration getConfiguration();

   String getVersion();

   String getPlatformName();

   String getPlatformVersion();

   Map<Capability, Preference> getCapabilities();

   Set<SideEffect> getSupportedSideEffects();

   long getTickCount();

   @NonAbstractForCompatibility(
      delegateName = "getPlatformName",
      delegateParams = {}
   )
   @Override
   default String id() {
      return "legacy:" + this.getPlatformName().toLowerCase(Locale.ROOT).replaceAll("[^a-z_.-]", "_");
   }

   @Nonnull
   RelighterFactory getRelighterFactory();

   int versionMinY();

   int versionMaxY();

   @Nullable
   default IBatchProcessor getPlatformProcessor(boolean fastMode) {
      return null;
   }

   @Nullable
   default IBatchProcessor getPlatformPostProcessor(boolean fastMode) {
      return null;
   }

   default PlacementStateProcessor getPlatformPlacementProcessor(Extent extent, BlockTypeMask mask, @Nullable Region region) {
      return null;
   }
}
