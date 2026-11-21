package net.thenextlvl.protect;

import com.fastasyncworldedit.core.util.WEManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.reflect.TypeToken;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import dev.faststats.bukkit.BukkitMetrics;
import dev.faststats.core.ErrorTracker;
import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.i18n.ComponentBundle;
import net.thenextlvl.nbt.serialization.NBT;
import net.thenextlvl.protect.adapters.FlagsAdapter;
import net.thenextlvl.protect.adapters.KeyAdapter;
import net.thenextlvl.protect.adapters.LocationAdapter;
import net.thenextlvl.protect.adapters.MembersAdapter;
import net.thenextlvl.protect.adapters.WeatherTypeAdapter;
import net.thenextlvl.protect.adapters.WorldAdapter;
import net.thenextlvl.protect.adapters.area.CuboidAreaAdapter;
import net.thenextlvl.protect.adapters.area.CylinderAreaAdapter;
import net.thenextlvl.protect.adapters.area.EllipsoidAreaAdapter;
import net.thenextlvl.protect.adapters.area.GlobalAreaAdapter;
import net.thenextlvl.protect.adapters.area.GroupedAreaAdapter;
import net.thenextlvl.protect.adapters.region.CuboidRegionAdapter;
import net.thenextlvl.protect.adapters.region.CylinderRegionAdapter;
import net.thenextlvl.protect.adapters.region.EllipsoidRegionAdapter;
import net.thenextlvl.protect.adapters.region.GroupedRegionAdapter;
import net.thenextlvl.protect.adapters.vector.BlockVectorAdapter;
import net.thenextlvl.protect.adapters.vector.Vector2Adapter;
import net.thenextlvl.protect.adapters.vector.Vector3Adapter;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.AreaProvider;
import net.thenextlvl.protect.area.AreaService;
import net.thenextlvl.protect.area.CraftAreaProvider;
import net.thenextlvl.protect.area.CraftAreaService;
import net.thenextlvl.protect.area.CraftCuboidArea;
import net.thenextlvl.protect.area.CraftCylinderArea;
import net.thenextlvl.protect.area.CraftEllipsoidArea;
import net.thenextlvl.protect.area.CraftGlobalArea;
import net.thenextlvl.protect.area.CraftGroupedArea;
import net.thenextlvl.protect.bstats.bukkit.Metrics;
import net.thenextlvl.protect.commands.AreaCommand;
import net.thenextlvl.protect.controllers.CollisionController;
import net.thenextlvl.protect.flag.CraftFlagRegistry;
import net.thenextlvl.protect.flag.Flag;
import net.thenextlvl.protect.flag.FlagRegistry;
import net.thenextlvl.protect.flag.ProtectionFlag;
import net.thenextlvl.protect.listeners.AreaListener;
import net.thenextlvl.protect.listeners.ConnectionListener;
import net.thenextlvl.protect.listeners.EntityListener;
import net.thenextlvl.protect.listeners.MovementListener;
import net.thenextlvl.protect.listeners.NexoListener;
import net.thenextlvl.protect.listeners.PhysicsListener;
import net.thenextlvl.protect.listeners.WorldListener;
import net.thenextlvl.protect.masks.ProtectMaskManager;
import net.thenextlvl.protect.region.GroupedRegion;
import net.thenextlvl.protect.service.CraftProtectionService;
import net.thenextlvl.protect.service.ProtectionService;
import net.thenextlvl.protect.utils.MessageMigrator;
import net.thenextlvl.protect.version.PluginVersionChecker;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ProtectPlugin extends JavaPlugin {
   private int languageFiles;
   public static final String ISSUES = "https://github.com/TheNextLvl-net/protect/issues/new?template=bug_report.yml";
   public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();
   private final Metrics metrics = new Metrics(this, 21712);
   private final dev.faststats.core.Metrics fastStats = BukkitMetrics.factory()
      .token("702e90699ba58f71536c54256557454e")
      .errorTracker(ERROR_TRACKER)
      .create((Plugin)this);
   private final Path schematicFolder = this.getDataPath().resolve("schematics");
   private final PluginVersionChecker versionChecker = new PluginVersionChecker(this);
   private final CollisionController collisionController = new CollisionController();
   private final CraftProtectionService protectionService = new CraftProtectionService(this);
   private final CraftFlagRegistry flagRegistry = new CraftFlagRegistry();
   private final CraftAreaProvider areaProvider = new CraftAreaProvider(this);
   private final CraftAreaService areaService = new CraftAreaService(this);
   public final ProtectPlugin.Flags flags = new ProtectPlugin.Flags();
   private final Key key = Key.key("protect", "translations");
   private final Path translations = this.getDataPath().resolve("translations");
   private final ComponentBundle bundle = ComponentBundle.builder(this.key, this.translations)
      .migrator(new MessageMigrator())
      .placeholder("failed_prefix", "prefix.failed")
      .placeholder("prefix", "prefix")
      .resource("protect.properties", Locale.US)
      .resource("protect_german.properties", Locale.GERMANY)
      .build();
   private static final Set<String> oldVersions = Set.of("1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11");
   private final Cache<Audience, String> cooldown = CacheBuilder.newBuilder().expireAfterAccess(5L, TimeUnit.SECONDS).build();
   public final NBT nbt = NBT.builder()
      .registerTypeAdapter((new TypeToken<Map<Flag<?>, Object>>() {}).getType(), new FlagsAdapter(this))
      .registerTypeAdapter((new TypeToken<Set<UUID>>() {}).getType(), new MembersAdapter())
      .registerTypeHierarchyAdapter(Location.class, new LocationAdapter())
      .registerTypeHierarchyAdapter(Key.class, new KeyAdapter())
      .registerTypeHierarchyAdapter(WeatherType.class, new WeatherTypeAdapter())
      .registerTypeHierarchyAdapter(World.class, new WorldAdapter(this.getServer()))
      .registerTypeHierarchyAdapter(CuboidRegion.class, new CuboidRegionAdapter())
      .registerTypeHierarchyAdapter(CylinderRegion.class, new CylinderRegionAdapter())
      .registerTypeHierarchyAdapter(EllipsoidRegion.class, new EllipsoidRegionAdapter())
      .registerTypeHierarchyAdapter(GroupedRegion.class, new GroupedRegionAdapter())
      .registerTypeHierarchyAdapter(BlockVector3.class, new BlockVectorAdapter())
      .registerTypeHierarchyAdapter(Vector2.class, new Vector2Adapter())
      .registerTypeHierarchyAdapter(Vector3.class, new Vector3Adapter())
      .build();

   public void onLoad() {
      WEManager.weManager().addManager(new ProtectMaskManager(this));
      this.versionChecker.checkVersion();
      this.registerAdapters();
      this.registerServices();
      this.registerWrappers();
   }

   public void onEnable() {
      this.languageFiles = this.loadLanguageFiles();
      this.fastStats.ready();
      this.registerEvents();
      this.registerCommands();
   }

   public void onDisable() {
      this.getServer().getOnlinePlayers().forEach(this.collisionController::remove);
      this.getServer().getWorlds().forEach(world -> {
         this.areaProvider().save(world);
         this.areaProvider().unload(world);
      });
      this.fastStats.shutdown();
      this.metrics.shutdown();
   }

   private void registerServices() {
      this.getServer().getServicesManager().register(ProtectionService.class, this.protectionService(), this, ServicePriority.Highest);
      this.getServer().getServicesManager().register(FlagRegistry.class, this.flagRegistry(), this, ServicePriority.Highest);
      this.getServer().getServicesManager().register(AreaProvider.class, this.areaProvider(), this, ServicePriority.Highest);
      this.getServer().getServicesManager().register(AreaService.class, this.areaService(), this, ServicePriority.Highest);
   }

   private void registerAdapters() {
      this.areaService().registerAdapter(CraftCuboidArea.class, new CuboidAreaAdapter(this));
      this.areaService().registerAdapter(CraftCylinderArea.class, new CylinderAreaAdapter(this));
      this.areaService().registerAdapter(CraftEllipsoidArea.class, new EllipsoidAreaAdapter(this));
      this.areaService().registerAdapter(CraftGlobalArea.class, new GlobalAreaAdapter(this));
      this.areaService().registerAdapter(CraftGroupedArea.class, new GroupedAreaAdapter(this));
   }

   private void registerWrappers() {
      this.areaService().registerWrapper(CuboidRegion.class, creator -> new CraftCuboidArea(this, creator));
      this.areaService().registerWrapper(CylinderRegion.class, creator -> new CraftCylinderArea(this, creator));
      this.areaService().registerWrapper(EllipsoidRegion.class, creator -> new CraftEllipsoidArea(this, creator));
      this.areaService().registerWrapper(GroupedRegion.class, creator -> new CraftGroupedArea(this, creator));
   }

   private void registerEvents() {
      this.getServer().getPluginManager().registerEvents(new AreaListener(this), this);
      this.getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
      this.getServer().getPluginManager().registerEvents(new EntityListener(this), this);
      this.getServer().getPluginManager().registerEvents(new MovementListener(this), this);
      this.getServer().getPluginManager().registerEvents(new PhysicsListener(this), this);
      this.getServer().getPluginManager().registerEvents(new WorldListener(this), this);
      Plugin nexo = this.getServer().getPluginManager().getPlugin("Nexo");
      if (nexo != null) {
         this.registerNexoEvents(nexo);
      }
   }

   private void registerNexoEvents(Plugin nexo) {
      this.getServer().getPluginManager().registerEvents(new NexoListener(this, nexo), this);
   }

   private void registerCommands() {
      this.getLifecycleManager()
         .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> ((Commands)event.registrar()).register(AreaCommand.create(this))));
   }

   public static Path getDataFolder(World world) {
      String version = ServerBuildInfo.buildInfo().minecraftVersionId();
      Path path = world.getWorldFolder().toPath();
      return oldVersions.contains(version) ? path.resolve("areas") : path.resolve("data").resolve("areas");
   }

   public void failed(@Nullable Audience audience, Area area, String message) {
      if (audience != null && area.getFlag(this.flags.notifyFailedInteractions)) {
         if (!message.equals(this.cooldown.getIfPresent(audience))) {
            this.bundle().sendMessage(audience, message, Placeholder.parsed("area", area.getName()));
            this.cooldown.put(audience, message);
         }
      }
   }

   public void failed(@Nullable Audience audience, Cancellable cancellable, Area area, String message) {
      if (cancellable.isCancelled()) {
         this.failed(audience, area, message);
      }
   }

   public Path schematicFolder() {
      return this.schematicFolder;
   }

   public CollisionController collisionController() {
      return this.collisionController;
   }

   public CraftProtectionService protectionService() {
      return this.protectionService;
   }

   public CraftFlagRegistry flagRegistry() {
      return this.flagRegistry;
   }

   public CraftAreaProvider areaProvider() {
      return this.areaProvider;
   }

   public CraftAreaService areaService() {
      return this.areaService;
   }

   public ComponentBundle bundle() {
      return this.bundle;
   }

   public class Flags {
      public final Flag<Long> time = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, Long.class, "time", null);
      public final Flag<String> farewell = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "farewell", null);
      public final Flag<String> farewellActionbar = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "farewell_actionbar", null);
      public final Flag<String> farewellTitle = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "farewell_title", null);
      public final Flag<String> greetings = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "greetings", null);
      public final Flag<String> greetingsActionbar = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "greetings_actionbar", null);
      public final Flag<String> greetingsTitle = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, String.class, "greetings_title", null);
      public final Flag<WeatherType> weather = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, WeatherType.class, "weather", null);
      public final Flag<Boolean> areaEnter = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "enter", true);
      public final Flag<Boolean> areaLeave = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "leave", true);
      public final Flag<Boolean> damage = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "damage", true);
      public final Flag<Boolean> entityItemDrop = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "entity_item_drop", true);
      public final Flag<Boolean> entityItemPickup = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "entity_item_pickup", true);
      public final Flag<Boolean> gameEvents = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "game_events", true);
      public final Flag<Boolean> gravity = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "gravity", true);
      public final Flag<Boolean> hunger = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "hunger", true);
      public final Flag<Boolean> liquidFlow = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "liquid_flow", true);
      public final Flag<Boolean> naturalEntitySpawn = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "natural_entity_spawn", true);
      public final Flag<Boolean> notifyFailedInteractions = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "notify_failed_interactions", false);
      public final Flag<Boolean> physics = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "physics", true);
      public final Flag<Boolean> redstone = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "redstone", true);
      public final Flag<Boolean> shoot = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "shoot", true);
      public final ProtectionFlag<Boolean> armorStandManipulate = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "armor_stand_manipulate", true, false);
      public final ProtectionFlag<Boolean> blockAbsorb = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_absorb", true, false);
      public final ProtectionFlag<Boolean> blockBurning = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_burning", true, false);
      public final ProtectionFlag<Boolean> blockDrying = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_drying", true, false);
      public final ProtectionFlag<Boolean> blockFading = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_fading", true, false);
      public final ProtectionFlag<Boolean> blockFertilize = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_fertilize", true, false);
      public final ProtectionFlag<Boolean> blockForming = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_forming", true, false);
      public final ProtectionFlag<Boolean> blockGrowth = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_growth", true, false);
      public final ProtectionFlag<Boolean> blockIgniting = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_igniting", true, false);
      public final ProtectionFlag<Boolean> blockMoisturising = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "block_moisturising", true, false);
      public final ProtectionFlag<Boolean> blockSpread = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "block_spread", true, false);
      public final ProtectionFlag<Boolean> cauldronEvaporation = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "cauldron_evaporation", true, false);
      public final ProtectionFlag<Boolean> cauldronExtinguishEntity = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "cauldron_extinguish_entity", true, false);
      public final ProtectionFlag<Boolean> collisions = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "collisions", true, false);
      public final ProtectionFlag<Boolean> cropTrample = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "crop_trample", true, false);
      public final ProtectionFlag<Boolean> destroy = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "destroy", true, false);
      public final ProtectionFlag<Boolean> entityAttackEntity = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "entity_attack_entity", true, false);
      public final ProtectionFlag<Boolean> entityAttackPlayer = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "entity_attack_player", true, false);
      public final ProtectionFlag<Boolean> entityBreakDoor = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "entity_break_door", true, false);
      public final ProtectionFlag<Boolean> entityInteract = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "entity_interact", true, false);
      public final ProtectionFlag<Boolean> entityShear = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "entity_shear", true, false);
      public final ProtectionFlag<Boolean> explosions = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "explosions", true, false);
      public final ProtectionFlag<Boolean> interact = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "interact", true, false);
      public final ProtectionFlag<Boolean> knockback = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "knockback", true, false);
      public final ProtectionFlag<Boolean> leavesDecay = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "leaves_decay", true, false);
      public final ProtectionFlag<Boolean> naturalCauldronFill = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "natural_cauldron_fill", true, false);
      public final ProtectionFlag<Boolean> physicalInteract = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "physical_interact", true, false);
      public final ProtectionFlag<Boolean> place = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "place", true, false);
      public final ProtectionFlag<Boolean> playerAttackEntity = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "player_attack_entity", true, false);
      public final ProtectionFlag<Boolean> playerAttackPlayer = ProtectPlugin.this.flagRegistry()
         .register(ProtectPlugin.this, "player_attack_player", true, false);
      public final ProtectionFlag<Boolean> playerItemDrop = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "player_item_drop", true, false);
      public final ProtectionFlag<Boolean> sheepEatGrass = ProtectPlugin.this.flagRegistry().register(ProtectPlugin.this, "sheep_eat_grass", true, false);
   }
}
