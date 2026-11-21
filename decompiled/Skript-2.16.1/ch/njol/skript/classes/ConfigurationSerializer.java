package ch.njol.skript.classes;

import ch.njol.yggdrasil.Fields;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.lang.invoke.StringConcatFactory;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.Nullable;

public class ConfigurationSerializer<T extends ConfigurationSerializable> extends Serializer<T> {
   public Fields serialize(T o) throws NotSerializableException {
      Fields f = new Fields();
      f.putObject("value", serializeCS(o));
      return f;
   }

   @Override
   public boolean mustSyncDeserialization() {
      return false;
   }

   @Override
   public boolean canBeInstantiated() {
      return false;
   }

   protected T deserialize(Fields fields) throws StreamCorruptedException {
      String val = fields.getObject("value", String.class);
      if (val == null) {
         throw new StreamCorruptedException();
      } else {
         ClassInfo<? extends T> info = this.info;

         assert info != null;

         T t = deserializeCS(val, (Class<T>)info.getC());
         if (t == null) {
            throw new StreamCorruptedException();
         } else {
            return t;
         }
      }
   }

   public static String serializeCS(ConfigurationSerializable o) {
      YamlConfiguration y = new YamlConfiguration();
      y.set("value", o);
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(y.saveToString());
   }

   @Nullable
   public static <T extends ConfigurationSerializable> T deserializeCS(String s, Class<T> c) {
      YamlConfiguration y = new YamlConfiguration();

      try {
         y.loadFromString(s);
      } catch (InvalidConfigurationException var4) {
         return null;
      }

      Object o = y.get("value");
      return (T)(!c.isInstance(o) ? null : o);
   }

   @Nullable
   public <E extends T> E newInstance(Class<E> c) {
      assert false;

      return null;
   }

   public void deserialize(T o, Fields fields) throws StreamCorruptedException {
      assert false;
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public T deserialize(String s) {
      ClassInfo<? extends T> info = this.info;

      assert info != null;

      return deserializeCSOld(s, (Class<T>)info.getC());
   }

   @Deprecated(
      since = "2.3.0",
      forRemoval = true
   )
   @Nullable
   public static <T extends ConfigurationSerializable> T deserializeCSOld(String s, Class<T> c) {
      YamlConfiguration y = new YamlConfiguration();

      try {
         y.loadFromString(s.replace("\ufeff", "\n"));
      } catch (InvalidConfigurationException var4) {
         return null;
      }

      Object o = y.get("value");
      return (T)(!c.isInstance(o) ? null : o);
   }
}
