package dev.cwhead.GravesX.compatibility;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class CompatibilitySoundEnum {
   public static Sound valueOf(String soundName) {
      try {
         Method method = Sound.class.getMethod("valueOf", String.class);
         return (Sound)method.invoke(null, soundName);
      } catch (NoSuchMethodException var2) {
         Bukkit.getServer().getLogger().severe(soundName + " does not exist in sound enum.");
         var2.printStackTrace();
      } catch (Exception var3) {
         Bukkit.getServer().getLogger().severe("An issue occurred while retrieving sound enum " + soundName + ".");
         var3.printStackTrace();
      }

      return null;
   }
}
