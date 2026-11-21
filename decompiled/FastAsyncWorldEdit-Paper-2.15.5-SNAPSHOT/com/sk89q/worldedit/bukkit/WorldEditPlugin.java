package com.sk89q.worldedit.bukkit;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import com.fastasyncworldedit.bukkit.BukkitPermissionAttachmentManager;
import com.fastasyncworldedit.bukkit.FaweBukkit;
import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.util.UpdateNotification;
import com.fastasyncworldedit.core.util.WEManager;
import com.fastasyncworldedit.serverlib.ServerLib;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sk89q.bukkit.util.ClassSourceValidator;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditManifest;
import com.sk89q.worldedit.bstats.bukkit.Metrics;
import com.sk89q.worldedit.bstats.charts.SimplePie;
import com.sk89q.worldedit.bukkit.adapter.AdapterLoadException;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplLoader;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.registry.Registries;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;

public class WorldEditPlugin extends JavaPlugin {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public static final String CUI_PLUGIN_CHANNEL = "worldedit:cui";
   private static WorldEditPlugin INSTANCE;
   private static final int BSTATS_ID = 1403;
   private final SimpleLifecycled<BukkitImplAdapter> adapter = SimpleLifecycled.invalid();
   private BukkitServerInterface platform;
   private BukkitConfiguration config;
   private BukkitPermissionAttachmentManager permissionAttachmentManager;
   private BukkitCommandSender bukkitConsoleCommandSender;

   public void onLoad() {
      Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();

      for (Plugin p : plugins) {
         if (p.getName().equals("WorldEdit")) {
            LOGGER.warn(
               "You installed WorldEdit alongside FastAsyncWorldEdit. That is unneeded and will cause unforeseen issues, because FastAsyncWorldEdit already provides WorldEdit. Stop your server and delete the 'worldedit-bukkit' jar from your plugins folder."
            );
         }
      }

      Attributes attributes = WorldEditManifest.readAttributes();
      Objects.requireNonNull(attributes, "Could not retrieve manifest attributes");
      String type = attributes.getValue("FAWE-Plugin-Jar-Type");
      Objects.requireNonNull(type, "Could not determine plugin jar type");
      if (PaperLib.isPaper()) {
         if (PaperLib.getMinecraftVersion() >= 20 && (PaperLib.getMinecraftVersion() != 20 || PaperLib.getMinecraftPatchVersion() >= 5)) {
            if ((PaperLib.getMinecraftVersion() > 20 || PaperLib.getMinecraftVersion() == 20 && PaperLib.getMinecraftPatchVersion() >= 5)
               && type.equals("spigot")) {
               LOGGER.warn(
                  "\n**********************************************\n** You are using the Spigot-mapped FAWE jar on a modern Paper version.\n** This will result in slower first-run times and wasted disk space from plugin remapping.\n** Download the Paper FAWE jar from Modrinth to avoid this: https://modrinth.com/plugin/fastasyncworldedit/\n**********************************************"
               );
            }
         } else if (type.equals("mojang") && !Refraction.isMojangMapped()) {
            throw new IllegalStateException(
               "\n**********************************************\n** You are using the wrong FAWE jar for your Minecraft version.\n** Download the correct FAWE jar from Modrinth: https://modrinth.com/plugin/fastasyncworldedit/\n**********************************************"
            );
         }
      } else if (type.equals("mojang")) {
         throw new IllegalStateException(
            "\n**********************************************\n** You are attempting to run the Paper FAWE jar on a Spigot server.\n** Either switch to Paper (https://papermc.io), or download the correct FAWE jar for your platform\n** from Modrinth: https://modrinth.com/plugin/fastasyncworldedit/\n**********************************************"
         );
      }

      INSTANCE = this;
      this.getDataFolder().mkdirs();
      this.migrateLegacyConfig();
      this.config = new BukkitConfiguration(new YAMLProcessor(new File(this.getDataFolder(), "worldedit-config.yml"), true), this);
      this.config.load();
      WorldEdit worldEdit = WorldEdit.getInstance();
      this.platform = new BukkitServerInterface(this, this.getServer());
      worldEdit.getPlatformManager().register(this.platform);
      this.permissionAttachmentManager = new BukkitPermissionAttachmentManager(this);
      this.bukkitConsoleCommandSender = new BukkitCommandSender(this, Bukkit.getConsoleSender());
      Path delChunks = Paths.get(this.getDataFolder().getPath(), "delete_chunks.json");
      if (Files.exists(delChunks)) {
         ChunkDeleter.runFromFile(delChunks, true);
      }

      if (this.getDataFolder().getParentFile().listFiles(file -> {
         if (file.getName().equals("DummyFawe.jar")) {
            file.delete();
            return true;
         } else {
            return false;
         }
      }).length > 0) {
         LOGGER.warn("DummyFawe detected and automatically deleted! This file is no longer necessary.");
      }
   }

