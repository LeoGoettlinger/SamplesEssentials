package net.thenextlvl.nbt.serialization.adapters;

import java.time.Duration;
import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.LongTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class DurationAdapter implements TagAdapter<Duration> {
   public static final DurationAdapter INSTANCE = new DurationAdapter();

   private DurationAdapter() {
   }

   public Duration deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      return Duration.ofMillis(tag.getAsLong());
   }

   public Tag serialize(Duration duration, TagSerializationContext context) throws ParserException {
      return LongTag.of(duration.toMillis());
   }
}
