package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.sort.SortType;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.config.gui.impl.BaitsMenuGuiConfig;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.utils.CooldownHelper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.evenmorefish.fish.libs.boostedyaml.block.implementation.Section;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentListMessage;
import org.evenmorefish.fish.libs.daisylib.messages.message.ComponentMessage;
import org.evenmorefish.fish.libs.inventorygui.DynamicGuiElement;
import org.evenmorefish.fish.libs.inventorygui.GuiElementGroup;
import org.evenmorefish.fish.libs.inventorygui.StaticGuiElement;
import org.jspecify.annotations.NonNull;

public class BaitsGui extends ConfigGui {
   private final CooldownHelper confirmation = CooldownHelper.create();
   private final CooldownHelper cooldown = CooldownHelper.create();
   private final SortType sortType;

   public BaitsGui(@NonNull HumanEntity player) {
      super(BaitsMenuGuiConfig.getInstance(), player);
      this.createGui();
      Section config = this.getGuiConfig();
      if (config != null) {
         this.sortType = FishUtils.getEnumValue(SortType.class, config.getString("sort-type"), SortType.ALPHABETICAL);
         this.getGui().addElements(this.getBaitsGroup(config));
      } else {
         this.sortType = SortType.ALPHABETICAL;
      }
   }

   private DynamicGuiElement getBaitsGroup(Section section) {
      char character = FishUtils.getCharFromString(section.getString("bait-character", "b"), 'b');
      return new DynamicGuiElement(character, who -> {
         GuiElementGroup group = new GuiElementGroup(character);
         this.sortType.sort(BaitManager.getInstance().getItemMap().values()).forEach(bait -> group.addElement(this.createBaitElement(character, bait)));
         return group;
      });
   }

   private StaticGuiElement createBaitElement(char character, @NonNull BaitHandler bait) {
      return new StaticGuiElement(character, this.createBaitItem(bait), click -> {
         UUID uuid = this.player.getUniqueId();
         if (this.cooldown.hasCooldown(uuid)) {
            return true;
         } else if (this.requireConfirmation(uuid)) {
            ConfigMessage.BAIT_CONFIRM_PURCHASE.getMessage().send(this.player);
            return true;
         } else {
            bait.attemptPurchase(this.player);
            this.cooldown.applyCooldown(uuid, Duration.ofMillis(250L));
            return true;
         }
      });
   }

   private List<String> getPurchaseLoreFormat() {
      return this.getGuiConfig().getStringList("purchase-lore");
   }

   private ItemStack createBaitItem(@NonNull BaitHandler bait) {
      ItemStack item = bait.create(this.player);
      item.editMeta(meta -> this.applyLore(meta, bait));
      return item;
   }

   private void applyLore(@NonNull ItemMeta meta, @NonNull BaitHandler bait) {
      Economy economy = bait.getEconomy();
      if (economy != null && !economy.isEmpty()) {
         List<String> loreFormat = this.getPurchaseLoreFormat();
         if (!loreFormat.isEmpty()) {
            ComponentListMessage purchaseLore = ComponentMessage.componentMessage(loreFormat)
               .replace("{quantity}", bait.getPurchaseQuantity())
               .replace("{price}", economy.getWorthFormat(bait.getPurchasePrice(), false))
               .replace("{bait}", bait.getDisplayName());
            meta.lore(purchaseLore.get());
         }
      }
   }

   private boolean requireConfirmation(@NonNull UUID uuid) {
      if (!this.confirmation.hasCooldown(uuid)) {
         this.confirmation.applyCooldown(uuid, Duration.ofSeconds(5L));
         return true;
      } else {
         this.confirmation.removeCooldown(uuid);
         return false;
      }
   }
}