   public void onEnable() {
      ClassSourceValidator verifier = new ClassSourceValidator(this);
      verifier.reportMismatches(ImmutableList.of(World.class, CommandManager.class, EditSession.class, Actor.class));
      new FaweBukkit(this);
      WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());
      PermissionsResolverManager.initialize(this);
      this.getServer().getMessenger().registerIncomingPluginChannel(this, "worldedit:cui", new CUIChannelListener(this));
      this.getServer().getMessenger().registerOutgoingPluginChannel(this, "worldedit:cui");
      this.getServer().getPluginManager().registerEvents(new WorldEditListener(this), this);
      if (PaperLib.isPaper()) {
         this.getServer().getPluginManager().registerEvents(new WorldEditPlugin.AsyncTabCompleteListener(), this);
      }

      if (Bukkit.getWorlds().isEmpty()) {
         this.setupPreWorldData();
         this.getServer().getPluginManager().registerEvents(new WorldEditPlugin.WorldInitListener(), this);
      } else {
         LOGGER.warn(
            "Server reload detected. This may cause various issues with FastAsyncWorldEdit and dependent plugins. Reloading the server is not advised."
         );
         LOGGER.warn("For more information why reloading is bad, see https://madelinemiller.dev/blog/problem-with-reload/");

         try {
            this.setupPreWorldData();
            this.setupWorldData();
         } catch (Throwable var3) {
         }
      }

