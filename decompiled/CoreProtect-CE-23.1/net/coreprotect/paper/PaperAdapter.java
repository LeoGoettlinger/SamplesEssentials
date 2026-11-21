package net.coreprotect.paper;

import java.util.UUID;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.config.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PaperAdapter implements PaperInterface {
   public static PaperInterface ADAPTER;
   public static final int PAPER_UNAVAILABLE = 0;
   public static final int PAPER_V1_13 = 13;
   public static final int PAPER_V1_14 = 14;
   public static final int PAPER_V1_15 = 15;
   public static final int PAPER_V1_16 = 16;
   public static final int PAPER_V1_17 = 17;
   public static final int PAPER_V1_18 = 18;
   public static final int PAPER_V1_19 = 19;
   public static final int PAPER_V1_20 = 20;
   public static final int PAPER_V1_21 = 21;

   public static void loadAdapter() {
      int paperVersion = ConfigHandler.SERVER_VERSION;
      if (!ConfigHandler.isPaper) {
         paperVersion = 0;
      }

      switch (paperVersion) {
         case 0:
            ADAPTER = new PaperAdapter();
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
         case 7:
         case 8:
         case 9:
         case 10:
         case 11:
         case 12:
         case 20:
         case 21:
         default:
            ADAPTER = new Paper_v1_20();
            break;
         case 13:
         case 14:
         case 15:
         case 16:
            ADAPTER = new PaperHandler();
            break;
         case 17:
         case 18:
         case 19:
            ADAPTER = new Paper_v1_17();
      }
   }

   @Override
   public InventoryHolder getHolder(Inventory holder, boolean useSnapshot) {
      return holder.getHolder();
   }

   @Override
   public boolean isStopping(Server server) {
      return false;
   }

   @Override
   public String getLine(Sign sign, int line) {
      return BukkitAdapter.ADAPTER.getLine(sign, line);
   }

   @Override
   public void teleportAsync(Entity entity, Location location) {
      entity.teleport(location);
   }

   @Override
   public String getSkullOwner(Skull skull) {
      return skull.getOwningPlayer().getUniqueId().toString();
   }

   @Override
   public void setSkullOwner(Skull skull, String owner) {
      if (owner != null && owner.length() >= 32 && owner.contains("-")) {
         skull.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(owner)));
      }
   }

   @Override
   public String getSkullSkin(Skull skull) {
      return null;
   }

   @Override
   public void setSkullSkin(Skull skull, String skin) {
   }
}
