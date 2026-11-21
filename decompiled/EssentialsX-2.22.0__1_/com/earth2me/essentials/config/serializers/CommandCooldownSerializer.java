package com.earth2me.essentials.config.serializers;

import com.earth2me.essentials.config.entities.CommandCooldown;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class CommandCooldownSerializer implements TypeSerializer<CommandCooldown> {
   public CommandCooldown deserialize(Type type, ConfigurationNode node) throws SerializationException {
      try {
         Pattern pattern = node.node("pattern").get(Pattern.class);
         if (node.node("value").isNull()) {
            return null;
         } else {
            Long longValue = node.node("value").getLong();
            CommandCooldown cooldown = new CommandCooldown();
            cooldown.pattern(pattern);
            cooldown.value(longValue);
            return cooldown;
         }
      } catch (SerializationException var6) {
         return null;
      }
   }

   public void serialize(Type type, @Nullable CommandCooldown obj, ConfigurationNode node) throws SerializationException {
      if (obj != null && !obj.isIncomplete()) {
         node.node("pattern").set(Pattern.class, obj.pattern());
         node.node("value").set(Long.class, obj.value());
      } else {
         node.raw(null);
      }
   }
}
