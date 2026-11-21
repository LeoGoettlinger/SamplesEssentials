package net.thenextlvl.protect.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType.Converted;
import java.util.concurrent.CompletableFuture;
import net.thenextlvl.protect.ProtectPlugin;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FlagProviderArgumentType implements Converted<Plugin, String> {
   private final ProtectPlugin plugin;

   public FlagProviderArgumentType(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   public Plugin convert(String nativeType) {
      Plugin provider = this.plugin.getServer().getPluginManager().getPlugin(nativeType);
      if (provider != null) {
         return provider;
      } else {
         throw new IllegalArgumentException("Unknown provider: " + nativeType);
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
      this.plugin
         .flagRegistry()
         .getRegistry()
         .keySet()
         .stream()
         .<String>map(Plugin::getName)
         .filter(s -> s.contains(builder.getRemaining()))
         .map(StringArgumentType::escapeIfRequired)
         .forEach(builder::suggest);
      return builder.buildFuture();
   }

   public ArgumentType<String> getNativeType() {
      return StringArgumentType.string();
   }
}
