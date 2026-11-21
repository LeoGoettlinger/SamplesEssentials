package net.thenextlvl.nbt.serialization;

import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.Contract;

public interface TagSerializable<T extends Tag> {
   @Contract(
      value = " -> new",
      pure = true
   )
   T serialize() throws ParserException;

   @Contract(
      mutates = "this"
   )
   void deserialize(T var1) throws ParserException;
}
