package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class TheLivingDeadConfig extends CustomBossesConfigFields {
   public TheLivingDeadConfig() {
      super("the_living_dead", EntityType.HUSK, true, "$reinforcementLevel <g:#808080:#D3D3D3>The Living Dead</g>", "dynamic");
      this.setHelmet(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_HELMET));
      this.setChestplate(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_CHESTPLATE));
      this.setLeggings(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_LEGGINGS));
      this.setBoots(ItemStackGenerator.generateItemStack(Material.CHAINMAIL_BOOTS));
      this.setPowers(new ArrayList<>(List.of("attack_fire.lua", "corpse.lua", "ground_pound.yml")));
      this.setFollowDistance(100);
   }
}
