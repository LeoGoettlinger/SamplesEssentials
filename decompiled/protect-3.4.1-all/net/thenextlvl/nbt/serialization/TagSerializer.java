package net.thenextlvl.nbt.serialization;

import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public interface TagSerializer<T> {
   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   Tag serialize(T var1, TagSerializationContext var2) throws ParserException;
}
