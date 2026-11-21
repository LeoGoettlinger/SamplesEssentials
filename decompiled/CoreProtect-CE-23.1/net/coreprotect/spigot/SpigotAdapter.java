package net.coreprotect.spigot;

import java.util.regex.Matcher;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Util;
import org.bukkit.command.CommandSender;

public class SpigotAdapter implements SpigotInterface {
   public static SpigotInterface ADAPTER;
   public static final int SPIGOT_UNAVAILABLE = 0;
   public static final int SPIGOT_V1_13 = 13;
   public static final int SPIGOT_V1_14 = 14;
   public static final int SPIGOT_V1_15 = 15;
   public static final int SPIGOT_V1_16 = 16;
   public static final int SPIGOT_V1_17 = 17;
   public static final int SPIGOT_V1_18 = 18;
   public static final int SPIGOT_V1_19 = 19;
   public static final int SPIGOT_V1_20 = 20;
   public static final int SPIGOT_V1_21 = 21;

   public static void loadAdapter() {
      int spigotVersion = ConfigHandler.SERVER_VERSION;
      if (!ConfigHandler.isSpigot) {
         spigotVersion = 0;
      }

      switch (spigotVersion) {
         case 0:
            ADAPTER = new SpigotAdapter();
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 13:
         case 14:
         case 15:
         case 16:
         case 17:
         case 18:
         case 19:
         case 20:
         case 21:
         default:
            ADAPTER = new SpigotHandler();
      }
   }

   @Override
   public void addHoverComponent(Object message, String[] data) {
   }

   @Override
   public void setHoverEvent(Object message, String text) {
   }

   @Override
   public void sendComponent(CommandSender sender, String string, String bypass) {
      StringBuilder message = new StringBuilder();
      Matcher matcher = Util.tagParser.matcher(string);

      while (matcher.find()) {
         String value = matcher.group(1);
         if (value != null) {
            String[] data = value.split("\\|", 3);
            if (data[0].equals("COMMAND") || data[0].equals("POPUP")) {
               message.append(data[2]);
            }
         } else {
            message.append(matcher.group(2));
         }
      }

      if (bypass != null) {
         message.append(bypass);
      }

      Chat.sendMessage(sender, message.toString());
   }

   public String processComponent(String component) {
      return component.replace("<PIPE/>", "|");
   }
}
