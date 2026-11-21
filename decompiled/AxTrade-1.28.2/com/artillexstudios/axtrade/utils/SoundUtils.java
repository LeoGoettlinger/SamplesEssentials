package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import net.kyori.adventure.key.InvalidKeyException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundUtils {
   public static void playSound(@NotNull Player player, @Nullable String route) {
      if (route != null) {
         if (!AxTrade.LANG.getString("sounds." + route, "").isBlank()) {
            try {
               player.playSound(player, AxTrade.LANG.getString("sounds." + route), 1.0F, 1.0F);
            } catch (InvalidKeyException var3) {
               Bukkit.getConsoleSender()
                  .sendMessage(
                     StringUtils.formatToString(
                        "&#FF0000[AxTrade] The sound %sound% does not exist, section: %section%!"
                           .replace("%sound%", AxTrade.LANG.getString("sounds." + route))
                           .replace("%section%", route)
                     )
                  );
            }
         }
      }
   }
}
