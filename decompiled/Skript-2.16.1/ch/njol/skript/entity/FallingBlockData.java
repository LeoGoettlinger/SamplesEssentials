package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.localization.Adjective;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FallingBlockData extends EntityData<FallingBlock> {
   private static final Message m_not_a_block_error = new Message("entities.falling block.not a block error");
   private static final Adjective m_adjective = new Adjective("entities.falling block.adjective");
   @Nullable
   private ItemType[] types = null;

   public FallingBlockData() {
   }

   public FallingBlockData(@Nullable ItemType[] types) {
      this.types = types;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 1) {
         assert exprs[0] != null;

         ItemType[] itemTypes = (ItemType[])exprs[0].getAll();
         this.types = Arrays.stream(itemTypes).map(itemType -> {
            ItemType clone = itemType.getBlock().clone();
            Iterator<ItemData> iterator = clone.iterator();

            while (iterator.hasNext()) {
               Material material = iterator.next().getType();
               if (!material.isBlock()) {
                  iterator.remove();
               }
            }

            if (clone.numTypes() == 0) {
               return null;
            } else {
               clone.setAmount(-1);
               clone.setAll(false);
               clone.clearEnchantments();
               return clone;
            }
         }).filter(Objects::nonNull).toArray(ItemType[]::new);
         if (this.types.length == 0) {
            Skript.error(m_not_a_block_error.toString());
            return false;
         }
      }

      return true;
   }

   protected boolean init(@Nullable Class<? extends FallingBlock> entityClass, @Nullable FallingBlock fallingBlock) {
      if (fallingBlock != null) {
         this.types = new ItemType[]{new ItemType(fallingBlock.getBlockData())};
      }

      return true;
   }

   public void set(FallingBlock fallingBlock) {
      assert false;
   }

   protected boolean match(FallingBlock fallingBlock) {
      if (this.types != null) {
         for (ItemType itemType : this.types) {
            if (itemType.isOfType(fallingBlock.getBlockData())) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public Class<? extends FallingBlock> getType() {
      return FallingBlock.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new FallingBlockData();
   }

   @Override
   protected int hashCode_i() {
      return Arrays.hashCode((Object[])this.types);
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof FallingBlockData other ? Arrays.equals((Object[])this.types, (Object[])other.types) : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      if (entityData instanceof FallingBlockData other) {
         if (this.types != null) {
            return other.types != null ? ItemType.isSubset(this.types, other.types) : false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @Nullable
   public FallingBlock spawn(Location loc, @Nullable Consumer<FallingBlock> consumer) {
      ItemType t = this.types == null ? new ItemType(Material.STONE) : CollectionUtils.getRandom(this.types);

      assert t != null;

      Material material = t.getMaterial();
      if (!material.isBlock()) {
         assert false : t;

         return null;
      } else {
         FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(loc, material.createBlockData());
         if (consumer != null) {
            consumer.accept(fallingBlock);
         }

         return fallingBlock;
      }
   }

   @Override
   public String toString(int flags) {
      ItemType[] types = this.types;
      if (types == null) {
         return super.toString(flags);
      } else {
         StringBuilder builder = new StringBuilder();
         builder.append(Noun.getArticleWithSpace(types[0].getTypes().get(0).getGender(), flags));
         builder.append(m_adjective.toString(types[0].getTypes().get(0).getGender(), flags));
         builder.append(" ");
         builder.append(Classes.toString(types, flags & -7, false));
         return builder.toString();
      }
   }

   static {
      EntityData.register(FallingBlockData.class, "falling block", FallingBlock.class, "falling block");
   }
}
