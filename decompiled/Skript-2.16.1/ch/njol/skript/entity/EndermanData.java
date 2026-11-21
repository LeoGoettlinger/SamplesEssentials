package ch.njol.skript.entity;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.ArgsMessage;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EndermanData extends EntityData<Enderman> {
   private static final ArgsMessage FORMAT = new ArgsMessage("entities.enderman.format");
   @Nullable
   private ItemType[] hand = null;

   public EndermanData() {
   }

   public EndermanData(@Nullable ItemType[] hand) {
      this.hand = hand;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (exprs[0] != null) {
         this.hand = (ItemType[])exprs[0].getAll();
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends Enderman> entityClass, @Nullable Enderman enderman) {
      if (enderman != null) {
         BlockData data = enderman.getCarriedBlock();
         if (data != null) {
            Material type = data.getMaterial();
            this.hand = new ItemType[]{new ItemType(type)};
         }
      }

      return true;
   }

   public void set(Enderman enderman) {
      if (this.hand != null) {
         ItemType itemType = CollectionUtils.getRandom(this.hand);

         assert itemType != null;

         ItemStack itemStack = itemType.getBlock().getRandom();
         if (itemStack != null) {
            enderman.setCarriedBlock(Bukkit.createBlockData(itemStack.getType()));
         }
      }
   }

   public boolean match(Enderman enderman) {
      return this.hand == null
         || SimpleExpression.check(this.hand, type -> type != null && type.isOfType(enderman.getCarriedBlock().getMaterial()), false, false);
   }

   @Override
   public Class<Enderman> getType() {
      return Enderman.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new EndermanData();
   }

   @Override
   protected int hashCode_i() {
      return Arrays.hashCode((Object[])this.hand);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof EndermanData other ? Arrays.equals((Object[])this.hand, (Object[])other.hand) : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof EndermanData other) {
         return this.hand == null ? true : other.hand != null && ItemType.isSubset(this.hand, other.hand);
      } else {
         return false;
      }
   }

   @Override
   public String toString(int flags) {
      ItemType[] hand = this.hand;
      return hand == null ? super.toString(flags) : FORMAT.toString(super.toString(flags), Classes.toString(hand, false));
   }

   private boolean isSubhand(@Nullable ItemType[] sub) {
      return this.hand == null ? true : sub != null && ItemType.isSubset(this.hand, sub);
   }

   static {
      EntityData.register(EndermanData.class, "enderman", Enderman.class, "enderman");
   }
}
