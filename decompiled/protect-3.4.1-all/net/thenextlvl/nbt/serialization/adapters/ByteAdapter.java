package net.thenextlvl.nbt.serialization.adapters;

import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.ByteTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ByteAdapter implements TagAdapter<Byte> {
   public static final ByteAdapter INSTANCE = new ByteAdapter();

   private ByteAdapter() {
   }

   public Byte deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      return tag.getAsByte();
   }

   public Tag serialize(Byte object, TagSerializationContext context) throws ParserException {
      return ByteTag.of(object);
   }
}
