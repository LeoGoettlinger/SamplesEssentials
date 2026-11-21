package de.oliver.cloud.paper.suggestion;

import de.oliver.cloud.bukkit.internal.CraftBukkitReflection;
import de.oliver.cloud.paper.LegacyPaperCommandManager;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.INTERNAL,
   since = "2.0.0"
)
public interface SuggestionListenerFactory<C> {
   @NonNull
   static <C> SuggestionListenerFactory<C> create(@NonNull final LegacyPaperCommandManager<C> commandManager) {
      return new SuggestionListenerFactory.SuggestionListenerFactoryImpl<>(commandManager);
   }

   @NonNull
   SuggestionListener<C> createListener();

   public static final class SuggestionListenerFactoryImpl<C> implements SuggestionListenerFactory<C> {
      private final LegacyPaperCommandManager<C> commandManager;

      private SuggestionListenerFactoryImpl(@NonNull final LegacyPaperCommandManager<C> commandManager) {
         this.commandManager = commandManager;
      }

      @Override
      public SuggestionListener<C> createListener() {
         Class<?> completionCls = CraftBukkitReflection.findClass("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent$Completion");
         return (SuggestionListener<C>)(completionCls != null
            ? new BrigadierAsyncCommandSuggestionListener<>(this.commandManager)
            : new AsyncCommandSuggestionListener<>(this.commandManager));
      }
   }
}
