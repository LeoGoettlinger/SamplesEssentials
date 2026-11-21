package com.magmaguy.elitemobs.config.custombosses.premade;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class TestCustomBossConfig extends CustomBossesConfigFields {
   public TestCustomBossConfig() {
      super("test_boss", EntityType.ZOMBIE, true, "$eventBossLevel &6Test boss", "dynamic");
      this.setTimeout(10);
      this.setHealthMultiplier(2.0);
      this.setDamageMultiplier(0.5);
      this.setHelmet(new ItemStack(Material.GOLDEN_HELMET));
      this.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
      this.setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
      this.setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
      this.setMainHand(new ItemStack(Material.GOLDEN_AXE));
      this.setOffHand(new ItemStack(Material.SHIELD));
      this.setPowers(new ArrayList<>(List.of("invulnerability_knockback.yml")));
      this.setSpawnMessage("A test boss has been spawned!");
      this.setDeathMessage("A test boss has been slain by $players!");
      this.setDeathMessages(
         new ArrayList<>(
            List.of(
               "&e&l---------------------------------------------",
               "&eThe Test Boss has been debugged!",
               "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
               "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
               "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
               "&aSlayers: $players",
               "&e&l---------------------------------------------"
            )
         )
      );
      this.setEscapeMessage("A test boss entity has escaped!");
      this.setLocationMessage("Test entity: $distance");
      this.setUniqueLootList(Collections.singletonList("magmaguys_toothpick.yml:1"));
      this.setTrails(Collections.singletonList(Particle.BLOCK_MARKER.toString()));
      this.setOnDamageMessages(Collections.singletonList("I've hit you!"));
      this.setOnDamagedMessages(Collections.singletonList("I've been hit!"));
      this.setOnDeathCommands(Collections.singletonList("say $players has killed $name! That was level $level!"));
      this.setAnnouncementPriority(3);
   }
}
