package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.BlockUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class BlockClassInfo extends ClassInfo<Block> {
   public BlockClassInfo() {
      super(Block.class, "block");
      this.user(new String[]{"blocks?"})
         .name("Block")
         .description(
            "A block in a <a href='#world'>world</a>. It has a <a href='#location'>location</a> and a <a href='#itemstack'>type</a>, and can also have a <a href='#direction'>direction</a> (mostly a <a href='#ExprFacing'>facing</a>), an <a href='#inventory'>inventory</a>, or other special properties."
         )
         .usage("")
         .examples("")
         .since("1.0")
         .defaultExpression(new EventValueExpression<>(Block.class))
         .parser(new BlockClassInfo.BlockParser())
         .changer(new BlockClassInfo.BlockChanger())
         .serializer(new BlockClassInfo.BlockSerializer())
         .property(
            Property.NAME,
            "The custom name of the block, if it has one. Only TileEntities like chests and furnaces can have names. Can be set or reset.",
            Skript.instance(),
            new BlockClassInfo.BlockNameHandler()
         );
   }

   public static class BlockChanger implements Changer<Block> {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.RESET) {
            return null;
         } else {
            return mode == Changer.ChangeMode.SET
               ? CollectionUtils.array(ItemType.class, BlockData.class)
               : CollectionUtils.array(ItemType[].class, Inventory[].class);
         }
      }

      public void change(Block[] blocks, @Nullable Object[] delta, Changer.ChangeMode mode) {
         for (Block block : blocks) {
            assert block != null;

            switch (mode) {
               case SET:
                  assert delta != null;

                  Object object = delta[0];
                  if (object instanceof ItemType itemType) {
                     itemType.getBlock().setBlock(block, true);
                  } else if (object instanceof BlockData blockData) {
                     block.setBlockData(blockData);
                  }
                  break;
               case DELETE:
                  block.setType(Material.AIR, true);
                  break;
               case ADD:
               case REMOVE:
               case REMOVE_ALL:
                  assert delta != null;

                  BlockState state = block.getState();
                  if (!(state instanceof InventoryHolder inventoryHolder)) {
                     break;
                  }

                  Inventory invi = inventoryHolder.getInventory();
                  if (mode == Changer.ChangeMode.ADD) {
                     for (Object obj : delta) {
                        if (obj instanceof Inventory) {
                           for (ItemStack itemStack : (Inventory)obj) {
                              if (itemStack != null) {
                                 invi.addItem(new ItemStack[]{itemStack});
                              }
                           }
                        } else {
                           ((ItemType)obj).addTo(invi);
                        }
                     }
                  } else {
                     for (Object objx : delta) {
                        if (objx instanceof Inventory inventory) {
                           invi.removeItem(Arrays.stream(inventory.getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new));
                        } else if (mode == Changer.ChangeMode.REMOVE) {
                           ((ItemType)objx).removeFrom(invi);
                        } else {
                           ((ItemType)objx).removeAll(invi);
                        }
                     }
                  }

                  state.update();
                  break;
               case RESET:
                  assert false;
            }
         }
      }
   }

   private static class BlockNameHandler implements ExpressionPropertyHandler<Block, Component> {
      public Component convert(Block block) {
         return block.getState() instanceof Nameable nameable ? nameable.customName() : null;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET ? null : new Class[]{Component.class};
      }

      public void change(Block named, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET;

         Component name = delta == null ? null : (Component)delta[0];
         BlockState state = named.getState();
         if (state instanceof Nameable nameable) {
            nameable.customName(name);
            state.update(true, false);
         }
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class BlockParser extends Parser<Block> {
      @Nullable
      public Block parse(String s, ParseContext context) {
         return null;
      }

      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Block b, int flags) {
         return BlockUtils.blockToString(b, flags);
      }

      public String toVariableNameString(Block b) {
         return b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ();
      }

      public String getDebugMessage(Block b) {
         return this.toString(b, 0) + " block (" + b.getWorld().getName() + ":" + b.getX() + "," + b.getY() + "," + b.getZ() + ")";
      }
   }

   private static class BlockSerializer extends Serializer<Block> {
      public Fields serialize(Block b) {
         Fields f = new Fields();
         f.putObject("world", b.getWorld());
         f.putPrimitive("x", b.getX());
         f.putPrimitive("y", b.getY());
         f.putPrimitive("z", b.getZ());
         return f;
      }

      public void deserialize(Block o, Fields f) {
         assert false;
      }

      protected Block deserialize(Fields fields) throws StreamCorruptedException {
         World w = fields.getObject("world", World.class);
         int x = fields.getPrimitive("x", int.class);
         int y = fields.getPrimitive("y", int.class);
         int z = fields.getPrimitive("z", int.class);
         if (w == null) {
            throw new StreamCorruptedException();
         } else {
            return w.getBlockAt(x, y, z);
         }
      }

      @Override
      public boolean mustSyncDeserialization() {
         return true;
      }

      @Override
      public boolean canBeInstantiated() {
         return false;
      }
   }
}
