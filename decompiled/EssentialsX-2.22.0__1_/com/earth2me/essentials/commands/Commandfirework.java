package com.earth2me.essentials.commands;

import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.MobCompat;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Server;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public class Commandfirework extends EssentialsCommand {
   public Commandfirework() {
      super("firework");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         ItemStack stack = user.getItemInHand();
         if (!MaterialUtil.isFirework(stack.getType())) {
            throw new TranslatableException("holdFirework");
         } else {
            if (args[0].equalsIgnoreCase("clear")) {
               FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();
               fmeta.clearEffects();
               stack.setItemMeta(fmeta);
               user.sendTl("fireworkEffectsCleared");
            } else if (args.length <= 1 || !args[0].equalsIgnoreCase("power") && !args[0].equalsIgnoreCase("p")) {
               if ((args[0].equalsIgnoreCase("fire") || args[0].equalsIgnoreCase("f")) && user.isAuthorized("essentials.firework.fire")) {
                  int amount = 1;
                  boolean direction = false;
                  if (args.length > 1) {
                     if (NumberUtil.isInt(args[1])) {
                        int serverLimit = this.ess.getSettings().getSpawnMobLimit();
                        amount = Integer.parseInt(args[1]);
                        if (amount > serverLimit) {
                           amount = serverLimit;
                           user.sendTl("mobSpawnLimit");
                        }
                     } else {
                        direction = true;
                     }
                  }

                  for (int i = 0; i < amount; i++) {
                     Firework firework = (Firework)user.getWorld().spawnEntity(user.getLocation(), MobCompat.FIREWORK_ROCKET);
                     FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();
                     if (direction) {
                        Vector vector = user.getBase().getEyeLocation().getDirection().multiply(0.07);
                        if (fmeta.getPower() > 1) {
                           fmeta.setPower(1);
                        }

                        firework.setVelocity(vector);
                     }

                     firework.setFireworkMeta(fmeta);
                  }
               } else {
                  MetaItemStack mStack = new MetaItemStack(stack);

                  for (String arg : args) {
                     try {
                        mStack.addFireworkMeta(user.getSource(), true, arg, this.ess);
                     } catch (Exception var13) {
                        user.sendTl("fireworkSyntax");
                        throw var13;
                     }
                  }

                  if (!mStack.isValidFirework()) {
                     user.sendTl("fireworkSyntax");
                     throw new TranslatableException("fireworkColor");
                  }

                  FireworkMeta fmeta = (FireworkMeta)mStack.getItemStack().getItemMeta();
                  FireworkEffect effect = mStack.getFireworkBuilder().build();
                  if (fmeta.getEffects().size() > 0 && !user.isAuthorized("essentials.firework.multiple")) {
                     throw new TranslatableException("multipleCharges");
                  }

                  fmeta.addEffect(effect);
                  stack.setItemMeta(fmeta);
               }
            } else {
               FireworkMeta fmeta = (FireworkMeta)stack.getItemMeta();

               try {
                  int power = Integer.parseInt(args[1]);
                  fmeta.setPower(power > 3 ? 4 : power);
               } catch (NumberFormatException var12) {
                  throw new TranslatableException("invalidFireworkFormat", args[1], args[0]);
               }

               stack.setItemMeta(fmeta);
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList();
         if (!args[0].startsWith("color:")) {
            options.add("clear");
            options.add("power");
            options.add("color:");
            if (user.isAuthorized("essentials.firework.fire")) {
               options.add("fire");
            }

            return options;
         } else {
            String prefix;
            if (args[0].contains(",")) {
               prefix = args[0].substring(0, args[0].lastIndexOf(44) + 1);
            } else {
               prefix = "color:";
            }

            for (DyeColor color : DyeColor.values()) {
               options.add(prefix + color.name().toLowerCase() + ",");
            }

            return options;
         }
      } else if (args.length != 2) {
         if (args.length == 3 && args[0].startsWith("color:")) {
            return Lists.newArrayList(new String[]{"shape:star", "shape:ball", "shape:large", "shape:creeper", "shape:burst"});
         } else {
            return (List<String>)(args.length == 4 && args[0].startsWith("color:")
               ? Lists.newArrayList(new String[]{"effect:trail", "effect:twinkle", "effect:trail,twinkle", "effect:twinkle,trail"})
               : Collections.emptyList());
         }
      } else if (args[0].equals("power")) {
         return Lists.newArrayList(new String[]{"1", "2", "3", "4"});
      } else if (args[0].equals("fire")) {
         return Lists.newArrayList(new String[]{"1"});
      } else if (!args[0].startsWith("color:")) {
         return Collections.emptyList();
      } else {
         List<String> options = Lists.newArrayList();
         if (!args[1].startsWith("fade:")) {
            args[1] = "fade:";
         }

         String prefix;
         if (args[1].contains(",")) {
            prefix = args[1].substring(0, args[1].lastIndexOf(44) + 1);
         } else {
            prefix = "fade:";
         }

         for (DyeColor color : DyeColor.values()) {
            options.add(prefix + color.name().toLowerCase() + ",");
         }

         return options;
      }
   }
}
