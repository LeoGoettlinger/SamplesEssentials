package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import com.oheers.fish.gui.ConfigGui;
import java.util.Optional;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.inventorygui.GuiStorageElement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SellGui extends ConfigGui {
   private final Inventory fishInventory;

   public SellGui(@NonNull Player player, @NonNull SellGui.SellState sellState, @Nullable Inventory fishInventory) {
      super(sellState.getGuiConfig(), player);
      this.fishInventory = Optional.ofNullable(fishInventory).orElse(Bukkit.createInventory(player, 54));
      Economy economy = Economy.getInstance();
      double shopSaleValue = FishUtils.calculateInventoryWorth(this.fishInventory);
      this.addReplacement("{sell-price}", economy.getWorthFormat(shopSaleValue, true));
      double playerSaleValue = FishUtils.calculateInventoryWorth(player.getInventory());
      this.addReplacement("{sell-all-price}", economy.getWorthFormat(playerSaleValue, true));
      this.setCloseAction(close -> {
         if (MainConfig.getInstance().sellOverDrop()) {
            SellHelper.get().sell(this.fishInventory, this.player);
         }

         this.doRescue();
         return false;
      });
      this.createGui();
      Section config = this.getGuiConfig();
      if (config != null) {
         this.getGui()
            .addElement(new GuiStorageElement(FishUtils.getCharFromString(this.getGuiConfig().getString("deposit-character", "i"), 'i'), this.fishInventory));
      }
   }

   public Inventory getFishInventory() {
      return this.fishInventory;
   }

   public static enum SellState {
      NORMAL(SellMenuNormalGuiConfig::getInstance),
      CONFIRM(SellMenuConfirmGuiConfig::getInstance);

      private final Supplier<GuiConfig> configSupplier;

      private SellState(@NonNull Supplier<GuiConfig> configSupplier) {
         this.configSupplier = configSupplier;
      }

      public GuiConfig getGuiConfig() {
         return this.configSupplier.get();
      }
   }
}
