package de.tr7zw.nbtapi.plugin.tests.entities;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NbtApiException;
import de.tr7zw.nbtapi.plugin.tests.Test;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

public class EntityCustomNbtPersistentTest implements Test {
   @Override
   public void test() throws Exception {
      if (MinecraftVersion.getVersion().getVersionId() >= MinecraftVersion.MC1_14_R1.getVersionId()) {
         if (!Bukkit.getWorlds().isEmpty()) {
            World world = (World)Bukkit.getWorlds().get(0);

            try {
               if (!world.getEntitiesByClasses(new Class[]{Animals.class, Monster.class}).isEmpty()) {
                  Entity ent = (Entity)world.getEntitiesByClasses(new Class[]{Animals.class, Monster.class}).iterator().next();
                  NBT.modifyPersistentData(ent, comp -> comp.setString("Hello", "World"));
                  NBT.modifyPersistentData(ent, comp -> {
                     if (!comp.toString().contains("Hello:\"World\"")) {
                        throw new NbtApiException("Custom Data did not save to the Entity!");
                     } else {
                        comp.removeKey("Hello");
                     }
                  });
               }
            } catch (Exception var3) {
               throw new NbtApiException("Wasn't able to use NBTEntities!", var3);
            }
         }
      }
   }
}
