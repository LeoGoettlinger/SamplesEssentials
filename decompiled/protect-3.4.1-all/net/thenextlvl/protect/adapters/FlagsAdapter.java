package net.thenextlvl.protect.adapters;

import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.protect.ProtectPlugin;
import net.thenextlvl.protect.flag.Flag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FlagsAdapter implements TagAdapter<Map<Flag<?>, Object>> {
   private final ProtectPlugin plugin;

   public FlagsAdapter(ProtectPlugin plugin) {
      this.plugin = plugin;
   }

   public Map<Flag<?>, Object> deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      HashMap<Flag<?>, Object> flags = new HashMap<>();
      compound.forEach((key, value) -> {
         Key namespace = Key.key(key);
         Flag<Object> flag = this.plugin.flagRegistry().<Object>getFlag(namespace).orElse(null);
         if (flag != null) {
            flags.put(flag, context.deserialize(value, flag.type()));
         } else {
            this.plugin.getComponentLogger().error("Unknown flag: {}", key);
         }
      });
      return flags;
   }

   public Tag serialize(Map<Flag<?>, Object> flags, TagSerializationContext context) {
      CompoundTag.Builder tag = CompoundTag.builder();
      flags.forEach((flag, value) -> {
         if (value != null) {
            Tag serialized = context.serialize(value, flag.type());
            tag.put(flag.key().asString(), serialized);
         }
      });
      return tag.build();
   }
}
