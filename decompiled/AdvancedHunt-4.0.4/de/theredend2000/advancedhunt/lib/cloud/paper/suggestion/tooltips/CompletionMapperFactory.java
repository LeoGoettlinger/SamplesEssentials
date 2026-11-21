package de.theredend2000.advancedhunt.lib.cloud.paper.suggestion.tooltips;

import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import org.apiguardian.api.API;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
public interface CompletionMapperFactory {
   @NonNull
   static CompletionMapperFactory detectingRelocation() {
      return new CompletionMapperFactory.CompletionMapperFactoryImpl();
   }

   @NonNull
   CompletionMapper createMapper();

   public static final class CompletionMapperFactoryImpl implements CompletionMapperFactory {
      private CompletionMapperFactoryImpl() {
      }

      @NonNull
      @Override
      public CompletionMapper createMapper() {
         return (CompletionMapper)(Audience.class.isAssignableFrom(Player.class) ? new NativeCompletionMapper() : new ReflectiveCompletionMapper());
      }
   }
}
