package com.sk89q.worldedit.bukkit;

import com.fastasyncworldedit.bukkit.util.MinecraftVersion;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.processor.PlacementStateProcessor;
import com.fastasyncworldedit.core.extent.processor.lighting.RelighterFactory;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.google.common.collect.Sets;
import com.sk89q.bukkit.util.CommandInfo;
import com.sk89q.bukkit.util.CommandRegistration;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlatform;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.MultiUserPlatform;
import com.sk89q.worldedit.extension.platform.Preference;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.registry.Registries;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.enginehub.piston.CommandManager;

public class BukkitServerInterface extends AbstractPlatform implements MultiUserPlatform {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public final Server server;
   public final WorldEditPlugin plugin;
   private final CommandRegistration dynamicCommands;
   private final Lifecycled<Watchdog> watchdog;
   private RelighterFactory relighterFactory;
   private boolean hookingEvents;
   private static final Set<SideEffect> SUPPORTED_SIDE_EFFECTS = Sets.immutableEnumSet(SideEffect.NEIGHBORS, new SideEffect[0]);

   public BukkitServerInterface(WorldEditPlugin plugin, Server server) {
      this.plugin = plugin;
      this.server = server;
      this.dynamicCommands = new CommandRegistration(plugin);
      this.watchdog = plugin.getLifecycledBukkitImplAdapter().filter(BukkitImplAdapter::supportsWatchdog).map(BukkitWatchdog::new);
   }

   CommandRegistration getDynamicCommands() {
      return this.dynamicCommands;
   }

   boolean isHookingEvents() {
      return this.hookingEvents;
   }

   @Override
   public Registries getRegistries() {
      return BukkitRegistries.getInstance();
   }

   @Override
   public int getDataVersion() {
      return this.plugin.getLifecycledBukkitImplAdapter().isValid() ? Bukkit.getUnsafe().getDataVersion() : -1;
   }

   @Override
   public DataFixer getDataFixer() {
      return this.plugin.getBukkitImplAdapter() != null ? this.plugin.getBukkitImplAdapter().getDataFixer() : null;
   }

   @Override
   public boolean isValidMobType(String type) {
      if (!type.startsWith("minecraft:")) {
         return false;
      } else {
         EntityType entityType = EntityType.fromName(type.substring(10));
         return entityType != null && entityType.isAlive();
      }
   }

   @Override
   public void reload() {
      this.plugin.loadConfiguration();
   }

