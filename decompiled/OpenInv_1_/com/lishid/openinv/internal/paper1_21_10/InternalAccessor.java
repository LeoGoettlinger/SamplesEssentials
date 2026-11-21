package com.lishid.openinv.internal.paper1_21_10;

import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.paper1_21_10.container.OpenInventory;
import com.lishid.openinv.internal.paper1_21_10.player.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InternalAccessor extends com.lishid.openinv.internal.paper1_21_11.InternalAccessor {
   @NotNull
   private final PlayerManager manager;

   public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
      super(logger, lang);
      this.manager = new PlayerManager(logger);
   }

   @NotNull
   public PlayerManager getPlayerManager() {
      return this.manager;
   }

   @NotNull
   @Override
   public ISpecialPlayerInventory createPlayerInventory(@NotNull Player player) {
      return new OpenInventory(player);
   }
}
