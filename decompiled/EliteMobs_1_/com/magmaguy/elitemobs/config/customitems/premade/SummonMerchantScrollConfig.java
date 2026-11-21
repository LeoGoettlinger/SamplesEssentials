package com.magmaguy.elitemobs.config.customitems.premade;

import com.magmaguy.elitemobs.config.customitems.CustomItemsConfigFields;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class SummonMerchantScrollConfig extends CustomItemsConfigFields {
   public SummonMerchantScrollConfig() {
      super(
         "summon_merchant_scroll",
         true,
         Material.PAPER,
         "&6Summon Merchant Scroll",
         new ArrayList<>(List.of("&aNeed to sell an item?", "&aRight-click to activate", "&aor yell &9Jeeves!"))
      );
      this.setEnchantments(List.of("elitemobs:summon_merchant,1"));
      this.setDropWeight("5");
   }
}
