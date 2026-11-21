package ch.njol.skript.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.yggdrasil.Fields;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jetbrains.annotations.Nullable;

public abstract class Serializer<T> extends ch.njol.yggdrasil.YggdrasilSerializer<T> {
   @Nullable
   protected ClassInfo<? extends T> info = null;

   void register(ClassInfo<? extends T> info) {
      assert this.info == null && info != null;

      this.info = info;
   }

   @Nullable
   @Override
   public Class<? extends T> getClass(String id) {
      ClassInfo<? extends T> info = this.info;

      assert info != null;

      return id.equals(info.getCodeName()) ? info.getC() : null;
   }

   @Nullable
   @Override
   public String getID(Class<?> c) {
      ClassInfo<? extends T> info = this.info;

      assert info != null;

      return info.getC().isAssignableFrom(c) ? info.getCodeName() : null;
   }

   @Nullable
   @Override
   public <E extends T> E newInstance(Class<E> c) {
      ClassInfo<? extends T> info = this.info;

      assert info != null;

      assert info.getC().isAssignableFrom(c);

      try {
         Constructor<E> constr = c.getDeclaredConstructor();
         constr.setAccessible(true);
         return constr.newInstance();
      } catch (InstantiationException var4) {
         throw new SkriptAPIException(
            "Serializer of "
               + info.getCodeName()
               + " must override newInstance(), canBeInstantiated() or mustSyncDeserialization() if its class does not have a nullary constructor"
         );
      } catch (NoSuchMethodException var5) {
         throw new SkriptAPIException(
            "Serializer of "
               + info.getCodeName()
               + " must override newInstance(), canBeInstantiated() or mustSyncDeserialization() if its class does not have a nullary constructor"
         );
      } catch (SecurityException var6) {
         throw Skript.exception("Security manager present");
      } catch (IllegalArgumentException var7) {
         assert false;

         return null;
      } catch (IllegalAccessException var8) {
         assert false;

         return null;
      } catch (InvocationTargetException var9) {
         throw new RuntimeException(var9);
      }
   }

   @Override
   public abstract Fields serialize(T var1) throws NotSerializableException;

   @Override
   public void deserialize(T o, Fields f) throws StreamCorruptedException, NotSerializableException {
      throw new SkriptAPIException("deserialize(Object, Fields) has not been overridden in " + this.getClass() + " (serializer of " + this.info + ")");
   }

   public abstract boolean mustSyncDeserialization();

   @Override
   public boolean canBeInstantiated(Class<? extends T> c) {
      assert this.info != null && this.info.getC().isAssignableFrom(c);

      return this.canBeInstantiated();
   }

   protected abstract boolean canBeInstantiated();

   @Override
   public <E extends T> E deserialize(Class<E> c, Fields fields) throws StreamCorruptedException, NotSerializableException {
      ClassInfo<? extends T> info = this.info;

      assert info != null;

      assert info.getC().isAssignableFrom(c);

      return (E)this.deserialize(fields);
   }

   protected T deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
      throw new SkriptAPIException("deserialize(Fields) has not been overridden in " + this.getClass() + " (serializer of " + this.info + ")");
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public T deserialize(String s) {
      return null;
   }
}
