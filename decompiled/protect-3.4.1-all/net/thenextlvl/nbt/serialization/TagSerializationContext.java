package net.thenextlvl.nbt.serialization;

import java.lang.reflect.Type;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public sealed interface TagSerializationContext permits NBT {
   @Contract(
      value = "_ -> new",
      pure = true
   )
   Tag serialize(Object var1) throws ParserException;

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   Tag serialize(Object var1, Class<?> var2) throws ParserException;

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   Tag serialize(Object var1, Type var2) throws ParserException;
}
