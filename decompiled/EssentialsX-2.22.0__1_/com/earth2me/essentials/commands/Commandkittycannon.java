package com.earth2me.essentials.commands;

import com.earth2me.essentials.Mob;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.RegistryUtil;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Ocelot.Type;

public class Commandkittycannon extends EssentialsCommand {
   private static final Random random = new Random();

   public Commandkittycannon() {
      super("kittycannon");
   }

   private static Ocelot spawnOcelot(final Server server, final User user) throws Mob.MobException {
      Ocelot ocelot = (Ocelot)Mob.OCELOT.spawn(user.getWorld(), server, user.getBase().getEyeLocation());
      Object[] values = RegistryUtil.values(Type.class);
      int i = random.nextInt(values.length);
      ocelot.setCatType((Type)values[i]);
      ((Tameable)ocelot).setTamed(true);
      ocelot.setBaby();
      ocelot.setVelocity(user.getBase().getEyeLocation().getDirection().multiply(2));
      return ocelot;
   }

   private static Entity spawnCat(final Server server, final User user) throws Mob.MobException {
      Cat cat = (Cat)Mob.CAT.spawn(user.getWorld(), server, user.getBase().getEyeLocation());
      Object[] values = RegistryUtil.values(org.bukkit.entity.Cat.Type.class);
      int i = random.nextInt(values.length);
      cat.setCatType((org.bukkit.entity.Cat.Type)values[i]);
      cat.setTamed(true);
      cat.setBaby();
      cat.setVelocity(user.getBase().getEyeLocation().getDirection().multiply(2));
      return cat;
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      Entity ocelot = (Entity)(Mob.CAT.getType() == null ? spawnOcelot(server, user) : spawnCat(server, user));
      this.ess.scheduleSyncDelayedTask(() -> {
         Location loc = ocelot.getLocation();
         ocelot.remove();
         loc.getWorld().createExplosion(loc, 0.0F);
      }, 20L);
   }
}
