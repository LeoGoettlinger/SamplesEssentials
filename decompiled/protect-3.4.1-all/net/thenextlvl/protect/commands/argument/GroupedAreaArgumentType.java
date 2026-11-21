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
import net.thenextlvl.protect.area.GroupedArea;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GroupedAreaArgumentType implements Converted<GroupedArea, String> {
   private final ProtectPlugin plugin;
   private final Predicate<? super GroupedArea> filter;

   public GroupedAreaArgumentType(ProtectPlugin plugin) {
      this(plugin, area -> true);
   }

   public GroupedAreaArgumentType(ProtectPlugin plugin, Predicate<? super GroupedArea> filter) {
      this.plugin = plugin;
      this.filter = filter;
   }

   public GroupedArea convert(String nativeType) {
      Area area = this.plugin.areaProvider().getArea(nativeType).orElseThrow(() -> new IllegalArgumentException("Unknown area: " + nativeType));
      if (area instanceof GroupedArea grouped) {
         return grouped;
      } else {
         throw new IllegalStateException("Not a grouped area: " + area.getName());
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      this.plugin
         .areaProvider()
         .getAreas()
         .filter(area -> area instanceof GroupedArea grouped && this.filter.test(grouped))
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
