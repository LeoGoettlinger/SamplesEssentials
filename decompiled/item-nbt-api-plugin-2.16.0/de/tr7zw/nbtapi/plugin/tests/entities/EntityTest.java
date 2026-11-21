package de.tr7zw.nbtapi.plugin.tests.entities;

import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public class EntityTest implements Test {
   @Override
   public void test() throws Exception {
      if (!Bukkit.getWorlds().isEmpty()) {
         World world = (World)Bukkit.getWorlds().get(0);

         try {
            if (!world.getEntitiesByClasses(new Class[]{Animals.class, Monster.class}).isEmpty()) {
               NBTEntity nbte = new NBTEntity((Entity)world.getEntitiesByClasses(new Class[]{Animals.class, Monster.class}).iterator().next());
               nbte.setString("INVALIDEKEY", "test");
            }
         } catch (Exception var3) {
            throw new NbtApiException("Wasn't able to use NBTEntities!", var3);
         }
      }
   }
}