   @Override
   public int schedule(long delay, long period, Runnable task) {
      return Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, task, delay, period);
   }

   @Override
   public Watchdog getWatchdog() {
      return this.watchdog.value().orElse(null);
   }

   @Override
   public List<com.sk89q.worldedit.world.World> getWorlds() {
      List<World> worlds = this.server.getWorlds();
      List<com.sk89q.worldedit.world.World> ret = new ArrayList<>(worlds.size());

      for (World world : worlds) {
         ret.add(BukkitAdapter.adapt(world));
      }

      return ret;
   }

   @Nullable
   @Override
   public Player matchPlayer(Player player) {
      if (player instanceof BukkitPlayer) {
         return player;
      } else {
         org.bukkit.entity.Player bukkitPlayer = this.server.getPlayerExact(player.getName());
         return bukkitPlayer != null ? WorldEditPlugin.getInstance().wrapPlayer(bukkitPlayer) : null;
      }
   }

   @Nullable
   public BukkitWorld matchWorld(com.sk89q.worldedit.world.World world) {
      if (world instanceof BukkitWorld) {
         return (BukkitWorld)world;
      } else {
         World bukkitWorld = this.server.getWorld(world.getName());
         return bukkitWorld != null ? new BukkitWorld(bukkitWorld) : null;
      }
   }

   @Override
   public void registerCommands(CommandManager dispatcher) {
      BukkitCommandInspector inspector = new BukkitCommandInspector(this.plugin, dispatcher);
      this.dynamicCommands
         .register(
            dispatcher.getAllCommands()
               .map(
                  command -> {
                     String[] permissionsArray = command.getCondition()
                        .as(PermissionCondition.class)
                        .map(PermissionCondition::getPermissions)
                        .map(s -> s.toArray(new String[0]))
                        .orElseGet(() -> new String[0]);
                     String[] aliases = Stream.concat(Stream.of(command.getName()), command.getAliases().stream()).toArray(String[]::new);
                     return new CommandInfo(
                        WorldEditText.reduceToText(command.getUsage(), WorldEdit.getInstance().getConfiguration().defaultLocale),
                        WorldEditText.reduceToText(command.getDescription(), WorldEdit.getInstance().getConfiguration().defaultLocale),
                        aliases,
                        inspector,
                        permissionsArray
                     );
                  }
               )
               .collect(Collectors.toList())
         );
   }

   @Override
   public void setGameHooksEnabled(boolean enabled) {
      this.hookingEvents = enabled;
   }

   @Override
   public LocalConfiguration getConfiguration() {
      return this.plugin.getLocalConfiguration();
   }

   @Override
   public String getVersion() {
      return this.plugin.getDescription().getVersion();
   }

   @Override
   public String getPlatformName() {
      return "Bukkit-Official";
   }

   @Override
   public String getPlatformVersion() {
      return this.plugin.getDescription().getVersion();
   }

   @Override
   public String id() {
      return "intellectualsites:bukkit";
   }

   @Override
   public Map<Capability, Preference> getCapabilities() {
      Map<Capability, Preference> capabilities = new EnumMap<>(Capability.class);
      capabilities.put(Capability.CONFIGURATION, Preference.NORMAL);
      capabilities.put(Capability.WORLDEDIT_CUI, Preference.NORMAL);
      capabilities.put(Capability.GAME_HOOKS, Preference.PREFERRED);
      capabilities.put(Capability.PERMISSIONS, Preference.PREFERRED);
      capabilities.put(Capability.USER_COMMANDS, Preference.PREFERRED);
      capabilities.put(Capability.WORLD_EDITING, Preference.PREFER_OTHERS);
      return capabilities;
   }

   @Override
   public Set<SideEffect> getSupportedSideEffects() {
      return this.plugin.getBukkitImplAdapter() != null ? this.plugin.getBukkitImplAdapter().getSupportedSideEffects() : SUPPORTED_SIDE_EFFECTS;
   }

   @Override
   public long getTickCount() {
      return PaperLib.isPaper() ? Bukkit.getCurrentTick() : super.getTickCount();
   }

   public void unregisterCommands() {
      this.dynamicCommands.unregisterCommands();
   }

   @Override
   public Collection<Actor> getConnectedUsers() {
      List<Actor> users = new ArrayList<>();

      for (org.bukkit.entity.Player player : Bukkit.getServer().getOnlinePlayers()) {
         users.add(WorldEditPlugin.getInstance().wrapPlayer(player));
      }

      return users;
   }

   @Nonnull
   @Override
   public RelighterFactory getRelighterFactory() {
      if (this.relighterFactory == null) {
         this.relighterFactory = this.plugin.getBukkitImplAdapter().getRelighterFactory();
         LOGGER.info("Using {} as relighter factory.", this.relighterFactory.getClass().getCanonicalName());
      }

      return this.relighterFactory;
   }

   @Override
   public int versionMinY() {
      return MinecraftVersion.getCurrent().isEqualOrHigherThan(MinecraftVersion.CAVES_18) ? -64 : 0;
   }

   @Override
   public int versionMaxY() {
      return MinecraftVersion.getCurrent().isEqualOrHigherThan(MinecraftVersion.CAVES_18) ? 319 : 255;
   }

   @Override
   public IBatchProcessor getPlatformPostProcessor(boolean fastMode) {
      boolean tickFluid = Settings.settings().EXPERIMENTAL.ALLOW_TICK_FLUIDS;
      return !tickFluid ? null : this.plugin.getBukkitImplAdapter().getTickingPostProcessor();
   }

   @Override
   public PlacementStateProcessor getPlatformPlacementProcessor(Extent extent, BlockTypeMask mask, Region region) {
      return this.plugin.getBukkitImplAdapter().getPlatformPlacementProcessor(extent, mask, region);
   }
}
