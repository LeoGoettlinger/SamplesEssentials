package com.artillexstudios.axrankmenu.utils;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.hooks.HookManager;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.reflection.ClassUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderUtils {
   @NotNull
   public static String parsePlaceholders(@NotNull Player player, @NotNull String string, @NotNull Section section) {
      LuckPerms lpApi = LuckPermsProvider.get();
      User user = lpApi.getUserManager().getUser(player.getUniqueId());
      Group playerRank = lpApi.getGroupManager().getGroup(user.getPrimaryGroup());
      if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
         string = PlaceholderAPI.setPlaceholders(player, string);
      }

      double price = section.getDouble("price");
      if (AxRankMenu.CONFIG.getBoolean("discount-ranks", false)) {
         double currentPrice = Math.max(
            AxRankMenu.RANKS.getDouble(playerRank.getName() + ".price", -1.0F),
            AxRankMenu.RANKS.getDouble(playerRank.getName().toUpperCase() + ".price", -1.0F)
         );
         if (currentPrice != -1.0) {
            price -= currentPrice;
            price = Math.max(0.0, price);
         }
      }

      string = string.replace("%price%", StringUtils.formatNumber("#,###.##", price));
      ImmutableContextSet set = lpApi.getContextManager().getStaticContext();
      string = string.replace(
         "%server%",
         (CharSequence)(section.getString("server").isEmpty() && set.getAnyValue("server").isPresent()
            ? (CharSequence)set.getAnyValue("server").get()
            : section.getString("server"))
      );
      if (HookManager.getPapi() != null) {
         string = HookManager.getPapi().parsePlaceholders(string);
      }

      return string;
   }
}
