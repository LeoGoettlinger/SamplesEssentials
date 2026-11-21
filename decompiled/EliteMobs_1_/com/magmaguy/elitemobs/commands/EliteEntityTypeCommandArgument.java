package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.mobproperties.MobPropertiesConfig;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.Locale;
import org.bukkit.entity.EntityType;

final class EliteEntityTypeCommandArgument extends DynamicListStringCommandArgument {
   EliteEntityTypeCommandArgument() {
      super(() -> MobPropertiesConfig.getMobProperties().keySet().stream().map(Enum::name).sorted().toList(), "<entityType>");
   }

   static EntityType parse(String input) {
      return EntityType.valueOf(input.toUpperCase(Locale.ROOT));
   }
}
