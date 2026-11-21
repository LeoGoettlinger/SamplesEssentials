package de.theredend2000.advancedhunt.lib.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import com.mojang.brigadier.Message;
import de.theredend2000.advancedhunt.lib.cloud.brigadier.suggestion.TooltipSuggestion;
import de.theredend2000.advancedhunt.lib.cloud.bukkit.internal.CraftBukkitReflection;
import io.papermc.paper.brigadier.PaperBrigadier;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

final class NativeCompletionMapper implements CompletionMapper {
   @NonNull
   @Override
   public Completion map(@NonNull final TooltipSuggestion suggestion) {
      return !CraftBukkitReflection.classExists("io.papermc.paper.command.brigadier.MessageComponentSerializer")
         ? mapLegacy(suggestion)
         : Completion.completion(suggestion.suggestion(), MessageComponentSerializer.message().deserializeOrNull(suggestion.tooltip()));
   }

   @NonNull
   private static Completion mapLegacy(@NotNull final TooltipSuggestion suggestion) {
      Message tooltip = suggestion.tooltip();
      return tooltip == null
         ? Completion.completion(suggestion.suggestion())
         : Completion.completion(suggestion.suggestion(), PaperBrigadier.componentFromMessage(tooltip));
   }
}
