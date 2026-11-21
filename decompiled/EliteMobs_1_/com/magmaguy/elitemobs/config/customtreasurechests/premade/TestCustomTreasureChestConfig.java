package com.magmaguy.elitemobs.config.customtreasurechests.premade;

import com.magmaguy.elitemobs.config.customtreasurechests.CustomTreasureChestConfigFields;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class TestCustomTreasureChestConfig extends CustomTreasureChestConfigFields {
   public TestCustomTreasureChestConfig() {
      super(
         "test_custom_treasure_config",
         true,
         Material.CHEST,
         BlockFace.NORTH,
         0,
         null,
         TreasureChest.DropStyle.SINGLE,
         1,
         List.of("magmaguys_toothpick.yml:1"),
         0.5,
         List.of("test_boss.yml:1"),
         0L,
         null,
         List.of("BARRIER")
      );
   }
}
