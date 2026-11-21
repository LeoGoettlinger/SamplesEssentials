package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Steerable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@Name("Equip")
@Description({"Equips or unequips an entity with the given itemtypes (usually armor).", "This effect will replace any armor that the entity is already wearing."})
@Example.Examples({@Example("equip player with diamond helmet"), @Example("equip player with diamond leggings, diamond chestplate, and diamond boots"), @Example("unequip diamond chestplate from player"), @Example("unequip player's armor")})
@Since({"1.0, 2.7 (multiple entities, unequip), 2.10 (wolves)", "2.12.1 (happy ghasts)"})
public class EffEquip extends Effect {
   private static final ItemType CHESTPLATE;
   private static final ItemType LEGGINGS;
   private static final ItemType BOOTS;
   private static final ItemType CARPET = new ItemType(Tag.WOOL_CARPETS);
   private static final ItemType WOLF_ARMOR;
   private static final ItemType HORSE_ARMOR = new ItemType(
      Material.LEATHER_HORSE_ARMOR, Material.IRON_HORSE_ARMOR, Material.GOLDEN_HORSE_ARMOR, Material.DIAMOND_HORSE_ARMOR
   );
   private static final ItemType SADDLE = new ItemType(Material.SADDLE);
   private static final ItemType CHEST = new ItemType(Material.CHEST);
   private static final ItemType HAPPY_GHAST_HARNESS;
   private static final Class<?> HAPPY_GHAST_CLASS;
   private static final ItemType[] ALL_EQUIPMENT;
   private Expression<LivingEntity> entities;
   @UnknownNullability
   private Expression<ItemType> itemTypes;
   private boolean equip = true;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      if (matchedPattern == 0 || matchedPattern == 1) {
         this.entities = (Expression<LivingEntity>)exprs[0];
         this.itemTypes = (Expression<ItemType>)exprs[1];
      } else if (matchedPattern == 2) {
         this.itemTypes = (Expression<ItemType>)exprs[0];
         this.entities = (Expression<LivingEntity>)exprs[1];
         this.equip = false;
      } else if (matchedPattern == 3) {
         this.entities = (Expression<LivingEntity>)exprs[0];
         this.equip = false;
      }

