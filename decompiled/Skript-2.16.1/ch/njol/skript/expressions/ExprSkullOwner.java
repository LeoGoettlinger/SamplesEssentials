package ch.njol.skript.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.profile.PlayerProfile;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

@Name("Skull Owner")
@Description({"The skull owner of a player skull."})
@Example.Examples({@Example("set {_owner} to the skull owner of event-block"), @Example("set skull owner of {_block} to \"Njol\" parsed as offlineplayer"), @Example("set head owner of player's tool to {_player}")})
@Since({"2.9.0, 2.10 (of items)"})
public class ExprSkullOwner extends SimplePropertyExpression<Object, OfflinePlayer> {
   @Nullable
   public OfflinePlayer convert(Object object) {
      if (object instanceof Block block && block.getState() instanceof Skull skull) {
         return this.getOfflinePlayer(skull.getPlayerProfile());
      } else {
         ItemStack skullItem = ItemUtils.asItemStack(object);
         return skullItem != null && skullItem.getItemMeta() instanceof SkullMeta skullMeta ? this.getOfflinePlayer(skullMeta.getPlayerProfile()) : null;
      }
   }

   @Nullable
   private OfflinePlayer getOfflinePlayer(PlayerProfile profile) {
      if (profile == null) {
         return null;
      } else {
         UUID uuid = profile.getId();
         if (uuid != null) {
            return Bukkit.getOfflinePlayer(uuid);
         } else {
            String name = profile.getName();
            return name != null ? Bukkit.getOfflinePlayer(name) : null;
         }
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(OfflinePlayer.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      OfflinePlayer offlinePlayer = (OfflinePlayer)delta[0];
      Consumer<Skull> blockChanger = this.getBlockChanger(offlinePlayer);
      Consumer<SkullMeta> metaChanger = this.getMetaChanger(offlinePlayer);

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Block block && block.getState() instanceof Skull skull) {
            blockChanger.accept(skull);
            skull.update(true, false);
         } else {
            ItemStack skullItem = ItemUtils.asItemStack(object);
            if (skullItem != null && skullItem.getItemMeta() instanceof SkullMeta skullMeta) {
               metaChanger.accept(skullMeta);
               if (object instanceof Slot slot) {
                  skullItem.setItemMeta(skullMeta);
                  slot.setItem(skullItem);
               } else if (object instanceof ItemType itemType) {
                  itemType.setItemMeta(skullMeta);
               } else if (object instanceof ItemStack itemStack) {
                  itemStack.setItemMeta(skullMeta);
               }
            }
         }
      }
   }

   private Consumer<Skull> getBlockChanger(OfflinePlayer offlinePlayer) {
      if (offlinePlayer.getName() != null) {
         return skull -> skull.setOwningPlayer(offlinePlayer);
      } else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
         org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), "");
         return skull -> skull.setOwnerProfile(profile);
      } else {
         return skull -> skull.setOwner("");
      }
   }

   private Consumer<SkullMeta> getMetaChanger(OfflinePlayer offlinePlayer) {
      if (offlinePlayer.getName() != null) {
         return skullMeta -> skullMeta.setOwningPlayer(offlinePlayer);
      } else if (ItemUtils.CAN_CREATE_PLAYER_PROFILE) {
         org.bukkit.profile.PlayerProfile profile = Bukkit.createPlayerProfile(offlinePlayer.getUniqueId(), "");
         return skullMeta -> skullMeta.setOwnerProfile(profile);
      } else {
         return skullMeta -> skullMeta.setOwner("");
      }
   }

   @Override
   public Class<? extends OfflinePlayer> getReturnType() {
      return OfflinePlayer.class;
   }

   @Override
   protected String getPropertyName() {
      return "skull owner";
   }

   static {
      register(ExprSkullOwner.class, OfflinePlayer.class, "(head|skull) owner", "slots/itemtypes/itemstacks/blocks");
   }
}
