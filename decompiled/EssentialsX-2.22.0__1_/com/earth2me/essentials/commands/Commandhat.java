package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.TriState;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Commandhat extends EssentialsCommand {
   public static final String PERM_PREFIX = "essentials.hat.prevent-type.";

   public Commandhat() {
      super("hat");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0 || !args[0].contains("rem") && !args[0].contains("off") && !args[0].equalsIgnoreCase("0")) {
         ItemStack hand = Inventories.getItemInMainHand(user.getBase());
         if (hand != null && hand.getType() != Material.AIR) {
            TriState wildcard = user.isAuthorizedExact("essentials.hat.prevent-type.*");
            TriState material = user.isAuthorizedExact("essentials.hat.prevent-type." + hand.getType().name().toLowerCase());
            if ((wildcard != TriState.TRUE || material == TriState.FALSE) && (wildcard == TriState.TRUE || material != TriState.TRUE)) {
               if (hand.getType().getMaxDurability() != 0) {
                  user.sendTl("hatArmor");
               } else {
                  PlayerInventory inv = user.getBase().getInventory();
                  ItemStack head = inv.getHelmet();
                  if (VersionUtil.getServerBukkitVersion().isHigherThan(VersionUtil.v1_9_4_R01)
                     && head != null
                     && head.getEnchantments().containsKey(Enchantment.BINDING_CURSE)
                     && !user.isAuthorized("essentials.hat.ignore-binding")) {
                     user.sendTl("hatCurse");
                  } else {
                     inv.setHelmet(hand);
                     Inventories.setItemInMainHand(user.getBase(), head);
                     user.sendTl("hatPlaced");
                  }
               }
            } else {
               user.sendTl("hatFail");
            }
         } else {
            user.sendTl("hatFail");
         }
      } else {
         PlayerInventory inv = user.getBase().getInventory();
         ItemStack head = inv.getHelmet();
         if (head == null || head.getType() == Material.AIR) {
            user.sendTl("hatEmpty");
         } else if (VersionUtil.getServerBukkitVersion().isHigherThan(VersionUtil.v1_9_4_R01)
            && head.getEnchantments().containsKey(Enchantment.BINDING_CURSE)
            && !user.isAuthorized("essentials.hat.ignore-binding")) {
            user.sendTl("hatCurse");
         } else {
            ItemStack air = new ItemStack(Material.AIR);
            inv.setHelmet(air);
            Inventories.addItem(user.getBase(), head);
            user.sendTl("hatRemoved");
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? Lists.newArrayList(new String[]{"remove", "wear"}) : Collections.emptyList());
   }
}
