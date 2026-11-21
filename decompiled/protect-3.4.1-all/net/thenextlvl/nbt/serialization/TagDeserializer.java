package net.thenextlvl.nbt.serialization;

import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public interface TagDeserializer<T> {
   @Contract(
      pure = true
   )
   T deserialize(Tag var1, TagDeserializationContext var2) throws ParserException;
}
