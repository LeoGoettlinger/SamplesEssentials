package ac.grim.grimac.platform.bukkit.manager;

import ac.grim.grimac.platform.api.manager.MessagePlaceHolderManager;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.bukkit.player.BukkitPlatformPlayer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.utils.reflection.ReflectionUtils;
import me.clip.placeholderapi.PlaceholderAPI;

public class BukkitMessagePlaceHolderManager implements MessagePlaceHolderManager {
   public static final boolean hasPlaceholderAPI = ReflectionUtils.hasClass("me.clip.placeholderapi.PlaceholderAPI");

   @NotNull
   @Override
   public String replacePlaceholders(@Nullable PlatformPlayer player, @NotNull String string) {
      return !hasPlaceholderAPI
         ? string
         : PlaceholderAPI.setPlaceholders(player instanceof BukkitPlatformPlayer bukkitPlatformPlayer ? bukkitPlatformPlayer.getBukkitPlayer() : null, string);
   }
}
