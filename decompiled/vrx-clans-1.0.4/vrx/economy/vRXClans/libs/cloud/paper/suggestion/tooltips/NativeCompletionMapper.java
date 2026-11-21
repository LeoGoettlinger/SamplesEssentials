package vrx.economy.vRXClans.libs.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import com.mojang.brigadier.Message;
import io.papermc.paper.brigadier.PaperBrigadier;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import vrx.economy.vRXClans.libs.cloud.brigadier.suggestion.TooltipSuggestion;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;

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
