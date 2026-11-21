package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Mob;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Flying;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.WaterMob;

public class Commandremove extends EssentialsCommand {
   public Commandremove() {
      super("remove");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      World world = user.getWorld();
      int radius = 0;
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         if (args.length >= 2) {
            try {
               radius = Integer.parseInt(args[1]);
            } catch (NumberFormatException var8) {
               world = this.ess.getWorld(args[1]);
            }
         }

         if (args.length >= 3) {
            radius = 0;
            world = this.ess.getWorld(args[2]);
         }

         this.parseCommand(server, user.getSource(), args, world, radius);
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = this.ess.getWorld(args[1]);
         this.parseCommand(server, sender, args, world, 0);
      }
   }

   private void parseCommand(final Server server, final CommandSource sender, final String[] args, final World world, final int radius) throws Exception {
      List<String> types = new ArrayList<>();
      List<String> customTypes = new ArrayList<>();
      if (world == null) {
         throw new TranslatableException("invalidWorld");
      } else {
         if (!args[0].contentEquals("*") && !args[0].contentEquals("all")) {
            for (String entityType : args[0].split(",")) {
               Commandremove.ToRemove toRemove;
               try {
                  toRemove = Commandremove.ToRemove.valueOf(entityType.toUpperCase(Locale.ENGLISH));
               } catch (Exception var16) {
                  try {
                     toRemove = Commandremove.ToRemove.valueOf(entityType.concat("S").toUpperCase(Locale.ENGLISH));
                  } catch (Exception var15) {
                     toRemove = Commandremove.ToRemove.CUSTOM;
                     customTypes.add(entityType);
                  }
               }

               types.add(toRemove.toString());
            }
         } else {
            types.add(0, "ALL");
         }

         this.removeHandler(sender, types, customTypes, world, radius);
      }
   }

   private void removeHandler(final CommandSource sender, final List<String> types, final List<String> customTypes, final World world, int radius) {
      int removed = 0;
      if (radius > 0) {
         radius *= radius;
      }

      ArrayList<Commandremove.ToRemove> removeTypes = new ArrayList<>();
      ArrayList<Mob> customRemoveTypes = new ArrayList<>();

      for (String s : types) {
         removeTypes.add(Commandremove.ToRemove.valueOf(s));
      }

      boolean warnUser = false;

      for (String s : customTypes) {
         Mob mobType = Mob.fromName(s);
         if (mobType == null) {
            warnUser = true;
         } else {
            customRemoveTypes.add(mobType);
         }
      }

      if (warnUser) {
         sender.sendTl("invalidMob");
      }

      for (Chunk chunk : world.getLoadedChunks()) {
         for (Entity e : chunk.getEntities()) {
            if ((radius <= 0 || !(sender.getPlayer().getLocation().distanceSquared(e.getLocation()) > radius)) && !(e instanceof HumanEntity)) {
               for (Commandremove.ToRemove toRemove : removeTypes) {
                  if ((
                        !(e instanceof Tameable)
                           || !((Tameable)e).isTamed()
                           || !(((Tameable)e).getOwner() instanceof Player) && !(((Tameable)e).getOwner() instanceof OfflinePlayer)
                           || removeTypes.contains(Commandremove.ToRemove.TAMED)
                     )
                     && (!(e instanceof LivingEntity) || e.getCustomName() == null || removeTypes.contains(Commandremove.ToRemove.NAMED))) {
                     switch (toRemove) {
                        case DROPS:
                           if (e instanceof Item) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case ARROWS:
                           if (e instanceof Projectile) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case BOATS:
                           if (e instanceof Boat) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case MINECARTS:
                           if (e instanceof Minecart) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case XP:
                           if (e instanceof ExperienceOrb) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case PAINTINGS:
                           if (e instanceof Painting) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case ITEMFRAMES:
                           if (e instanceof ItemFrame) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case ENDERCRYSTALS:
                           if (e instanceof EnderCrystal) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case HOSTILE:
                        case MONSTERS:
                           if (e instanceof Monster || e instanceof ComplexLivingEntity || e instanceof Flying || e instanceof Slime) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case PASSIVE:
                        case ANIMALS:
                           if (e instanceof Animals || e instanceof NPC || e instanceof Snowman || e instanceof WaterMob || e instanceof Ambient) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case AMBIENT:
                           if (e instanceof Flying) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case MOBS:
                           if (e instanceof Animals
                              || e instanceof NPC
                              || e instanceof Snowman
                              || e instanceof WaterMob
                              || e instanceof Monster
                              || e instanceof ComplexLivingEntity
                              || e instanceof Flying
                              || e instanceof Slime
                              || e instanceof Ambient) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case ENTITIES:
                        case ALL:
                           e.remove();
                           removed++;
                           break;
                        case CUSTOM:
                           for (Mob type : customRemoveTypes) {
                              if (e.getType() == type.getType()) {
                                 e.remove();
                                 removed++;
                              }
                           }
                           break;
                        case TAMED:
                           if (e instanceof Tameable && ((Tameable)e).isTamed()) {
                              e.remove();
                              removed++;
                           }
                           break;
                        case NAMED:
                           if (e instanceof LivingEntity && e.getCustomName() != null) {
                              e.remove();
                              removed++;
                           }
                     }
                  }
               }
            }
         }
      }

      sender.sendTl("removed", removed);
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> options = Lists.newArrayList();

         for (Commandremove.ToRemove toRemove : Commandremove.ToRemove.values()) {
            options.add(toRemove.name().toLowerCase(Locale.ENGLISH));
         }

         return options;
      } else if (args.length != 2) {
         return Collections.emptyList();
      } else {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            worlds.add(world.getName());
         }

         return worlds;
      }
   }

   private static enum ToRemove {
      DROPS,
      ARROWS,
      BOATS,
      MINECARTS,
      XP,
      PAINTINGS,
      ITEMFRAMES,
      ENDERCRYSTALS,
      HOSTILE,
      MONSTERS,
      PASSIVE,
      ANIMALS,
      AMBIENT,
      MOBS,
      ENTITIES,
      ALL,
      CUSTOM,
      TAMED,
      NAMED;
   }
}
