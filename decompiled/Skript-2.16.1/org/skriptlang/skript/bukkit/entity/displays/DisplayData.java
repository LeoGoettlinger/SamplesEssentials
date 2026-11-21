package org.skriptlang.skript.bukkit.entity.displays;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.variables.Variables;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class DisplayData extends EntityData<Display> {
   public static final Color DEFAULT_BACKGROUND_COLOR = ColorRGB.fromRGBA(0, 0, 0, 64).asBukkitColor();
   private DisplayData.DisplayType type = DisplayData.DisplayType.ANY;
   @Nullable
   private BlockData blockData;
   @Nullable
   private ItemStack item;
   @Nullable
   private Component text;

   public static void register(SyntaxRegistry registry) {
      EntityData.register(DisplayData.class, "display", Display.class, 0, DisplayData.DisplayType.codeNames);
      Variables.yggdrasil.registerSingleClass(DisplayData.DisplayType.class, "DisplayType");
   }

   public DisplayData() {
   }

   public DisplayData(DisplayData.DisplayType type) {
      this.type = type;
      this.codeNameIndex = type.ordinal();
   }

   @Override
   protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, SkriptParser.ParseResult parseResult) {
      this.type = DisplayData.DisplayType.values()[matchedCodeName];
      if (exprs.length != 0 && exprs[0] != null) {
         if (this.type == DisplayData.DisplayType.BLOCK) {
            Object object = exprs[0].getSingle();
            if (object instanceof ItemType itemType) {
               if (!itemType.hasBlock()) {
                  Skript.error(
                     "A block display must be of a block item. "
                        + Classes.toString(itemType.getMaterial())
                        + " is not a block. If you want to display an item, use an 'item display'."
                  );
                  return false;
               }

               this.blockData = Bukkit.createBlockData(itemType.getBlockMaterial());
            } else {
               this.blockData = (BlockData)object;
            }
         } else if (this.type == DisplayData.DisplayType.ITEM) {
            ItemType itemType = (ItemType)exprs[0].getSingle();
            if (!itemType.hasItem()) {
               Skript.error(
                  "An item display must be of a valid item. "
                     + Classes.toString(itemType.getMaterial())
                     + " is not a valid item. If you want to display a block, use a 'block display'."
               );
               return false;
            }

            this.item = itemType.getRandom();
         }

         return true;
      } else {
         return true;
      }
   }

   protected boolean init(@Nullable Class<? extends Display> displayClass, @Nullable Display entity) {
      DisplayData.DisplayType[] types = DisplayData.DisplayType.values();

      for (int i = types.length - 1; i >= 0; i--) {
         Class<?> display = types[i].displaySubClass;
         if (display != null && (entity == null ? displayClass.isAssignableFrom(display) : display.isInstance(entity))) {
            this.type = types[i];
            if (entity != null) {
               switch (this.type) {
                  case BLOCK:
                     this.blockData = ((BlockDisplay)entity).getBlock();
                     break;
                  case ITEM:
                     this.item = ((ItemDisplay)entity).getItemStack();
                     break;
                  case TEXT:
                     this.text = ((TextDisplay)entity).text();
               }
            }

            return true;
         }
      }

      assert false;

      return false;
   }

   public void set(Display entity) {
      switch (this.type) {
         case BLOCK:
            if (this.blockData != null && entity instanceof BlockDisplay blockDisplay) {
               blockDisplay.setBlock(this.blockData);
            }
            break;
         case ITEM:
            if (this.item != null && entity instanceof ItemDisplay itemDisplay) {
               itemDisplay.setItemStack(this.item);
            }
            break;
         case TEXT:
            if (this.text != null && entity instanceof TextDisplay textDisplay) {
               textDisplay.text(this.text);
            }
      }
   }

   public boolean match(Display entity) {
      switch (this.type) {
         case BLOCK:
            if (!(entity instanceof BlockDisplay blockDisplay)) {
               return false;
            }

            if (this.blockData != null && !blockDisplay.getBlock().equals(this.blockData)) {
               return false;
            }
            break;
         case ITEM:
            if (!(entity instanceof ItemDisplay itemDisplay)) {
               return false;
            }

            if (this.item != null && !itemDisplay.getItemStack().isSimilar(this.item)) {
               return false;
            }
            break;
         case TEXT:
            if (entity instanceof TextDisplay textDisplay) {
               if (this.text != null && !this.text.equals(Component.empty())) {
                  return textDisplay.text().equals(this.text);
               }

               return true;
            }

            return false;
      }

      return this.type.displaySubClass != null && this.type.displaySubClass.isInstance(entity);
   }

   @Override
   public Class<? extends Display> getType() {
      return this.type.displaySubClass != null ? this.type.displaySubClass : Display.class;
   }

   @NotNull
   @Override
   public EntityData<?> getSuperType() {
      return new DisplayData(DisplayData.DisplayType.ANY);
   }

   @Override
   protected int hashCode_i() {
      return this.type.hashCode();
   }

   @Override
   protected boolean equals_i(EntityData<?> entityData) {
      return entityData instanceof DisplayData other ? this.type == other.type : false;
   }

   @Override
   public boolean isSupertypeOf(EntityData<?> entityData) {
      return !(entityData instanceof DisplayData displayData)
         ? Display.class.isAssignableFrom(entityData.getType())
         : this.type == DisplayData.DisplayType.ANY || displayData.type == this.type;
   }

   @Override
   public boolean canSpawn(@Nullable World world) {
      return this.type != DisplayData.DisplayType.ANY;
   }

   private static enum DisplayType {
      ANY("org.bukkit.entity.Display", "display"),
      BLOCK("org.bukkit.entity.BlockDisplay", "block display"),
      ITEM("org.bukkit.entity.ItemDisplay", "item display"),
      TEXT("org.bukkit.entity.TextDisplay", "text display");

      @Nullable
      private Class<? extends Display> displaySubClass;
      private final String codeName;
      private static final String[] codeNames;

      private DisplayType(String className, String codeName) {
         try {
            this.displaySubClass = (Class<? extends Display>)Class.forName(className);
         } catch (ClassNotFoundException var6) {
         }

         this.codeName = codeName;
      }

      @Override
      public String toString() {
         return this.codeName;
      }

      static {
         List<String> codeNamesList = new ArrayList<>();

         for (DisplayData.DisplayType type : values()) {
            if (type.displaySubClass != null) {
               codeNamesList.add(type.codeName);
            }
         }

         codeNames = codeNamesList.toArray(new String[0]);
      }
   }
}
