package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Boat.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoatData extends EntityData<Boat> {
   private static final Type[] types = Type.values();
   private static final Map<Material, Type> materialToType;

   public BoatData() {
      this(0);
   }

   public BoatData(@Nullable Type type) {
      this(type != null ? type.ordinal() + 2 : 1);
   }

   private BoatData(int type) {
      this.codeNameIndex = type;
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      return true;
   }

   protected boolean init(@Nullable Class<? extends Boat> entityClass, @Nullable Boat boat) {
      if (boat != null) {
         this.codeNameIndex = 2 + boat.getBoatType().ordinal();
      }

      return true;
   }

   public void set(Boat boat) {
      if (this.codeNameIndex == 1) {
         this.codeNameIndex = this.codeNameIndex + new Random().nextInt(Type.values().length);
      }

      if (this.codeNameIndex > 1) {
         boat.setBoatType(types[this.codeNameIndex - 2]);
      }
   }

   protected boolean match(Boat boat) {
      return this.codeNameIndex <= 1 || boat.getBoatType().ordinal() == this.codeNameIndex - 2;
   }

   @Override
   public Class<? extends Boat> getType() {
      return Boat.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new BoatData(this.codeNameIndex);
   }

   @Override
   protected int hashCode_i() {
      return this.codeNameIndex <= 1 ? 0 : this.codeNameIndex;
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof BoatData other ? this.codeNameIndex == other.codeNameIndex : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return !(entityData instanceof BoatData other) ? false : this.codeNameIndex <= 1 || this.codeNameIndex == other.codeNameIndex;
   }

   public boolean isOfItemType(ItemType itemType) {
      for (ItemData itemData : itemType.getTypes()) {
         Material material = itemData.getType();
         Type type = materialToType.get(material);
         if (type != null) {
            int ordinal = type.ordinal();
            if (this.codeNameIndex <= 1 || this.codeNameIndex == ordinal + 2) {
               return true;
            }
         }
      }

      return false;
   }

   static {
      if (!Skript.isRunningMinecraft(1, 21, 2)) {
         String[] patterns = new String[types.length + 2];
         patterns[0] = "boat";
         patterns[1] = "any boat";

         for (Type boat : types) {
            String boatName;
            if (boat == Type.BAMBOO) {
               boatName = "bamboo raft";
            } else {
               boatName = boat.toString().replace("_", " ").toLowerCase(Locale.ENGLISH) + " boat";
            }

            patterns[boat.ordinal() + 2] = boatName;
         }

         EntityData.register(BoatData.class, "boat", Boat.class, 0, patterns);
      }

      materialToType = new HashMap<>();
      materialToType.put(Material.OAK_BOAT, Type.OAK);
      materialToType.put(Material.BIRCH_BOAT, Type.BIRCH);
      materialToType.put(Material.SPRUCE_BOAT, Type.SPRUCE);
      materialToType.put(Material.JUNGLE_BOAT, Type.JUNGLE);
      materialToType.put(Material.DARK_OAK_BOAT, Type.DARK_OAK);
      materialToType.put(Material.ACACIA_BOAT, Type.ACACIA);
      materialToType.put(Material.MANGROVE_BOAT, Type.MANGROVE);
      materialToType.put(Material.CHERRY_BOAT, Type.CHERRY);
      materialToType.put(Material.BAMBOO_RAFT, Type.BAMBOO);
      materialToType.put(Material.OAK_CHEST_BOAT, Type.OAK);
      materialToType.put(Material.BIRCH_CHEST_BOAT, Type.BIRCH);
      materialToType.put(Material.SPRUCE_CHEST_BOAT, Type.SPRUCE);
      materialToType.put(Material.JUNGLE_CHEST_BOAT, Type.JUNGLE);
      materialToType.put(Material.DARK_OAK_CHEST_BOAT, Type.DARK_OAK);
      materialToType.put(Material.ACACIA_CHEST_BOAT, Type.ACACIA);
      materialToType.put(Material.MANGROVE_CHEST_BOAT, Type.MANGROVE);
      materialToType.put(Material.CHERRY_CHEST_BOAT, Type.CHERRY);
      materialToType.put(Material.BAMBOO_CHEST_RAFT, Type.BAMBOO);
   }
}
