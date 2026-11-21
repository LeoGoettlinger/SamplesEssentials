package net.thenextlvl.protect.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.math.FinePosition;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.commands.argument.AreaArgumentType;
import net.thenextlvl.protect.commands.argument.EnumArgumentType;
import net.thenextlvl.protect.commands.argument.FlagArgumentType;
import net.thenextlvl.protect.commands.argument.FlagProviderArgumentType;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AreaFlagCommand {
   private static final Map<Class<?>, AreaFlagCommand.Converter> argumentTypes = Map.of(
      Boolean.class,
      new AreaFlagCommand.Converter(BoolArgumentType::bool),
      Double.class,
      new AreaFlagCommand.Converter(DoubleArgumentType::doubleArg),
      Float.class,
      new AreaFlagCommand.Converter(FloatArgumentType::floatArg),
      Integer.class,
      new AreaFlagCommand.Converter(IntegerArgumentType::integer),
      Long.class,
      new AreaFlagCommand.Converter(LongArgumentType::longArg),
      String.class,
      new AreaFlagCommand.Converter(StringArgumentType::string),
      Location.class,
      new AreaFlagCommand.Converter(ArgumentTypes::finePosition, (type, context) -> {
         FinePositionResolver resolver = (FinePositionResolver)context.getArgument("value", FinePositionResolver.class);
         Area area = (Area)context.getArgument("area", Area.class);
         return ((FinePosition)resolver.resolve((CommandSourceStack)context.getSource())).toLocation(area.getWorld());
      }),
      WeatherType.class,
      new AreaFlagCommand.Converter(() -> new EnumArgumentType(WeatherType.class))
   );

   public static LiteralArgumentBuilder<CommandSourceStack> create(ProtectPlugin plugin) {
      return (LiteralArgumentBuilder<CommandSourceStack>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                        "flag"
                     )
                     .requires(stack -> stack.getSender().hasPermission("protect.command.area.flag")))
                  .then(info(plugin)))
               .then(list(plugin)))
            .then(set(plugin)))
         .then(reset(plugin));
   }

   private static ArgumentBuilder<CommandSourceStack, ?> info(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)Commands.literal("info").requires(stack -> stack.getSender().hasPermission("protect.command.area.flag.info")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument("flag", new FlagArgumentType(plugin))
                  .then(Commands.argument("area", new AreaArgumentType(plugin)).executes(context -> {
                     Area area = (Area)context.getArgument("area", Area.class);
                     return info(context, area, plugin);
                  })))
               .executes(context -> {
                  Location location = ((CommandSourceStack)context.getSource()).getLocation();
                  Area area = plugin.areaProvider().getArea(location);
                  return info(context, area, plugin);
               })
         );
   }

   private static ArgumentBuilder<CommandSourceStack, ?> list(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("list")
               .requires(stack -> stack.getSender().hasPermission("protect.command.area.flag.list")))
            .then(Commands.argument("provider", new FlagProviderArgumentType(plugin)).executes(context -> {
               Plugin provider = (Plugin)context.getArgument("provider", Plugin.class);
               return list(context, provider, plugin);
            })))
         .executes(context -> {
            plugin.flagRegistry().getRegistry().keySet().forEach(key -> list(context, key, plugin));
            return 1;
         });
   }

   private static ArgumentBuilder<CommandSourceStack, ?> set(ProtectPlugin plugin) {
      LiteralArgumentBuilder<CommandSourceStack> command = (LiteralArgumentBuilder<CommandSourceStack>)Commands.literal("set")
         .requires(stack -> stack.getSender().hasPermission("protect.command.area.flag.set"));
      plugin.flagRegistry()
         .getRegistry()
         .values()
         .forEach(
            flags -> flags.forEach(
               flag -> {
                  AreaFlagCommand.Converter converter = argumentTypes.get(flag.type());
                  if (converter == null) {
                     plugin.getComponentLogger().error("No argument type for flag type: {}", flag.type().getName());
                  } else {
                     command.then(
                        Commands.literal(flag.key().asString())
                           .then(
                              ((RequiredArgumentBuilder)Commands.argument("value", converter.type().get())
                                    .then(Commands.argument("area", new AreaArgumentType(plugin)).executes(context -> {
                                       Area area = (Area)context.getArgument("area", Area.class);
                                       return set(context, converter.resolver(), flag, area, plugin);
                                    })))
                                 .executes(context -> {
                                    Location location = ((CommandSourceStack)context.getSource()).getLocation();
                                    return set(context, converter.resolver(), flag, plugin.areaProvider().getArea(location), plugin);
                                 })
                           )
                     );
                  }
               }
            )
         );
      return command;
   }

   private static ArgumentBuilder<CommandSourceStack, ?> reset(ProtectPlugin plugin) {
      return ((LiteralArgumentBuilder)Commands.literal("reset").requires(stack -> stack.getSender().hasPermission("protect.command.area.flag.reset")))
         .then(
            ((RequiredArgumentBuilder)Commands.argument(
                     "flag", new FlagArgumentType(plugin, flag -> plugin.areaProvider().getAreas().anyMatch(area -> area.hasFlag(flag)))
                  )
                  .then(Commands.argument("area", new AreaArgumentType(plugin, (context, area) -> {
                     Flag<?> flag = (Flag<?>)context.getLastChild().getArgument("flag", Flag.class);
                     return area.hasFlag(flag);
                  })).executes(context -> {
                     Area area = (Area)context.getArgument("area", Area.class);
                     return reset(context, area, plugin);
                  })))
               .executes(context -> {
                  Location location = ((CommandSourceStack)context.getSource()).getLocation();
                  return reset(context, plugin.areaProvider().getArea(location), plugin);
               })
         );
   }

   private static int info(CommandContext<CommandSourceStack> context, Area area, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Flag<?> flag = (Flag<?>)context.getArgument("flag", Flag.class);
      plugin.bundle()
         .sendMessage(
            sender,
            "area.flag.info",
            Placeholder.parsed("area", area.getName()),
            Placeholder.parsed("flag", flag.key().asString()),
            Placeholder.unparsed("value", String.valueOf(area.getFlag(flag)))
         );
      return 1;
   }

   private static int list(CommandContext<CommandSourceStack> context, Plugin provider, ProtectPlugin plugin) {
      CommandSender sender = ((CommandSourceStack)context.getSource()).getSender();
      Set<Flag<?>> flags = plugin.flagRegistry().getFlags(provider);
      List<Component> components = flags.stream()
         .map(flag -> plugin.bundle().component("area.flag.list.format", sender, Placeholder.parsed("flag", flag.key().asString())))
         .toList();
      plugin.bundle()
         .sendMessage(
            sender,
            "area.flag.list",
            Placeholder.parsed("provider", provider.getName()),
            Formatter.number("amount", flags.size()),
            Formatter.joining("flags", components)
         );
      return 1;
   }

   private static int set(CommandContext<CommandSourceStack> context, AreaFlagCommand.Resolver<?> resolver, Flag flag, Area area, ProtectPlugin plugin) throws CommandSyntaxException {
      Object value = resolver.resolve(flag.type(), context);
      String message = area.setFlag(flag, value) ? "area.flag.set" : "nothing.changed";
      plugin.bundle()
         .sendMessage(
            ((CommandSourceStack)context.getSource()).getSender(),
            message,
            Placeholder.parsed("area", area.getName()),
            Placeholder.parsed("flag", flag.key().asString()),
            Placeholder.unparsed("value", String.valueOf(value))
         );
      return 1;
   }

   private static int reset(CommandContext<CommandSourceStack> context, Area area, ProtectPlugin plugin) {
      Flag<?> flag = (Flag<?>)context.getArgument("flag", Flag.class);
      String message = area.removeFlag(flag) ? "area.flag.reset" : "nothing.changed";
      plugin.bundle()
         .sendMessage(
            ((CommandSourceStack)context.getSource()).getSender(),
            message,
            Placeholder.parsed("area", area.getName()),
            Placeholder.parsed("flag", flag.key().asString())
         );
      return 1;
   }

   private record Converter(Supplier<ArgumentType<?>> type, AreaFlagCommand.Resolver<?> resolver) {
      public Converter(Supplier<ArgumentType<?>> supplier) {
         this(supplier, (type, context) -> context.getArgument("value", type));
      }
   }

   @FunctionalInterface
   public interface Resolver<T> {
      T resolve(Class<T> var1, CommandContext<CommandSourceStack> var2) throws CommandSyntaxException;
   }
}
