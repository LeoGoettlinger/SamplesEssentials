package com.artillexstudios.axafkzone.libs.lamp.bukkit.core;

import com.artillexstudios.axafkzone.libs.lamp.CommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.autocomplete.SuggestionProvider;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitBrigadier;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandActor;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.BukkitCommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.Version;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.adventure.AudienceSenderResolver;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.adventure.ComponentResponseHandler;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.brigadier.CommodoreBukkitBrigadier;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.BukkitExceptionAdapter;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.InvalidPlayerException;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.InvalidWorldException;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.MalformedEntitySelectorException;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.MoreThanOnePlayerException;
import com.artillexstudios.axafkzone.libs.lamp.bukkit.exception.NonPlayerEntitiesException;
import com.artillexstudios.axafkzone.libs.lamp.command.CommandCategory;
import com.artillexstudios.axafkzone.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axafkzone.libs.lamp.core.BaseCommandHandler;
import com.artillexstudios.axafkzone.libs.lamp.core.CommandPath;
import com.artillexstudios.axafkzone.libs.lamp.exception.EnumNotFoundException;
import com.artillexstudios.axafkzone.libs.lamp.util.Preconditions;
import com.artillexstudios.axafkzone.libs.lamp.util.Primitives;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class BukkitHandler extends BaseCommandHandler implements BukkitCommandHandler {
   public static final SuggestionProvider playerSuggestionProvider = (args, sender, command) -> Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> !((BukkitCommandActor)sender).isPlayer() || ((BukkitCommandActor)sender).requirePlayer().canSee(player))
      .<String>map(HumanEntity::getName)
      .collect(Collectors.toList());
   private final Plugin plugin;
   private Optional<BukkitBrigadier> brigadier;
   @Nullable
   Object bukkitAudiences;
   private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
   @Nullable
   private static final Field KNOWN_COMMANDS;
   private static final CommandMap COMMAND_MAP;

   public BukkitHandler(@NotNull Plugin plugin) {
      this.plugin = Preconditions.notNull(plugin, "plugin");

      try {
         this.brigadier = Optional.of(new CommodoreBukkitBrigadier(this));
      } catch (NoClassDefFoundError var3) {
         this.brigadier = Optional.empty();
      }

      this.registerSenderResolver(BukkitSenderResolver.INSTANCE);
      this.registerValueResolver(Player.class, context -> {
         String value = context.pop();
         if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
            return context.<BukkitCommandActor>actor().requirePlayer();
         } else if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors()) {
            try {
               List<Entity> entityList = Bukkit.selectEntities(context.<BukkitActor>actor().getSender(), value);
               if (entityList.stream().anyMatch(c -> !(c instanceof Player))) {
                  throw new NonPlayerEntitiesException(value);
               } else if (entityList.isEmpty()) {
                  throw new InvalidPlayerException(context.parameter(), value);
               } else if (entityList.size() > 1) {
                  throw new MoreThanOnePlayerException(value);
               } else {
                  return (Player)entityList.get(0);
               }
            } catch (IllegalArgumentException var3x) {
               throw new MalformedEntitySelectorException(context.actor(), value, var3x.getCause().getMessage());
            }
         } else {
            Player player = Bukkit.getPlayerExact(value);
            if (player == null) {
               throw new InvalidPlayerException(context.parameter(), value);
            } else {
               return player;
            }
         }
      });
      this.registerValueResolver(OfflinePlayer.class, context -> {
         String value = context.pop();
         if (!value.equalsIgnoreCase("self") && !value.equalsIgnoreCase("me")) {
            Player player = Bukkit.getPlayerExact(value);
            if (player != null) {
               return player;
            } else {
               OfflinePlayer offlinePlayer = getCachedOfflinePlayer(value);
               if (offlinePlayer == null && !(offlinePlayer = Bukkit.getOfflinePlayer(value)).hasPlayedBefore()) {
                  throw new InvalidPlayerException(context.parameter(), value);
               } else {
                  return offlinePlayer;
               }
            }
         } else {
            return context.<BukkitCommandActor>actor().requirePlayer();
         }
      });
      this.registerValueResolver(World.class, context -> {
         String value = context.pop();
         if (!value.equalsIgnoreCase("self") && !value.equalsIgnoreCase("me")) {
            World world = Bukkit.getWorld(value);
            if (world == null) {
               throw new InvalidWorldException(context.parameter(), value);
            } else {
               return world;
            }
         } else {
            return context.<BukkitCommandActor>actor().requirePlayer().getWorld();
         }
      });
      this.registerValueResolver(EntityType.class, context -> {
         String value = context.pop().toLowerCase();
         if (value.startsWith("minecraft:")) {
            value = value.substring("minecraft:".length());
         }

         EntityType type = EntityType.fromName(value);
         if (type == null) {
            throw new EnumNotFoundException(context.parameter(), value);
         } else {
            return type;
         }
      });
      if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors() && this.isBrigadierSupported()) {
         this.getAutoCompleter().registerParameterSuggestions(EntityType.class, SuggestionProvider.EMPTY);
      }

      this.registerValueResolverFactory(EntitySelectorResolver.INSTANCE);
      this.getAutoCompleter().registerSuggestion("players", playerSuggestionProvider);
      this.getAutoCompleter().registerSuggestion("worlds", SuggestionProvider.map(Bukkit::getWorlds, World::getName));
      this.getAutoCompleter().registerParameterSuggestions(Player.class, "players");
      this.getAutoCompleter().registerParameterSuggestions(OfflinePlayer.class, "players");
      this.getAutoCompleter().registerParameterSuggestions(World.class, "worlds");
      this.getAutoCompleter().registerSuggestionFactory(EntitySelectorResolver.SelectorSuggestionFactory.INSTANCE);
      this.registerContextValue(plugin.getClass(), plugin);
      this.registerDependency(plugin.getClass(), plugin);
      this.registerDependency(FileConfiguration.class, plugin::getConfig);
      this.registerDependency(Logger.class, plugin::getLogger);
      this.registerPermissionReader(BukkitPermissionReader.INSTANCE);
      this.setExceptionHandler(BukkitExceptionAdapter.INSTANCE);
      Bukkit.getServer().getPluginManager().registerEvents(new BukkitCommandListeners(this), plugin);
   }

   @NotNull
   @Override
   public CommandHandler register(@NotNull Object... commands) {
      super.register(commands);

      for (ExecutableCommand command : this.executables.values()) {
         if (command.getParent() == null) {
            this.createPluginCommand(command.getName(), command.getDescription(), command.getUsage());
         }
      }

      for (CommandCategory category : this.categories.values()) {
         if (category.getParent() == null) {
            this.createPluginCommand(category.getName(), null, null);
         }
      }

      return this;
   }

   @NotNull
   @Override
   public Optional<BukkitBrigadier> getBrigadier() {
      return this.brigadier;
   }

   @Override
   public boolean isBrigadierSupported() {
      return this.brigadier.isPresent();
   }

   @Override
   public BukkitCommandHandler registerBrigadier() {
      this.brigadier.ifPresent(BukkitBrigadier::register);
      return this;
   }

   @Override
   public void enableAdventure() {
      if (Audience.class.isAssignableFrom(CommandSender.class)) {
         this.registerSenderResolver(new AudienceSenderResolver(sender -> (Audience)sender));
         this.registerResponseHandler(ComponentLike.class, new ComponentResponseHandler(sender -> (Audience)sender));
      } else {
         this.enableAdventure(BukkitAudiences.create(this.plugin));
      }
   }

   @Override
   public void enableAdventure(@NotNull BukkitAudiences audiences) {
      Preconditions.notNull(audiences, "audiences");
      this.bukkitAudiences = audiences;
      this.registerSenderResolver(new AudienceSenderResolver(audiences::sender));
      this.registerResponseHandler(ComponentLike.class, new ComponentResponseHandler(audiences::sender));
   }

   @NotNull
   @Override
   public Plugin getPlugin() {
      return this.plugin;
   }

   private void createPluginCommand(String name, @Nullable String description, @Nullable String usage) {
      try {
         PluginCommand cmd = ((JavaPlugin)this.plugin).getCommand(name);
         if (cmd == null) {
            cmd = COMMAND_CONSTRUCTOR.newInstance(name, this.plugin);
            COMMAND_MAP.register(this.plugin.getName(), cmd);
         }

         BukkitCommandExecutor executor = new BukkitCommandExecutor(this);
         cmd.setExecutor(executor);
         cmd.setTabCompleter(executor);
         if (cmd.getDescription().isEmpty() && description != null) {
            cmd.setDescription(description);
         }

         if (cmd.getUsage().isEmpty() && usage != null) {
            cmd.setUsage(usage);
         }
      } catch (Throwable var6) {
         throw var6;
      }
   }

   @Override
   public boolean unregister(@NotNull CommandPath path) {
      if (path.isRoot()) {
         PluginCommand command = ((JavaPlugin)this.plugin).getCommand(path.getFirst());
         this.unregisterCommand(command);
      }

      return super.unregister(path);
   }

   private void unregisterCommand(PluginCommand command) {
      if (command != null) {
         command.unregister(COMMAND_MAP);
         Map<String, Command> knownCommands = getKnownCommands();
         if (knownCommands != null) {
            Command rawAlias = knownCommands.get(command.getName());
            if (rawAlias instanceof PluginCommand && ((PluginCommand)rawAlias).getPlugin() == this.plugin) {
               knownCommands.remove(command.getName());
            }

            knownCommands.remove(this.plugin.getDescription().getName() + ":" + command.getName());
         }
      }
   }

   public static Class<? extends Entity> getSelectedEntity(@NotNull Type selectorType) {
      return (Class<? extends Entity>)Primitives.getInsideGeneric(selectorType, Entity.class);
   }

   @Nullable
   public static Map<String, Command> getKnownCommands() {
      try {
         return KNOWN_COMMANDS != null ? (Map)KNOWN_COMMANDS.get(COMMAND_MAP) : null;
      } catch (Throwable var1) {
         throw var1;
      }
   }

   private static OfflinePlayer getCachedOfflinePlayer(String name) {
      OfflinePlayer offlinePlayer = Bukkit.getPlayerExact(name);
      if (offlinePlayer == null) {
         try {
            Class<?> minecraftServerClass = Class.forName("net.minecraft.server.MinecraftServer");
            Method minecraftServer = minecraftServerClass.getDeclaredMethod("getServer");
            minecraftServer.setAccessible(true);
            Object serverInstance = minecraftServer.invoke(null);
            Object gameProfileCache;
            switch (Version.getServerVersion()) {
               case v1_20_1:
               case v1_20_2: {
                  Method method = minecraftServerClass.getDeclaredMethod("ap");
                  method.setAccessible(true);
                  gameProfileCache = method.invoke(serverInstance);
                  break;
               }
               case v1_20_3: {
                  Method method = minecraftServerClass.getDeclaredMethod("ar");
                  method.setAccessible(true);
                  gameProfileCache = method.invoke(serverInstance);
                  break;
               }
               case v1_20_4:
               case v1_21: {
                  Method method = minecraftServerClass.getDeclaredMethod("au");
                  method.setAccessible(true);
                  gameProfileCache = method.invoke(serverInstance);
                  break;
               }
               case v1_21_2:
               case v1_21_3:
               case v1_21_4:
               case v1_21_5:
               case v1_21_6: {
                  Method method = minecraftServerClass.getDeclaredMethod("at");
                  method.setAccessible(true);
                  gameProfileCache = method.invoke(serverInstance);
               }
               default:
                  Method servicesMethod = minecraftServerClass.getDeclaredMethod("av");
                  servicesMethod.setAccessible(true);
                  Object services = servicesMethod.invoke(null);
                  Class<?> servicesClass = Class.forName("net.minecraft.server.Services");
                  Method nameToIdCacheGetterMethod = servicesClass.getDeclaredMethod("f");
                  gameProfileCache = nameToIdCacheGetterMethod.invoke(services);
            }

            if (gameProfileCache == null) {
               return null;
            }

            if (Version.getServerVersion().isOlderThan(Version.v1_21_7)) {
               Object gameProfile = gameProfileCache.getClass().getDeclaredMethod("getProfileIfCached", String.class).invoke(gameProfileCache, name);
               if (gameProfile != null) {
                  return (OfflinePlayer)Bukkit.getServer()
                     .getClass()
                     .getDeclaredMethod("getOfflinePlayer", Class.forName("com.mojang.authlib.GameProfile"))
                     .invoke(Bukkit.getServer(), gameProfile);
               }
            } else {
               Object nameAndId = gameProfileCache.getClass().getDeclaredMethod("getIfCached", String.class).invoke(gameProfileCache, name);
               if (nameAndId != null) {
                  return (OfflinePlayer)Bukkit.getServer()
                     .getClass()
                     .getDeclaredMethod("getOfflinePlayer", Class.forName("net.minecraft.server.players.NameAndId"))
                     .invoke(Bukkit.getServer(), nameAndId);
               }
            }
         } catch (Exception var10) {
         }
      }

      return offlinePlayer;
   }

   static {
      Field knownCommands = null;

      Constructor<PluginCommand> ctr;
      CommandMap commandMap;
      try {
         ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
         ctr.setAccessible(true);
         Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
         commandMapField.setAccessible(true);
         commandMap = (CommandMap)commandMapField.get(Bukkit.getServer());
         if (commandMap instanceof SimpleCommandMap) {
            knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommands.setAccessible(true);
         }
      } catch (NoSuchMethodException var4) {
         throw new IllegalStateException("Unable to access PluginCommand(String, Plugin) construtor!");
      } catch (IllegalAccessException | NoSuchFieldException var5) {
         var5.printStackTrace();
         throw new IllegalStateException("Unable to access Bukkit.getServer()#commandMap!");
      }

      COMMAND_CONSTRUCTOR = ctr;
      COMMAND_MAP = commandMap;
      KNOWN_COMMANDS = knownCommands;
   }
}
