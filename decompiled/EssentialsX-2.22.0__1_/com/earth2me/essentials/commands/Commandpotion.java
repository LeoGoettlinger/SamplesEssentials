package com.earth2me.essentials.commands;

import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.Potions;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.StringUtil;
import com.earth2me.essentials.utils.VersionUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Commandpotion extends EssentialsCommand {
   public Commandpotion() {
      super("potion");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ItemStack stack = user.getItemInHand();
      if (args.length == 0) {
         Set<String> potionslist = new TreeSet<>();

         for (Entry<String, PotionEffectType> entry : Potions.entrySet()) {
            String potionName = entry.getValue().getName().toLowerCase(Locale.ENGLISH);
            if (potionslist.contains(potionName) || user.isAuthorized("essentials.potions." + potionName)) {
               potionslist.add(entry.getKey());
            }
         }

         throw new NotEnoughArgumentsException(user.playerTl("potions", StringUtil.joinList(potionslist.toArray())));
      } else {
         boolean holdingPotion = stack.getType() == Material.POTION;
         if (!holdingPotion && VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_9_R01)) {
            holdingPotion = stack.getType() == Material.SPLASH_POTION
               || stack.getType() == Material.LINGERING_POTION
               || stack.getType() == Material.TIPPED_ARROW;
         }

         if (!holdingPotion) {
            throw new TranslatableException("holdPotion");
         } else {
            PotionMeta pmeta = (PotionMeta)stack.getItemMeta();
            if (args[0].equalsIgnoreCase("clear")) {
               pmeta.clearCustomEffects();
               stack.setItemMeta(pmeta);
            } else if (args[0].equalsIgnoreCase("apply") && user.isAuthorized("essentials.potion.apply")) {
               for (PotionEffect effect : pmeta.getCustomEffects()) {
                  effect.apply(user.getBase());
               }
            } else {
               if (args.length < 3) {
                  throw new NotEnoughArgumentsException();
               }

               MetaItemStack mStack = new MetaItemStack(stack);

               for (String arg : args) {
                  mStack.addPotionMeta(user.getSource(), true, arg, this.ess);
               }

               if (!mStack.completePotion()) {
                  user.sendTl("invalidPotion");
                  throw new NotEnoughArgumentsException();
               }

               pmeta = (PotionMeta)mStack.getItemStack().getItemMeta();
               stack.setItemMeta(pmeta);
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList();
         options.add("clear");
         if (user.isAuthorized("essentials.potion.apply")) {
            options.add("apply");
         }

         for (Entry<String, PotionEffectType> entry : Potions.entrySet()) {
            String potionName = entry.getValue().getName().toLowerCase(Locale.ENGLISH);
            if (user.isAuthorized("essentials.potions." + potionName)) {
               options.add("effect:" + entry.getKey());
            }
         }

         return options;
      } else if (args.length == 2 && args[0].startsWith("effect:")) {
         return Lists.newArrayList(new String[]{"power:1", "power:2", "power:3", "power:4", "amplifier:0", "amplifier:1", "amplifier:2", "amplifier:3"});
      } else if (args.length == 3 && args[0].startsWith("effect:")) {
         List<String> options = Lists.newArrayList();

         for (String duration : COMMON_DURATIONS) {
            options.add("duration:" + duration);
         }

         return options;
      } else {
         return (List<String>)(args.length == 4 && args[0].startsWith("effect:")
            ? Lists.newArrayList(new String[]{"splash:true", "splash:false"})
            : Collections.emptyList());
      }
   }
}
