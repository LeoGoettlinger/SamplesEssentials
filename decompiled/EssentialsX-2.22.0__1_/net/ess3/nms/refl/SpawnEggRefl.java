package net.ess3.nms.refl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnEggRefl {
   private EntityType type;

   public SpawnEggRefl(final EntityType type) {
      this.type = type;
   }

   public static SpawnEggRefl fromItemStack(final ItemStack item) throws Exception {
      if (item == null) {
         throw new IllegalArgumentException("Item cannot be null");
      } else if (item.getType() != Material.MONSTER_EGG) {
         throw new IllegalArgumentException("Item is not a monster egg");
      } else {
         Class<?> NMSItemStackClass = ReflUtil.getNMSClass("ItemStack");
         Class<?> craftItemStackClass = ReflUtil.getOBCClass("inventory.CraftItemStack");
         Method asNMSCopyMethod = ReflUtil.getMethodCached(craftItemStackClass, "asNMSCopy", ItemStack.class);
         Object stack = asNMSCopyMethod.invoke(null, item);
         Object tagCompound = ReflUtil.getMethodCached(NMSItemStackClass, "getTag").invoke(stack);
         if (tagCompound != null) {
            Method tagGetCompound = ReflUtil.getMethodCached(tagCompound.getClass(), "getCompound", String.class);
            Object entityTag = tagGetCompound.invoke(tagCompound, "EntityTag");
            Method tagGetString = ReflUtil.getMethodCached(entityTag.getClass(), "getString", String.class);
            String idString = (String)tagGetString.invoke(entityTag, "id");
            if (ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_11_R1)) {
               idString = idString.split("minecraft:")[1];
            }

            EntityType type = EntityType.fromName(idString);
            if (type != null) {
               return new SpawnEggRefl(type);
            } else {
               throw new IllegalArgumentException("Unable to parse type from item");
            }
         } else {
            throw new IllegalArgumentException("Item is lacking tag compound");
         }
      }
   }

   public EntityType getSpawnedType() {
      return this.type;
   }

   public void setSpawnedType(final EntityType type) {
      if (type.isAlive()) {
         this.type = type;
      }
   }

   @Override
   public String toString() {
      return "SPAWN EGG{" + this.getSpawnedType() + "}";
   }

   public ItemStack toItemStack() throws Exception {
      return this.toItemStack(1);
   }

   public ItemStack toItemStack(final int amount) throws Exception {
      ItemStack item = new ItemStack(Material.MONSTER_EGG, amount);
      Class<?> craftItemStackClass = ReflUtil.getOBCClass("inventory.CraftItemStack");
      Method asNMSCopyMethod = ReflUtil.getMethodCached(craftItemStackClass, "asNMSCopy", ItemStack.class);
      Class<?> NMSItemStackClass = ReflUtil.getNMSClass("ItemStack");
      Object stack = asNMSCopyMethod.invoke(null, item);
      Object tagCompound = ReflUtil.getMethodCached(NMSItemStackClass, "getTag").invoke(stack);
      Class<?> NBTTagCompoundClass = ReflUtil.getNMSClass("NBTTagCompound");
      Constructor<?> NBTTagCompoundConstructor = ReflUtil.getConstructorCached(NBTTagCompoundClass);
      if (tagCompound == null) {
         tagCompound = NBTTagCompoundConstructor.newInstance();
      }

      Object id = NBTTagCompoundConstructor.newInstance();
      Method tagSetString = ReflUtil.getMethodCached(NBTTagCompoundClass, "setString", String.class, String.class);
      String idString = this.type.getName();
      if (ReflUtil.getNmsVersionObject().isHigherThanOrEqualTo(ReflUtil.V1_11_R1)) {
         idString = "minecraft:" + idString;
      }

      tagSetString.invoke(id, "id", idString);
      Method tagSetTag = ReflUtil.getMethodCached(NBTTagCompoundClass, "set", String.class, NBTTagCompoundClass.getSuperclass());
      tagSetTag.invoke(tagCompound, "EntityTag", id);
      Method stackSetTag = ReflUtil.getMethodCached(NMSItemStackClass, "setTag", NBTTagCompoundClass);
      stackSetTag.invoke(stack, tagCompound);
      Method asBukkitCopyMethod = ReflUtil.getMethodCached(craftItemStackClass, "asBukkitCopy", NMSItemStackClass);
      return (ItemStack)asBukkitCopyMethod.invoke(null, stack);
   }
}
