package com.oheers.fish.gui.guis;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.config.gui.impl.ApplyBaitsMenuGuiConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.inventorygui.GuiStorageElement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ApplyBaitsGui extends ConfigGui {
   private final Inventory baitInventory;

   public ApplyBaitsGui(@NonNull Player player, @Nullable Inventory baitInventory) {
      super(ApplyBaitsMenuGuiConfig.getInstance(), player);
      this.baitInventory = Optional.ofNullable(baitInventory).orElse(Bukkit.createInventory(player, 54));
      this.setCloseAction(close -> {
         this.processBaits();
         this.doRescue();
         return false;
      });
      this.createGui();
      Section config = this.getGuiConfig();
      if (config != null) {
         this.getGui()
            .addElement(new GuiStorageElement(FishUtils.getCharFromString(this.getGuiConfig().getString("bait-character", "b"), 'b'), this.baitInventory));
      }
   }

   private void processBaits() {
      ItemStack handItem = this.player.getInventory().getItemInMainHand();
      if (Checks.canUseRod(handItem)) {
         boolean changedRod = false;
         List<String> ignoredBaits = new ArrayList<>();

         for (ItemStack item : this.baitInventory.getContents()) {
            if (item != null && !item.isEmpty()) {
               BaitHandler bait = BaitManager.getInstance().getBait(item);
               if (bait != null && !ignoredBaits.contains(bait.getId())) {
                  ApplicationResult result;
                  try {
                     result = BaitNBTManager.applyBaitedRodNBT(handItem, bait, item.getAmount());
                     EvenMoreFish.getInstance().getMetricsManager().incrementBaitsApplied(item.getAmount());
                  } catch (MaxBaitReachedException var12) {
                     EMFMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
                     message.setBait(bait);
                     message.send(this.player);
                     ignoredBaits.add(bait.getId());
                     continue;
                  } catch (MaxBaitsReachedException var13) {
                     ConfigMessage.BAITS_MAXED.getMessage().send(this.player);
                     return;
                  }

                  this.baitInventory.remove(item);
                  handItem = result.fishingRod();
                  changedRod = true;
               }
            }
         }

         if (changedRod) {
            this.player.getInventory().setItemInMainHand(handItem);
         }
      }
   }
}
