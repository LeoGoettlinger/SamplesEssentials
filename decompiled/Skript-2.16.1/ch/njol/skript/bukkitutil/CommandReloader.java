package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.Nullable;

public class CommandReloader {
   @Nullable
   private static Method syncCommandsMethod;

   public static boolean syncCommands(Server server) {
      if (syncCommandsMethod == null) {
         return false;
      } else {
         try {
            syncCommandsMethod.invoke(server);
            return true;
         } catch (Throwable var2) {
            if (Skript.debug()) {
               Skript.info("syncCommands failed; stack trace for debugging below");
               var2.printStackTrace();
            }

            return false;
         }
      }
   }

   static {
      try {
         syncCommandsMethod = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
         if (syncCommandsMethod != null) {
            syncCommandsMethod.setAccessible(true);
         }
      } catch (NoSuchMethodException var1) {
         if (Skript.debug()) {
            var1.printStackTrace();
         }
      }
   }
}
