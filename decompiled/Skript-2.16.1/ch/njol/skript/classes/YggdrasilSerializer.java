package ch.njol.skript.classes;

import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

public class YggdrasilSerializer<T extends YggdrasilSerializable> extends Serializer<T> {
   public final Fields serialize(T o) throws NotSerializableException {
      return o instanceof YggdrasilSerializable.YggdrasilExtendedSerializable
         ? ((YggdrasilSerializable.YggdrasilExtendedSerializable)o).serialize()
         : new Fields(o);
   }

   public final void deserialize(T o, Fields f) throws StreamCorruptedException, NotSerializableException {
      if (o instanceof YggdrasilSerializable.YggdrasilExtendedSerializable) {
         ((YggdrasilSerializable.YggdrasilExtendedSerializable)o).deserialize(f);
      } else {
         f.setFields(o);
      }
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   public T deserialize(String s) {
      return null;
   }

   @Override
   public boolean mustSyncDeserialization() {
      return false;
   }

   @Override
   public boolean canBeInstantiated() {
      return true;
   }
}