      return true;
   }

   @Override
   protected void execute(Event event) {
      boolean unequipHelmet = false;
      ItemType[] itemTypes;
      if (this.itemTypes != null) {
         itemTypes = this.itemTypes.getArray(event);
      } else {
         itemTypes = ALL_EQUIPMENT;
         unequipHelmet = true;
      }

      for (LivingEntity entity : this.entities.getArray(event)) {
         if (entity instanceof Steerable steerable) {
            for (ItemType itemType : itemTypes) {
               if (SADDLE.isOfType(itemType.getMaterial())) {
                  steerable.setSaddle(this.equip);
               }
            }
         } else if (entity instanceof Llama llama) {
            LlamaInventory inv = llama.getInventory();

            for (ItemType itemTypex : itemTypes) {
               for (ItemStack item : itemTypex.getAll()) {
                  if (CARPET.isOfType(item)) {
                     inv.setDecor(this.equip ? item : null);
                  } else if (CHEST.isOfType(item)) {
                     llama.setCarryingChest(this.equip);
                  }
               }
            }
         } else if (entity instanceof AbstractHorse horse) {
            AbstractHorseInventory inv = horse.getInventory();

            for (ItemType itemTypex : itemTypes) {
               for (ItemStack itemx : itemTypex.getAll()) {
                  if (SADDLE.isOfType(itemx)) {
                     inv.setSaddle(this.equip ? itemx : null);
                  } else if (HORSE_ARMOR.isOfType(itemx) && entity instanceof Horse) {
                     ((HorseInventory)inv).setArmor(this.equip ? itemx : null);
                  } else if (CHEST.isOfType(itemx) && entity instanceof ChestedHorse chestedHorse) {
                     chestedHorse.setCarryingChest(this.equip);
                  }
               }
            }
         } else if (entity instanceof Wolf wolf) {
            EntityEquipment equipment = wolf.getEquipment();

            for (ItemType itemTypex : itemTypes) {
               for (ItemStack itemxx : itemTypex.getAll()) {
                  if (WOLF_ARMOR.isOfType(itemxx)) {
                     equipment.setItem(EquipmentSlot.BODY, this.equip ? itemxx : null);
                  }
               }
            }
         } else if (HAPPY_GHAST_CLASS != null && HAPPY_GHAST_CLASS.isInstance(entity)) {
            EntityEquipment equipment = ((Mob)entity).getEquipment();

            for (ItemType itemTypex : itemTypes) {
               for (ItemStack itemStack : itemTypex.getAll()) {
                  if (HAPPY_GHAST_HARNESS.isOfType(itemStack)) {
                     equipment.setItem(EquipmentSlot.BODY, this.equip ? itemStack : null);
                  }
               }
            }
         } else {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment != null) {
               for (ItemType itemTypex : itemTypes) {
                  for (ItemStack itemxxx : itemTypex.getAll()) {
                     if (CHESTPLATE.isOfType(itemxxx)) {
                        equipment.setChestplate(this.equip ? itemxxx : null);
                     } else if (LEGGINGS.isOfType(itemxxx)) {
                        equipment.setLeggings(this.equip ? itemxxx : null);
                     } else if (BOOTS.isOfType(itemxxx)) {
                        equipment.setBoots(this.equip ? itemxxx : null);
                     } else {
                        equipment.setHelmet(this.equip ? itemxxx : null);
                     }
                  }

                  if (unequipHelmet) {
                     equipment.setHelmet(null);
                  }
               }

               if (entity instanceof Player player) {
                  PlayerUtils.updateInventory(player);
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.equip) {
         assert this.itemTypes != null;

         return "equip " + this.entities.toString(event, debug) + " with " + this.itemTypes.toString(event, debug);
      } else {
         return this.itemTypes != null
            ? "unequip " + this.itemTypes.toString(event, debug) + " from " + this.entities.toString(event, debug)
            : "unequip " + this.entities.toString(event, debug) + "'s equipment";
      }
   }

   static {
      if (Skript.fieldExists(Material.class, "WOLF_ARMOR")) {
         WOLF_ARMOR = new ItemType(Material.WOLF_ARMOR);
      } else {
         WOLF_ARMOR = new ItemType();
      }

      if (Skript.fieldExists(Tag.class, "ITEM_CHEST_ARMOR")) {
         CHESTPLATE = new ItemType(Tag.ITEMS_CHEST_ARMOR);
         LEGGINGS = new ItemType(Tag.ITEMS_LEG_ARMOR);
         BOOTS = new ItemType(Tag.ITEMS_FOOT_ARMOR);
      } else {
         CHESTPLATE = new ItemType(
            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE,
            Material.ELYTRA
         );
         LEGGINGS = new ItemType(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS
         );
         BOOTS = new ItemType(
            Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.GOLDEN_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
         );
      }

      if (Skript.fieldExists(Tag.class, "ITEMS_HARNESSES")) {
         HAPPY_GHAST_HARNESS = new ItemType(Tag.ITEMS_HARNESSES);

         try {
            HAPPY_GHAST_CLASS = Class.forName("org.bukkit.entity.HappyGhast");
         } catch (ClassNotFoundException var1) {
            throw new RuntimeException(var1);
         }
      } else {
         HAPPY_GHAST_HARNESS = new ItemType();
         HAPPY_GHAST_CLASS = null;
      }

      ALL_EQUIPMENT = new ItemType[]{CHESTPLATE, LEGGINGS, BOOTS, HORSE_ARMOR, SADDLE, CHEST, CARPET, WOLF_ARMOR, HAPPY_GHAST_HARNESS};
      Skript.registerEffect(
         EffEquip.class,
         "equip [%livingentities%] with %itemtypes%",
         "make %livingentities% wear %itemtypes%",
         "unequip %itemtypes% [from %livingentities%]",
         "unequip %livingentities%'[s] (armo[u]r|equipment)"
      );
   }
}
