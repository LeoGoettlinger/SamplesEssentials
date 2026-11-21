package net.coreprotect.bukkit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.coreprotect.model.BlockGroup;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.ChiseledBookshelf;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Arrow;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class Bukkit_v1_20 extends Bukkit_v1_19 {
   private Boolean hasClickedPosition;
   private Boolean hasBasePotionType;

   public Bukkit_v1_20() {
      this.initializeBlockGroups();
      this.initializeTaggedBlocks();
   }

   private void initializeBlockGroups() {
      BlockGroup.CONTAINERS = new HashSet<>(
         Arrays.asList(
            Material.JUKEBOX,
            Material.DISPENSER,
            Material.CHEST,
            Material.FURNACE,
            Material.BREWING_STAND,
            Material.TRAPPED_CHEST,
            Material.HOPPER,
            Material.DROPPER,
            Material.ARMOR_STAND,
            Material.ITEM_FRAME,
            Material.SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.PINK_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.BARREL,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.LECTERN,
            Material.CHISELED_BOOKSHELF,
            Material.DECORATED_POT
         )
      );
      BlockGroup.UPDATE_STATE = new HashSet<>(
         Arrays.asList(
            Material.TORCH,
            Material.WALL_TORCH,
            Material.REDSTONE_WIRE,
            Material.RAIL,
            Material.POWERED_RAIL,
            Material.DETECTOR_RAIL,
            Material.FURNACE,
            Material.BLAST_FURNACE,
            Material.SMOKER,
            Material.LEVER,
            Material.REDSTONE_TORCH,
            Material.REDSTONE_WALL_TORCH,
            Material.GLOWSTONE,
            Material.JACK_O_LANTERN,
            Material.REPEATER,
            Material.REDSTONE_LAMP,
            Material.BEACON,
            Material.COMPARATOR,
            Material.DAYLIGHT_DETECTOR,
            Material.REDSTONE_BLOCK,
            Material.HOPPER,
            Material.CHEST,
            Material.TRAPPED_CHEST,
            Material.ACTIVATOR_RAIL,
            Material.SOUL_TORCH,
            Material.SOUL_WALL_TORCH,
            Material.SHROOMLIGHT,
            Material.RESPAWN_ANCHOR,
            Material.CRYING_OBSIDIAN,
            Material.TARGET,
            Material.SMALL_AMETHYST_BUD,
            Material.MEDIUM_AMETHYST_BUD,
            Material.LARGE_AMETHYST_BUD,
            Material.AMETHYST_CLUSTER,
            Material.CAVE_VINES,
            Material.CAVE_VINES_PLANT,
            Material.GLOW_LICHEN,
            Material.LIGHT,
            Material.LAVA_CAULDRON,
            Material.CHISELED_BOOKSHELF
         )
      );
   }

   private void initializeTaggedBlocks() {
      BlockGroup.BUTTONS.clear();
      BlockGroup.BUTTONS.addAll(Tag.BUTTONS.getValues());
      BlockGroup.PRESSURE_PLATES.clear();
      BlockGroup.PRESSURE_PLATES.addAll(Tag.PRESSURE_PLATES.getValues());
      this.addMissingTaggedBlocks(Tag.DOORS.getValues(), BlockGroup.DOORS);
      this.addMissingTaggedBlocks(Tag.FENCE_GATES.getValues(), BlockGroup.INTERACT_BLOCKS);
      this.addMissingTaggedBlocks(Tag.FENCE_GATES.getValues(), BlockGroup.SAFE_INTERACT_BLOCKS);
      this.addMissingTaggedBlocks(Tag.WOODEN_TRAPDOORS.getValues(), BlockGroup.INTERACT_BLOCKS);
      this.addMissingTaggedBlocks(Tag.WOODEN_TRAPDOORS.getValues(), BlockGroup.SAFE_INTERACT_BLOCKS);
      this.addMissingTaggedBlocks(Tag.CEILING_HANGING_SIGNS.getValues(), BlockGroup.TRACK_BOTTOM);
      this.addMissingTaggedBlocks(Tag.WALL_SIGNS.getValues(), BlockGroup.TRACK_SIDE);
      this.addMissingTaggedBlocks(Tag.SAPLINGS.getValues(), BlockGroup.TRACK_TOP);
      this.addMissingTaggedBlocks(Tag.SAPLINGS.getValues(), BlockGroup.NON_ATTACHABLE);
      this.addMissingTaggedBlocks(Tag.FLOWERS.getValues(), BlockGroup.TRACK_TOP);
      this.addMissingTaggedBlocks(Tag.FLOWERS.getValues(), BlockGroup.NON_ATTACHABLE);

      for (Material value : Tag.SIGNS.getValues()) {
         if (!Tag.WALL_SIGNS.isTagged(value) && !BlockGroup.TRACK_TOP.contains(value)) {
            BlockGroup.TRACK_TOP.add(value);
         }
      }
   }

   private void addMissingTaggedBlocks(Iterable<Material> taggedBlocks, Set<Material> targetGroup) {
      for (Material value : taggedBlocks) {
         if (!targetGroup.contains(value)) {
            targetGroup.add(value);
         }
      }
   }

   @Override
   public void setGlowing(Sign sign, boolean isFront, boolean isGlowing) {
      Side side = isFront ? Side.FRONT : Side.BACK;
      sign.getSide(side).setGlowingText(isGlowing);
   }

   @Override
   public String parseLegacyName(String name) {
      switch (name) {
         case "GRASS_PATH":
            return "DIRT_PATH";
         case "GRASS":
            return "SHORT_GRASS";
         case "SCUTE":
            return "TURTLE_SCUTE";
         default:
            return "SHORT_GRASS".equals(name) && Material.getMaterial(name) == null ? "GRASS" : name;
      }
   }

   @Override
   public void setColor(Sign sign, boolean isFront, int color) {
      Side side = isFront ? Side.FRONT : Side.BACK;
      sign.getSide(side).setColor(DyeColor.getByColor(Color.fromRGB(color)));
   }

   @Override
   public void setWaxed(Sign sign, boolean isWaxed) {
      sign.setWaxed(isWaxed);
   }

   @Override
   public int getColor(Sign sign, boolean isFront) {
      Side side = isFront ? Side.FRONT : Side.BACK;
      return sign.getSide(side).getColor().getColor().asRGB();
   }

   @Override
   public boolean isGlowing(Sign sign, boolean isFront) {
      Side side = isFront ? Side.FRONT : Side.BACK;
      return sign.getSide(side).isGlowingText();
   }

   @Override
   public boolean isWaxed(Sign sign) {
      return sign.isWaxed();
   }

   @Override
   public Material getPlantSeeds(Material material) {
      switch (material) {
         case WHEAT:
            return Material.WHEAT_SEEDS;
         case PUMPKIN_STEM:
            return Material.PUMPKIN_SEEDS;
         case MELON_STEM:
            return Material.MELON_SEEDS;
         case BEETROOTS:
            return Material.BEETROOT_SEEDS;
         case TORCHFLOWER_CROP:
            return Material.TORCHFLOWER_SEEDS;
         default:
            return material;
      }
   }

   @Override
   public boolean isDecoratedPot(Material material) {
      return material == Material.DECORATED_POT;
   }

   @Override
   public boolean isSuspiciousBlock(Material material) {
      return material == Material.SUSPICIOUS_GRAVEL || material == Material.SUSPICIOUS_SAND;
   }

   @Override
   public boolean isSign(Material material) {
      return Tag.ALL_SIGNS.isTagged(material);
   }

   @Override
   public boolean isChiseledBookshelf(Material material) {
      return material == Material.CHISELED_BOOKSHELF;
   }

   @Override
   public boolean isBookshelfBook(Material material) {
      return Tag.ITEMS_BOOKSHELF_BOOKS.isTagged(material);
   }

   @Override
   public ItemStack getChiseledBookshelfBook(BlockState blockState, PlayerInteractEvent event) {
      try {
         if (this.hasClickedPosition == null) {
            this.hasClickedPosition = true;
            PlayerInteractEvent.class.getMethod("getClickedPosition");
         } else if (Boolean.FALSE.equals(this.hasClickedPosition)) {
            return null;
         }

         ChiseledBookshelf bookshelf = (ChiseledBookshelf)blockState;
         int slot = bookshelf.getSlot(event.getClickedPosition());
         ItemStack book = bookshelf.getInventory().getItem(slot);
         return book == null ? new ItemStack(Material.AIR) : book;
      } catch (Exception var6) {
         this.hasClickedPosition = false;
         return null;
      }
   }

   @Override
   public String getLine(Sign sign, int line) {
      return line < 4 ? sign.getSide(Side.FRONT).getLine(line) : sign.getSide(Side.BACK).getLine(line - 4);
   }

   @Override
   public void setLine(Sign sign, int line, String string) {
      string = string == null ? "" : string;
      if (line < 4) {
         sign.getSide(Side.FRONT).setLine(line, string);
      } else {
         sign.getSide(Side.BACK).setLine(line - 4, string);
      }
   }

   @Override
   public boolean isSignFront(SignChangeEvent event) {
      return event.getSide().equals(Side.FRONT);
   }

   @Override
   public ItemStack getArrowMeta(Arrow arrow, ItemStack itemStack) {
      try {
         if (this.hasBasePotionType == null) {
            this.hasBasePotionType = true;
            Arrow.class.getMethod("getBasePotionType");
         } else if (Boolean.FALSE.equals(this.hasBasePotionType)) {
            return super.getArrowMeta(arrow, itemStack);
         }

         PotionType potionType = arrow.getBasePotionType();
         Color color = arrow.getColor();
         if (potionType != null || color != null) {
            itemStack = new ItemStack(Material.TIPPED_ARROW);
            PotionMeta meta = (PotionMeta)itemStack.getItemMeta();
            meta.setBasePotionType(potionType);
            meta.setColor(color);

            for (PotionEffect effect : arrow.getCustomEffects()) {
               meta.addCustomEffect(effect, false);
            }

            itemStack.setItemMeta(meta);
         }

         return itemStack;
      } catch (Exception var8) {
         this.hasBasePotionType = false;
         return super.getArrowMeta(arrow, itemStack);
      }
   }
}
