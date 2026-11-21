package net.thenextlvl.protect.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType.Converted;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.RegionizedArea;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class RegionizedAreaArgumentType implements Converted<RegionizedArea<?>, String> {
   private final ProtectPlugin plugin;
   private final Predicate<? super RegionizedArea<?>> filter;

   public RegionizedAreaArgumentType(ProtectPlugin plugin) {
      this(plugin, area -> true);
   }

   public RegionizedAreaArgumentType(ProtectPlugin plugin, Predicate<? super RegionizedArea<?>> filter) {
      this.plugin = plugin;
      this.filter = filter;
   }

   public RegionizedArea<?> convert(String nativeType) {
      Area area = this.plugin.areaProvider().getArea(nativeType).orElseThrow(() -> new IllegalArgumentException("Unknown area: " + nativeType));
      if (area instanceof RegionizedArea<?> regionized) {
         return regionized;
      } else {
         throw new IllegalStateException("Not a regionized area: " + area.getName());
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      this.plugin
         .areaProvider()
         .getAreas()
         .filter(area -> area instanceof RegionizedArea<?> regionized && this.filter.test(regionized))
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
