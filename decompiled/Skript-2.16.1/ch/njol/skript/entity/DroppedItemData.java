package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DroppedItemData extends EntityData<Item> {
   private static final boolean HAS_JAVA_CONSUMER_DROP = Skript.methodExists(World.class, "dropItem", Location.class, ItemStack.class, Consumer.class);
   @Nullable
   private static Method BUKKIT_CONSUMER_DROP;
   private static final Adjective m_adjective;
   @Nullable
   private ItemType[] types = null;

   public DroppedItemData() {
   }

   public DroppedItemData(@Nullable ItemType[] types) {
      this.types = types;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (exprs.length > 0 && exprs[0] != null) {
         this.types = (ItemType[])exprs[0].getAll();

         for (ItemType type : this.types) {
            if (!type.getMaterial().isItem()) {
               Skript.error("'" + type + "' cannot represent a dropped item");
               return false;
            }
         }
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Item> entityClass, @Nullable Item item) {
      if (item != null) {
         ItemStack itemStack = item.getItemStack();
         this.types = new ItemType[]{new ItemType(itemStack)};
      }

      return true;
   }

   public void set(Item item) {
      if (this.types != null) {
         ItemType itemType = CollectionUtils.getRandom(this.types);

         assert itemType != null;

         ItemStack stack = itemType.getItem().getRandom();

         assert stack != null;

         item.setItemStack(stack);
      }
   }

   protected boolean match(Item item) {
      if (this.types != null) {
         for (ItemType itemType : this.types) {
            if (itemType.isOfType(item.getItemStack())) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public Class<? extends Item> getType() {
      return Item.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new DroppedItemData();
   }

   @Override
   protected int hashCode_i() {
      return Arrays.hashCode((Object[])this.types);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof DroppedItemData other ? Arrays.equals((Object[])this.types, (Object[])other.types) : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> otherData) {
      if (otherData instanceof DroppedItemData other) {
         return this.types == null ? true : other.types != null && ItemType.isSubset(this.types, other.types);
      } else {
         return false;
      }
   }

   @Override
   public String toString(int flags) {
      if (this.types == null) {
         return super.toString(flags);
      } else {
         int gender = this.types[0].getTypes().get(0).getGender();
         return Noun.getArticleWithSpace(gender, flags) + m_adjective.toString(gender, flags) + " " + Classes.toString(this.types, flags & -7, false);
      }
   }

   @Override
   public boolean canSpawn(@Nullable World world) {
      return this.types != null && this.types.length > 0 && world != null;
   }

   @Nullable
   public Item spawn(Location location, @Nullable Consumer<Item> consumer) {
      World world = location.getWorld();
      if (!this.canSpawn(world)) {
         return null;
      } else {
         assert this.types != null && this.types.length > 0;

         ItemType itemType = CollectionUtils.getRandom(this.types);

         assert itemType != null;

         ItemStack stack = itemType.getItem().getRandom();

         assert stack != null;

         Item item;
         if (consumer == null) {
            item = world.dropItem(location, stack);
         } else if (HAS_JAVA_CONSUMER_DROP) {
            item = world.dropItem(location, stack, consumer);
         } else if (BUKKIT_CONSUMER_DROP != null) {
            try {
               item = (Item)BUKKIT_CONSUMER_DROP.invoke(world, location, stack, consumer::accept);
            } catch (IllegalAccessException | InvocationTargetException var8) {
               if (Skript.testing()) {
                  Skript.exception(var8, "Can't spawn " + this.getName());
               }

               return null;
            }
         } else {
            item = world.dropItem(location, stack);
            consumer.accept(item);
         }

         return item;
      }
   }

   static {
      EntityData.register(DroppedItemData.class, "dropped item", Item.class, "dropped item");

      try {
         BUKKIT_CONSUMER_DROP = World.class.getDeclaredMethod("dropItem", Location.class, ItemStack.class, org.bukkit.util.Consumer.class);
      } catch (SecurityException | NoSuchMethodException var1) {
      }

      m_adjective = new Adjective("entities.dropped item.adjective");
   }
}
