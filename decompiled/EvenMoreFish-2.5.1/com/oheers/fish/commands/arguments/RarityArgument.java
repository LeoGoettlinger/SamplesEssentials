package com.oheers.fish.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType.Converted;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NonNull;

public class RarityArgument implements Converted<IRarity, String> {
   private static final SimpleCommandExceptionType UNKNOWN_RARITY = new SimpleCommandExceptionType(
      (Message)MessageComponentSerializer.message().serialize(ConfigMessage.RARITY_INVALID.getMessage().getComponentMessage())
   );

   @NonNull
   public IRarity convert(@NonNull String nativeType) throws CommandSyntaxException {
      IRarity rarity = FishManager.getInstance().getRarity(nativeType);
      if (rarity == null) {
         throw UNKNOWN_RARITY.create();
      } else {
         return rarity;
      }
   }

   @NonNull
   public ArgumentType<String> getNativeType() {
      return StringArgumentType.string();
   }

   @NonNull
   public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
      FishManager.getInstance()
         .getRarityMap()
         .keySet()
         .stream()
         .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
         .forEach(builder::suggest);
      return builder.buildFuture();
   }
}
