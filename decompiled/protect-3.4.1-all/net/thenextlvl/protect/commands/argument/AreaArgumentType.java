package net.thenextlvl.protect.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType.Converted;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiPredicate;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AreaArgumentType implements Converted<Area, String> {
   private final ProtectPlugin plugin;
   private final BiPredicate<? super CommandContext<?>, ? super Area> filter;

   public AreaArgumentType(ProtectPlugin plugin) {
      this(plugin, (context, area) -> true);
   }

   public AreaArgumentType(ProtectPlugin plugin, BiPredicate<CommandContext<?>, ? super Area> filter) {
      this.plugin = plugin;
      this.filter = filter;
   }

   public Area convert(String nativeType) {
      return this.plugin.areaProvider().getArea(nativeType).orElseThrow(() -> new IllegalArgumentException("Unknown area: " + nativeType));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      this.plugin
         .areaProvider()
         .getAreas()
         .filter(area -> this.filter.test(context, area))
         .map(Area::getName)
         .filter(s -> s.contains(builder.getRemaining()))
         .map(StringArgumentType::escapeIfRequired)
         .forEach(builder::suggest);
      return builder.buildFuture();
   }

   public ArgumentType<String> getNativeType() {
      return StringArgumentType.string();
   }
}
