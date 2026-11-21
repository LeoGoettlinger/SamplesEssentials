package vrx.economy.vRXClans.libs.cloud.paper.suggestion;

import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.bukkit.internal.CraftBukkitReflection;
import vrx.economy.vRXClans.libs.cloud.paper.LegacyPaperCommandManager;

@API(
   status = API.Status.INTERNAL,
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