      Metrics m = new Metrics(this, 1403);
      m.addCustomChart(new SimplePie("residence", () -> WEManager.weManager().getManagers().toString().contains("residence") ? "Yes" : "No"));
      ServerLib.checkUnsafeForks();
      UpdateNotification.doUpdateCheck();
   }

   private void setupPreWorldData() {
      this.loadAdapter();
      this.initializeRegistries();
      WorldEdit.getInstance().loadMappings();
   }

   private void setupWorldData() {
      this.setupTags();
      this.setupBiomes(false);
      this.adapter.value().get().setupFeatures();
      WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(this.platform));
   }

   private void initializeRegistries() {
      this.setupBiomes(true);

      for (EntityType entityType : EntityType.values()) {
         String mcid = entityType.getName();
         if (mcid != null) {
            String lowerCaseMcId = mcid.toLowerCase(Locale.ROOT);
            com.sk89q.worldedit.world.entity.EntityType.REGISTRY
               .register("minecraft:" + lowerCaseMcId, new com.sk89q.worldedit.world.entity.EntityType("minecraft:" + lowerCaseMcId));
         }
      }

      BukkitImplAdapter adapter = this.getBukkitImplAdapter();
      if (adapter != null) {
         adapter.initializeRegistries();
      }

      GameModes.get("");
      WeatherTypes.get("");
      Registries.get("");
   }

   private void setupTags() {
      try {
         for (Tag<Material> blockTag : Bukkit.getTags("blocks", Material.class)) {
            String key = blockTag.getKey().toString();
            BlockCategory.REGISTRY.register(key, new BlockCategory(blockTag.getKey().toString()));
         }

         for (Tag<Material> itemTag : Bukkit.getTags("items", Material.class)) {
            String key = itemTag.getKey().toString();
            ItemCategory.REGISTRY.register(key, new ItemCategory(itemTag.getKey().toString()));
         }
      } catch (NoSuchMethodError var4) {
         LOGGER.warn("The version of Spigot/Paper you are using doesn't support Tags. The usage of tags with WorldEdit will not work until you update.");
      }
   }

   private void setupBiomes(boolean expectFail) {
      if (this.adapter.value().isPresent()) {
         for (NamespacedKey biome : this.adapter.value().get().getRegisteredBiomes()) {
            BiomeType biomeType;
            if ((biomeType = BiomeType.REGISTRY.get(biome.toString())) == null) {
               biomeType = new BiomeType(biome.toString());
               BiomeType.REGISTRY.register(biome.toString(), biomeType);
            }

            biomeType.setLegacyId(this.adapter.value().get().getInternalBiomeId(biomeType));
         }
      } else {
         if (!expectFail) {
            LOGGER.warn("Failed to load biomes via adapter (not present). Will load via bukkit");
         }

         for (Biome biome : Biome.values()) {
            if (!biome.name().equals("CUSTOM")) {
               String lowerCaseBiome = biome.getKey().toString().toLowerCase(Locale.ROOT);
               if (BiomeType.REGISTRY.get(lowerCaseBiome) == null) {
                  BiomeType.REGISTRY.register(lowerCaseBiome, new BiomeType(lowerCaseBiome));
               }
            }
         }
      }
   }

   private void loadAdapter() {
      WorldEdit worldEdit = WorldEdit.getInstance();
      BukkitImplLoader adapterLoader = new BukkitImplLoader();

      try {
         adapterLoader.addFromPath(this.getClass().getClassLoader());
      } catch (IOException var6) {
         LOGGER.warn("Failed to search path for Bukkit adapters");
      }

      try {
         adapterLoader.addFromJar(this.getFile());
      } catch (IOException var5) {
         LOGGER.warn("Failed to search " + this.getFile() + " for Bukkit adapters", var5);
      }

      try {
         BukkitImplAdapter bukkitAdapter = adapterLoader.loadAdapter();
         LOGGER.info("Using " + bukkitAdapter.getClass().getCanonicalName() + " as the Bukkit adapter");
         this.adapter.newValue(bukkitAdapter);
      } catch (AdapterLoadException var7) {
         Platform platform = worldEdit.getPlatformManager().queryCapability(Capability.WORLD_EDITING);
         if (platform instanceof BukkitServerInterface) {
            LOGGER.warn(var7.getMessage());
         } else {
            LOGGER.info(
               "FastAsyncWorldEdit could not find a Bukkit adapter for this MC version, but it seems that you have another implementation of FastAsyncWorldEdit installed ({}) that handles the world editing.",
               platform.getPlatformName()
            );
         }

         this.adapter.invalidate();
      }
   }

   public void onDisable() {
      Fawe.instance().onDisable();
      WorldEdit worldEdit = WorldEdit.getInstance();
      worldEdit.getSessionManager().unload();
      if (this.platform != null) {
         worldEdit.getEventBus().post(new PlatformUnreadyEvent(this.platform));
         worldEdit.getPlatformManager().unregister(this.platform);
         this.platform.unregisterCommands();
      }

      if (this.config != null) {
         this.config.unload();
      }

      this.getServer().getScheduler().cancelTasks(this);
   }

   protected void loadConfiguration() {
      this.config.unload();
      this.config.load();
      this.getPermissionsResolver().load();
   }

   protected void createDefaultConfiguration(String name) {
      File actual = new File(this.getDataFolder(), name);
      if (!actual.exists()) {
         try (InputStream stream = this.getResource("defaults/" + name)) {
            if (stream == null) {
               throw new FileNotFoundException();
            }

            this.copyDefaultConfig(stream, actual, name);
         } catch (IOException var8) {
            LOGGER.error("Unable to read default configuration: " + name);
         }
      }
   }

   private void migrateLegacyConfig() {
      File legacy = new File(this.getDataFolder(), "config-legacy.yml");
      if (legacy.exists()) {
         try {
            legacy.renameTo(new File(this.getDataFolder(), "worldedit-config.yml"));
            LOGGER.info("Migrated config-legacy.yml to worldedit-config.yml");
         } catch (Exception var3) {
            LOGGER.error("Unable to rename legacy config file", var3);
         }
      }

      this.createDefaultConfiguration("worldedit-config.yml");
   }

   private void copyDefaultConfig(InputStream input, File actual, String name) {
      try (FileOutputStream output = new FileOutputStream(actual)) {
         byte[] buf = new byte[8192];

         int length;
         while ((length = input.read(buf)) > 0) {
            output.write(buf, 0, length);
         }

         LOGGER.info("Default configuration file written: " + name);
      } catch (IOException var9) {
         LOGGER.warn("Failed to write default config file", var9);
      }
   }

   public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
      String[] split = new String[args.length + 1];
      System.arraycopy(args, 0, split, 1, args.length);
      split[0] = commandLabel.startsWith("fastasyncworldedit:") ? commandLabel.replace("fastasyncworldedit:", "") : commandLabel;
      CommandEvent event = new CommandEvent(this.wrapCommandSender(sender), Joiner.on(" ").join(split));
      this.getWorldEdit().getEventBus().post(event);
      return true;
   }

   public LocalSession getSession(Player player) {
      return WorldEdit.getInstance().getSessionManager().get(this.wrapPlayer(player));
   }

   public EditSession createEditSession(Player player) {
      com.sk89q.worldedit.entity.Player wePlayer = this.wrapPlayer(player);
      LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
      BlockBag blockBag = session.getBlockBag(wePlayer);
      EditSession editSession = WorldEdit.getInstance()
         .newEditSessionBuilder()
         .locatableActor(wePlayer)
         .maxBlocks(session.getBlockChangeLimit())
         .blockBag(blockBag)
         .build();
      editSession.enableStandardMode();
      return editSession;
   }

   public void remember(Player player, EditSession editSession) {
      com.sk89q.worldedit.entity.Player wePlayer = this.wrapPlayer(player);
      LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
      session.remember(editSession);
      editSession.close();
      WorldEdit.getInstance().flushBlockBag(wePlayer, editSession);
   }

   public BukkitConfiguration getLocalConfiguration() {
      return this.config;
   }

   public PermissionsResolverManager getPermissionsResolver() {
      return PermissionsResolverManager.getInstance();
   }

   public BukkitPermissionAttachmentManager getPermissionAttachmentManager() {
      return this.permissionAttachmentManager;
   }

   public BukkitPlayer wrapPlayer(Player player) {
      BukkitPlayer wePlayer = this.getCachedPlayer(player);
      if (wePlayer != null) {
         return wePlayer;
      } else {
         synchronized (player) {
            BukkitPlayer bukkitPlayer = this.getCachedPlayer(player);
            if (bukkitPlayer == null) {
               bukkitPlayer = new BukkitPlayer(this, player);
               player.setMetadata("WE", new FixedMetadataValue(this, bukkitPlayer));
            }

            return bukkitPlayer;
         }
      }
   }

   BukkitPlayer getCachedPlayer(Player player) {
      List<MetadataValue> meta = player.getMetadata("WE");
      return meta.isEmpty() ? null : (BukkitPlayer)meta.get(0).value();
   }

   BukkitPlayer reCachePlayer(Player player) {
      synchronized (player) {
         BukkitPlayer wePlayer = new BukkitPlayer(this, player);
         player.setMetadata("WE", new FixedMetadataValue(this, wePlayer));
         return wePlayer;
      }
   }

   public Actor wrapCommandSender(CommandSender sender) {
      if (sender instanceof Player) {
         return this.wrapPlayer((Player)sender);
      } else {
         return (Actor)(this.config.commandBlockSupport && sender instanceof BlockCommandSender
            ? new BukkitBlockCommandSender(this, (BlockCommandSender)sender)
            : this.bukkitConsoleCommandSender);
      }
   }

   public BukkitServerInterface getInternalPlatform() {
      return this.platform;
   }

   public WorldEdit getWorldEdit() {
      return WorldEdit.getInstance();
   }

   public static WorldEditPlugin getInstance() {
      return (WorldEditPlugin)Preconditions.checkNotNull(INSTANCE);
   }

   Lifecycled<BukkitImplAdapter> getLifecycledBukkitImplAdapter() {
      return this.adapter;
   }

   public BukkitImplAdapter getBukkitImplAdapter() {
      return this.adapter.value().orElse(null);
   }

   private class AsyncTabCompleteListener implements Listener {
      AsyncTabCompleteListener() {
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onAsyncTabComplete(AsyncTabCompleteEvent event) {
         if (event.isCommand()) {
            String buffer = event.getBuffer();
            int firstSpace = buffer.indexOf(32);
            if (firstSpace >= 0) {
               String label = buffer.substring(0, firstSpace);
               label = label.startsWith("/") ? label.substring(1) : label;
               Plugin owner = WorldEditPlugin.this.platform.getDynamicCommands().getCommandOwner(label);
               if (owner == WorldEditPlugin.this) {
                  Optional<Command> command = WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getCommandManager().getCommand(label);
                  if (command.isPresent()) {
                     CommandSuggestionEvent suggestEvent = new CommandSuggestionEvent(WorldEditPlugin.this.wrapCommandSender(event.getSender()), buffer);
                     WorldEditPlugin.this.getWorldEdit().getEventBus().post(suggestEvent);
                     event.setCompletions(CommandUtil.fixSuggestions(buffer, suggestEvent.getSuggestions()));
                     event.setHandled(true);
                  }
               }
            }
         }
      }
   }

   private class WorldInitListener implements Listener {
      private boolean loaded = false;

      @EventHandler(
         priority = EventPriority.LOWEST
      )
      public void onWorldInit(WorldInitEvent event) {
         if (!this.loaded) {
            this.loaded = true;
            WorldEditPlugin.this.setupWorldData();
         }
      }
   }
}
