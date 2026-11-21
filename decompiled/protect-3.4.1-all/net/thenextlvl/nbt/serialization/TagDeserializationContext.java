package net.thenextlvl.nbt.serialization;

import java.lang.reflect.Type;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public sealed interface TagDeserializationContext permits NBT {
   @Contract(
      pure = true
   )
   <T> T deserialize(Tag var1, Class<T> var2) throws ParserException;

   @Contract(
      pure = true
   )
   <T> T deserialize(Tag var1, Type var2) throws ParserException;
}
