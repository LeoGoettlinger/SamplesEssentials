package net.thenextlvl.protect.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType.Converted;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.kyori.adventure.key.Key;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.flag.Flag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FlagArgumentType implements Converted<Flag<?>, Key> {
   private final ProtectPlugin plugin;
   private final Predicate<? super Flag<?>> filter;

   public FlagArgumentType(ProtectPlugin plugin) {
      this(plugin, flag -> true);
   }

   public FlagArgumentType(ProtectPlugin plugin, Predicate<? super Flag<?>> filter) {
      this.plugin = plugin;
      this.filter = filter;
   }

   public Flag<?> convert(Key nativeType) {
      return this.plugin.flagRegistry().getFlag(nativeType).filter(this.filter).orElseThrow(() -> new IllegalArgumentException("Unknown flag: " + nativeType));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      this.plugin
         .flagRegistry()
         .getFlags()
         .stream()
         .filter(this.filter)
         .map(Flag::key)
         .<String>map(Key::asString)
         .filter(s -> s.contains(builder.getRemaining()))
         .forEach(builder::suggest);
      return builder.buildFuture();
   }

   public ArgumentType<Key> getNativeType() {
      return ArgumentTypes.key();
   }
}
