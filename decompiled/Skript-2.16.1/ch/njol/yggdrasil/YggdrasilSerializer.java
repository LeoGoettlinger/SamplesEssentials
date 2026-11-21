package ch.njol.yggdrasil;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import org.jetbrains.annotations.Nullable;

public abstract class YggdrasilSerializer<T> implements ClassResolver {
   @Nullable
   @Override
   public abstract Class<? extends T> getClass(String var1);

   public abstract Fields serialize(T var1) throws NotSerializableException;

   public boolean canBeInstantiated(Class<? extends T> type) {
      return true;
   }

   @Nullable
   public abstract <E extends T> E newInstance(Class<E> var1);

   public abstract void deserialize(T var1, Fields var2) throws StreamCorruptedException, NotSerializableException;

   public <E extends T> E deserialize(Class<E> type, Fields fields) throws StreamCorruptedException, NotSerializableException {
      throw new YggdrasilException(this.getClass() + " does not override deserialize(Class, Fields)");
   }
}
