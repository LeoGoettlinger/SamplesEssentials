package net.coreprotect.paper;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Sign;

public class Paper_v1_17 extends PaperHandler {
   @Override
   public String getLine(Sign sign, int line) {
      return line >= 4 ? "" : LegacyComponentSerializer.legacySection().serialize(sign.line(line));
   }
}
