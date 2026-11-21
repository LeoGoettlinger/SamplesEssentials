package net.thenextlvl.nbt.serialization.adapters;

import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.ByteTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class BooleanAdapter implements TagAdapter<Boolean> {
   public static final BooleanAdapter INSTANCE = new BooleanAdapter();

   private BooleanAdapter() {
   }

   public Boolean deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      return tag.getAsBoolean();
   }

   public Tag serialize(Boolean object, TagSerializationContext context) throws ParserException {
      return ByteTag.of((byte)(object ? 1 : 0));
   }
}
