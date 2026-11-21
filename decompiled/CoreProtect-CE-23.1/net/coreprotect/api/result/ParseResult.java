package net.coreprotect.api.result;

import java.util.Locale;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;
import net.coreprotect.utility.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class ParseResult {
   private final String[] parse;

   public ParseResult(String[] data) {
      this.parse = data;
   }

   public int getActionId() {
      return Integer.parseInt(this.parse[7]);
   }

   public String getActionString() {
      int actionID = Integer.parseInt(this.parse[7]);
      if (this.parse.length < 13 && Integer.parseInt(this.parse[6]) == 0) {
         switch (actionID) {
            case 0:
               return "logout";
            case 1:
               return "login";
            default:
               return "unknown";
         }
      } else {
         String result = "unknown";
         if (actionID == 0) {
            result = "break";
         } else if (actionID == 1) {
            result = "place";
         } else if (actionID == 2) {
            result = "click";
         } else if (actionID == 3) {
            result = "kill";
         }

         return result;
      }
   }

   @Deprecated
   public int getData() {
      return Integer.parseInt(this.parse[6]);
   }

   public String getPlayer() {
      return this.parse[1];
   }

   @Deprecated
   public int getTime() {
      return Integer.parseInt(this.parse[0]);
   }

   public long getTimestamp() {
      return Long.parseLong(this.parse[0]) * 1000L;
   }

   public Material getType() {
      if (this.parse.length < 13) {
         return null;
      } else {
         int actionID = this.getActionId();
         int type = Integer.parseInt(this.parse[5]);
         String typeName;
         if (actionID == 3) {
            typeName = EntityUtils.getEntityType(type).name();
         } else {
            typeName = MaterialUtils.getType(type).name().toLowerCase(Locale.ROOT);
            typeName = StringUtils.nameFilter(typeName, this.getData());
         }

         return MaterialUtils.getType(typeName);
      }
   }

   public BlockData getBlockData() {
      if (this.parse.length < 13) {
         return null;
      } else {
         String blockData = this.parse[12];
         return blockData != null && blockData.length() != 0 ? Bukkit.getServer().createBlockData(blockData) : this.getType().createBlockData();
      }
   }

   public int getX() {
      return Integer.parseInt(this.parse[2]);
   }

   public int getY() {
      return Integer.parseInt(this.parse[3]);
   }

   public int getZ() {
      return Integer.parseInt(this.parse[4]);
   }

   public boolean isRolledBack() {
      return this.parse.length < 13 ? false : Integer.parseInt(this.parse[8]) == 1 || Integer.parseInt(this.parse[8]) == 3;
   }

   public String worldName() {
      return WorldUtils.getWorldName(Integer.parseInt(this.parse.length < 13 ? this.parse[5] : this.parse[9]));
   }
}
