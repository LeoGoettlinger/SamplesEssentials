package github.nighter.smartspawner.commands.set;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.BaseSubCommand;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.TimeFormatter;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SetSubCommand extends BaseSubCommand {
   private static final int TARGET_DISTANCE = 8;
   private static final int[] STACK_SIZE_SUGGESTIONS = new int[]{1, 10, 100, 1000, 10000};
   private static final int[] RANGE_SUGGESTIONS = new int[]{8, 16, 24, 32, 64};
   private static final String[] DELAY_SUGGESTIONS = new String[]{"5s", "10s", "25s", "1m", "5m"};
   private final SpawnerManager spawnerManager;
   private final TimeFormatter timeFormatter;

   public SetSubCommand(SmartSpawner plugin) {
      super(plugin);
      this.spawnerManager = plugin.getSpawnerManager();
      this.timeFormatter = new TimeFormatter(plugin);
   }

   @Override
   public String getName() {
      return "set";
   }

   @Override
   public String getPermission() {
      return "smartspawner.command.set";
   }

   @Override
   public String getDescription() {
      return "Set smart spawner properties";
   }

   @Override
   public LiteralArgumentBuilder<CommandSourceStack> build() {
      LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
      builder.requires(source -> this.hasPermission(source.getSender()));
      builder.executes(this::execute);
      builder.then(this.buildIntegerPropertyCommand(SetSubCommand.SetProperty.STACK_SIZE, STACK_SIZE_SUGGESTIONS));
      builder.then(this.buildIntegerPropertyCommand(SetSubCommand.SetProperty.RANGE, RANGE_SUGGESTIONS));
      builder.then(this.buildDelayCommand());
      return builder;
   }

   @Override
   public int execute(CommandContext<CommandSourceStack> context) {
      this.logCommandExecution(context);
      this.plugin.getMessageService().sendMessage(((CommandSourceStack)context.getSource()).getSender(), "set.usage");
      return 0;
   }

   private LiteralArgumentBuilder<CommandSourceStack> buildIntegerPropertyCommand(SetSubCommand.SetProperty property, int[] suggestions) {
      RequiredArgumentBuilder<CommandSourceStack, Integer> valueArgument = (RequiredArgumentBuilder<CommandSourceStack, Integer>)Commands.argument(
            "value", IntegerArgumentType.integer(1)
         )
         .suggests(this.createIntegerSuggestions(suggestions, property.valueTooltip()))
         .executes(context -> this.executeSet(context, property, String.valueOf(IntegerArgumentType.getInteger(context, "value")), false));
      this.addLocationArguments(valueArgument, property, context -> String.valueOf(IntegerArgumentType.getInteger(context, "value")));
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal(property.commandName()).then(valueArgument);
   }

   private LiteralArgumentBuilder<CommandSourceStack> buildDelayCommand() {
      RequiredArgumentBuilder<CommandSourceStack, String> valueArgument = (RequiredArgumentBuilder<CommandSourceStack, String>)Commands.argument(
            "value", StringArgumentType.word()
         )
         .suggests(this.createDelaySuggestions())
         .executes(context -> this.executeSet(context, SetSubCommand.SetProperty.DELAY, StringArgumentType.getString(context, "value"), false));
      this.addLocationArguments(valueArgument, SetSubCommand.SetProperty.DELAY, context -> StringArgumentType.getString(context, "value"));
      return (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal(SetSubCommand.SetProperty.DELAY.commandName()).then(valueArgument);
   }

   private <T> void addLocationArguments(
      RequiredArgumentBuilder<CommandSourceStack, T> valueArgument, SetSubCommand.SetProperty property, SetSubCommand.ValueReader valueReader
   ) {
      valueArgument.then(
         Commands.argument("world", StringArgumentType.word())
            .suggests(this.createWorldSuggestions())
            .then(
               Commands.argument("x", IntegerArgumentType.integer())
                  .suggests(this.createCoordinateSuggestions(SetSubCommand.Coordinate.X))
                  .then(
                     Commands.argument("y", IntegerArgumentType.integer())
                        .suggests(this.createCoordinateSuggestions(SetSubCommand.Coordinate.Y))
                        .then(
                           Commands.argument("z", IntegerArgumentType.integer())
                              .suggests(this.createCoordinateSuggestions(SetSubCommand.Coordinate.Z))
                              .executes(context -> this.executeSet(context, property, valueReader.read(context), true))
                        )
                  )
            )
      );
   }

   private int executeSet(CommandContext<CommandSourceStack> context, SetSubCommand.SetProperty property, String rawValue, boolean explicitLocation) {
      this.logCommandExecution(context);
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Long parsedValue = this.parseValue(sender, property, rawValue);
      if (parsedValue == null) {
         return 0;
      } else {
         SpawnerData spawner = explicitLocation ? this.getSpawnerFromArguments(context, sender) : this.getSpawnerPlayerIsLookingAt(sender);
         if (spawner == null) {
            return 0;
         } else {
            String oldValue = this.getDisplayValue(property, spawner);
            if (!this.applyValue(sender, property, spawner, parsedValue)) {
               return 0;
            } else {
               this.plugin.getSpawnerManager().markSpawnerModified(spawner.getSpawnerId());
               Location location = spawner.getSpawnerLocation();
               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("property", property.commandName());
               placeholders.put("old_value", oldValue);
               placeholders.put("new_value", this.getDisplayValue(property, spawner));
               placeholders.put("world", location.getWorld().getName());
               placeholders.put("x", String.valueOf(location.getBlockX()));
               placeholders.put("y", String.valueOf(location.getBlockY()));
               placeholders.put("z", String.valueOf(location.getBlockZ()));
               this.plugin.getMessageService().sendMessage(sender, "set.success", placeholders);
               return 1;
            }
         }
      }
   }

   private Long parseValue(CommandSender sender, SetSubCommand.SetProperty property, String rawValue) {
      if (property == SetSubCommand.SetProperty.DELAY) {
         long ticks = this.timeFormatter.parseTimeToTicks(rawValue, -1L);
         if (ticks <= 0L) {
            this.sendInvalidValue(sender, property);
            return null;
         } else {
            return ticks;
         }
      } else {
         try {
            int value = Integer.parseInt(rawValue);
            if (value < 1) {
               this.sendInvalidValue(sender, property);
               return null;
            } else {
               return (long)value;
            }
         } catch (NumberFormatException var6) {
            this.sendInvalidValue(sender, property);
            return null;
         }
      }
   }

   private void sendInvalidValue(CommandSender sender, SetSubCommand.SetProperty property) {
      this.sendInvalid(sender);
   }

   private void sendInvalid(CommandSender sender) {
      this.plugin.getMessageService().sendMessage(sender, "set.invalid");
   }

   private SpawnerData getSpawnerFromArguments(CommandContext<CommandSourceStack> context, CommandSender sender) {
      String worldName = StringArgumentType.getString(context, "world");
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         this.sendInvalid(sender);
         return null;
      } else {
         Location location = new Location(
            world, IntegerArgumentType.getInteger(context, "x"), IntegerArgumentType.getInteger(context, "y"), IntegerArgumentType.getInteger(context, "z")
         );
         SpawnerData spawner = this.spawnerManager.getSpawnerByLocation(location);
         if (spawner == null) {
            this.sendInvalid(sender);
         }

         return spawner;
      }
   }

   private SpawnerData getSpawnerPlayerIsLookingAt(CommandSender sender) {
      if (sender instanceof Player player) {
         Block targetBlock = this.getTargetSpawnerBlock(player);
         if (targetBlock == null) {
            this.sendInvalid(sender);
            return null;
         } else {
            SpawnerData spawner = this.spawnerManager.getSpawnerByLocation(targetBlock.getLocation());
            if (spawner == null) {
               this.sendInvalid(sender);
            }

            return spawner;
         }
      } else {
         this.plugin.getMessageService().sendMessage(sender, "set.location_required");
         return null;
      }
   }

   private Block getTargetSpawnerBlock(Player player) {
      Block targetBlock = player.getTargetBlockExact(8, FluidCollisionMode.NEVER);
      return targetBlock != null && targetBlock.getType() == Material.SPAWNER ? targetBlock : null;
   }

   private boolean applyValue(CommandSender sender, SetSubCommand.SetProperty property, SpawnerData spawner, long value) {
      switch (property) {
         case STACK_SIZE:
            int stackSize = this.toInt(value);
            if (stackSize > spawner.getMaxStackSize() && stackSize > spawner.getStackSize()) {
               Map<String, String> placeholders = new HashMap<>();
               placeholders.put("value", String.valueOf(stackSize));
               placeholders.put("max", String.valueOf(spawner.getMaxStackSize()));
               this.plugin.getMessageService().sendMessage(sender, "set.stack_size_exceeds_max", placeholders);
               return false;
            }

            spawner.setStackSize(stackSize);
            break;
         case RANGE:
            spawner.getDataLock().lock();

            try {
               spawner.setSpawnerRange(this.toInt(value));
               break;
            } finally {
               spawner.getDataLock().unlock();
            }
         case DELAY:
            spawner.getDataLock().lock();

            try {
               spawner.setSpawnDelay(value);
               spawner.setLastSpawnTime(System.currentTimeMillis());
            } finally {
               spawner.getDataLock().unlock();
            }
      }

      return true;
   }

   private int toInt(long value) {
      return value > 2147483647L ? Integer.MAX_VALUE : (int)value;
   }

   private String getDisplayValue(SetSubCommand.SetProperty property, SpawnerData spawner) {
      return switch (property) {
         case STACK_SIZE -> String.valueOf(spawner.getStackSize());
         case RANGE -> String.valueOf(spawner.getSpawnerRange());
         case DELAY -> this.timeFormatter.formatTicks(spawner.getSpawnDelay());
      };
   }

   private SuggestionProvider<CommandSourceStack> createIntegerSuggestions(int[] values, String tooltip) {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (int value : values) {
            String suggestion = String.valueOf(value);
            if (suggestion.startsWith(input)) {
               builder.suggest(value, new LiteralMessage(tooltip));
            }
         }

         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createDelaySuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (String suggestion : DELAY_SUGGESTIONS) {
            if (suggestion.startsWith(input)) {
               builder.suggest(suggestion, new LiteralMessage("Spawner delay, for example 25s or 1m"));
            }
         }

         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createWorldSuggestions() {
      return (context, builder) -> {
         String input = builder.getRemaining().toLowerCase(Locale.ROOT);

         for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            if (worldName.toLowerCase(Locale.ROOT).startsWith(input)) {
               builder.suggest(worldName, new LiteralMessage("World name"));
            }
         }

         return builder.buildFuture();
      };
   }

   private SuggestionProvider<CommandSourceStack> createCoordinateSuggestions(SetSubCommand.Coordinate coordinate) {
      return (context, builder) -> {
         if (((CommandSourceStack)context.getSource()).getSender() instanceof Player player) {
            Location location = player.getLocation();
            Block targetBlock = this.getTargetSpawnerBlock(player);
            if (targetBlock != null) {
               location = targetBlock.getLocation();
            }

            builder.suggest(coordinate.value(location), new LiteralMessage(coordinate.tooltip()));
         }

         return builder.buildFuture();
      };
   }

   private static enum Coordinate {
      X("X coordinate") {
         @Override
         int value(Location location) {
            return location.getBlockX();
         }
      },
      Y("Y coordinate") {
         @Override
         int value(Location location) {
            return location.getBlockY();
         }
      },
      Z("Z coordinate") {
         @Override
         int value(Location location) {
            return location.getBlockZ();
         }
      };

      private final String tooltip;

      private Coordinate(String tooltip) {
         this.tooltip = tooltip;
      }

      abstract int value(Location var1);

      String tooltip() {
         return this.tooltip;
      }
   }

   private static enum SetProperty {
      STACK_SIZE("stack_size", "Stack size"),
      RANGE("range", "Spawner range"),
      DELAY("delay", "Spawner delay");

      private final String commandName;
      private final String valueTooltip;

      private SetProperty(String commandName, String valueTooltip) {
         this.commandName = commandName;
         this.valueTooltip = valueTooltip;
      }

      String commandName() {
         return this.commandName;
      }

      String valueTooltip() {
         return this.valueTooltip;
      }
   }

   @FunctionalInterface
   private interface ValueReader {
      String read(CommandContext<CommandSourceStack> var1);
   }
}
