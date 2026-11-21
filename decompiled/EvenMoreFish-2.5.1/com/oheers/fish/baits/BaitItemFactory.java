package com.oheers.fish.baits;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.items.config.DisplayNameItemConfig;
import com.oheers.fish.items.config.LoreItemConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BaitItemFactory {
   private final String baitId;
   private final List<IRarity> rarities;
   private final List<IFish> fish;
   private final YamlDocument config;

   public BaitItemFactory(String baitId, List<IRarity> rarities, List<IFish> fish, YamlDocument config) {
      this.baitId = baitId;
      this.rarities = rarities;
      this.fish = fish;
      this.config = config;
   }

   public ItemFactory createFactory() {
      ItemFactory factory = ItemFactory.itemFactory(this.config);
      DisplayNameItemConfig displayNameConfig = factory.getItemConfig(DisplayNameItemConfig.class);
      if (displayNameConfig != null) {
         displayNameConfig.setEnabled(true);
         displayNameConfig.setDefault("<yellow>" + this.baitId);
      }

      LoreItemConfig loreConfig = factory.getItemConfig(LoreItemConfig.class);
      if (loreConfig != null) {
         loreConfig.setEnabled(true);
         loreConfig.setTransformer(this::createBoostLore);
      }

      factory.setFinalChanges(item -> {
         item.setAmount(this.config.getInt("drop-quantity", 1));
         BaitNBTManager.applyBaitNBT(item, this.baitId);
      });
      return factory;
   }

   @NonNull
   private List<Component> createBoostLore(@Nullable List<Component> original) {
      EMFListMessage lore = this.getBaseLoreTemplate();
      lore.setVariableWithListInsertion("{boosts}", this.createBoostsVariable());
      lore.setVariableWithListInsertion("{lore}", original == null ? EMFListMessage.empty() : original);
      lore.setVariable("{bait_theme}", Component.empty());
      return lore.getComponentListMessage();
   }

   private EMFListMessage getBaseLoreTemplate() {
      return ConfigMessage.BAIT_BAIT_LORE.getMessage().toListMessage();
   }

   @NonNull
   private EMFMessage createBoostsVariable() {
      Logging.debug("Creating {boosts} replacement for " + this.baitId);
      List<Component> message = new ArrayList<>();
      message.addAll(this.appendRarityBoosts().getComponentListMessage());
      message.addAll(this.appendFishBoosts().getComponentListMessage());
      return EMFListMessage.ofList(message);
   }

   private EMFMessage appendRarityBoosts() {
      if (this.rarities.isEmpty()) {
         Logging.debug("No rarities available.");
         return EMFListMessage.empty();
      } else {
         Logging.debug("Adding rarities to replacement.");
         EMFMessage boost = this.rarities.size() > 1 ? ConfigMessage.BAIT_BOOSTS_RARITIES.getMessage() : ConfigMessage.BAIT_BOOSTS_RARITY.getMessage();
         boost.setAmount(this.rarities.size());
         return boost;
      }
   }

   private EMFMessage appendFishBoosts() {
      if (this.fish.isEmpty()) {
         Logging.debug("No fish available.");
         return EMFListMessage.empty();
      } else {
         Logging.debug("Adding fish to replacement.");
         EMFMessage boost = ConfigMessage.BAIT_BOOSTS_FISH.getMessage();
         boost.setAmount(this.fish.size());
         return boost;
      }
   }
}
