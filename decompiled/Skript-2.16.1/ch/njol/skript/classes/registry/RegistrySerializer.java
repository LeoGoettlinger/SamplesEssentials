package ch.njol.skript.classes.registry;

import ch.njol.skript.classes.Serializer;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

public class RegistrySerializer<R extends Keyed> extends Serializer<R> {
   private final Registry<R> registry;

   public RegistrySerializer(Registry<R> registry) {
      this.registry = registry;
   }

   @NotNull
   public Fields serialize(R o) {
      Fields fields = new Fields();
      fields.putObject("name", o.getKey().toString());
      return fields;
   }

   protected R deserialize(Fields fields) throws StreamCorruptedException {
      String name = fields.getAndRemoveObject("name", String.class);

      assert name != null;

      NamespacedKey namespacedKey;
      if (!name.contains(":")) {
         namespacedKey = NamespacedKey.minecraft(name);
      } else {
         namespacedKey = NamespacedKey.fromString(name);
      }

      if (namespacedKey == null) {
         throw new StreamCorruptedException("Invalid namespacedkey: " + name);
      } else {
         R object = (R)this.registry.get(namespacedKey);
         if (object == null) {
            throw new StreamCorruptedException("Invalid object from registry: " + namespacedKey);
         } else {
            return object;
         }
      }
   }

   @Override
   public boolean mustSyncDeserialization() {
      return false;
   }

   @Override
   protected boolean canBeInstantiated() {
      return false;
   }

   public void deserialize(R o, Fields f) {
      throw new UnsupportedOperationException();
   }
}
