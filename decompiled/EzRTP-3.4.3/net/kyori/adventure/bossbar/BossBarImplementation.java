package net.kyori.adventure.bossbar;

import java.util.List;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface BossBarImplementation {
   @Internal
   static <I extends BossBarImplementation> I get(final BossBar bar, final Class<I> type) {
      return BossBarImpl.ImplementationAccessor.get(bar, type);
   }

   @Internal
   default Iterable<? extends BossBarViewer> viewers() {
      return List.of();
   }

   @Internal
   public interface Provider {
      @Internal
      BossBarImplementation create(final BossBar bar);
   }
}
