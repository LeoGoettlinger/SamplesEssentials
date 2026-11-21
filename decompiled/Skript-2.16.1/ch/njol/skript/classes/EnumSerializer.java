package ch.njol.skript.classes;

import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import org.jetbrains.annotations.Nullable;

public class EnumSerializer<T extends Enum<T>> extends Serializer<T> {
   private final Class<T> c;

   public EnumSerializer(Class<T> c) {
      this.c = c;
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public T deserialize(String s) {
      try {
         return Enum.valueOf(this.c, s);
      } catch (IllegalArgumentException var3) {
         return null;
      }
   }

   @Override
   public boolean mustSyncDeserialization() {
      return false;
   }

   @Override
   public boolean canBeInstantiated() {
      return false;
   }

   public Fields serialize(T e) {
      Fields fields = new Fields();
      fields.putObject("name", e.name());
      return fields;
   }

   public T deserialize(Fields fields) {
      try {
         String name = fields.getAndRemoveObject("name", String.class);
         return name == null ? null : Enum.valueOf(this.c, name);
      } catch (StreamCorruptedException | IllegalArgumentException var3) {
         return null;
      }
   }

   public void deserialize(T o, Fields f) {
      assert false;
   }
}
