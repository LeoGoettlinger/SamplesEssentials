package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.clock;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.mapper.AbstractMappedEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.mappings.TypesBuilderData;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class StaticWorldClock extends AbstractMappedEntity implements WorldClock {
   public StaticWorldClock() {
      this(null);
   }

   @ApiStatus.Internal
   public StaticWorldClock(@Nullable TypesBuilderData data) {
      super(data);
   }

   public WorldClock copy(@Nullable TypesBuilderData newData) {
      return new StaticWorldClock(newData);
   }

   @Override
   public boolean deepEquals(@Nullable Object obj) {
      return obj instanceof StaticWorldClock;
   }

   @Override
   public int deepHashCode() {
      return Objects.hashCode(0);
   }
}
