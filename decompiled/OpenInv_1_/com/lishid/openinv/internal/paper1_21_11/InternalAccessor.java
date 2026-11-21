package com.lishid.openinv.internal.paper1_21_11;

import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.paper1_21_11.container.OpenEnderChest;
import com.lishid.openinv.internal.paper1_21_11.container.OpenInventory;
import com.lishid.openinv.util.lang.LanguageManager;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class InternalAccessor extends com.lishid.openinv.internal.paper26_1.InternalAccessor {
   public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
      super(logger, lang);
   }

   @NotNull
   @Override
   public ISpecialEnderChest createEnderChest(@NotNull Player player) {
      return new OpenEnderChest(player);
   }

   @NotNull
   @Override
   public ISpecialPlayerInventory createPlayerInventory(@NotNull Player player) {
      return new OpenInventory(player);
   }
}
