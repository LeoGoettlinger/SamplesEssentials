package ch.njol.skript.classes.data;

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.types.BlockClassInfo;
import org.skriptlang.skript.bukkit.types.EntityClassInfo;
import org.skriptlang.skript.bukkit.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.types.PlayerClassInfo;

public class DefaultChangers {
   public static final Changer<Entity> entityChanger = new EntityClassInfo.EntityChanger();
   public static final Changer<Player> playerChanger = new PlayerClassInfo.PlayerChanger();
   public static final Changer<Entity> nonLivingEntityChanger = new Changer<Entity>() {
      @Nullable
      @Override
      public Class<Object>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.DELETE ? CollectionUtils.array() : null;
      }

      public void change(Entity[] entities, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert mode == Changer.ChangeMode.DELETE;

         for (Entity e : entities) {
            if (!(e instanceof Player)) {
               e.remove();
            }
         }
      }
   };
   public static final Changer<Item> itemChanger = new Changer<Item>() {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.SET ? CollectionUtils.array(ItemStack.class) : DefaultChangers.nonLivingEntityChanger.acceptChange(mode);
      }

      public void change(Item[] what, @Nullable Object[] delta, Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.SET) {
            assert delta != null;

            for (Item i : what) {
               i.setItemStack((ItemStack)delta[0]);
            }
         } else {
            DefaultChangers.nonLivingEntityChanger.change(what, delta, mode);
         }
      }
   };
   public static final Changer<Inventory> inventoryChanger = new InventoryClassInfo.InventoryChanger();
   public static final Changer<Block> blockChanger = new BlockClassInfo.BlockChanger();
}
