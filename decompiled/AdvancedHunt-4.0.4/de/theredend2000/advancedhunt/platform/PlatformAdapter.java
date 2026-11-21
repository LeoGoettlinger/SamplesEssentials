package de.theredend2000.advancedhunt.platform;

import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface PlatformAdapter {
   boolean isAir(Material var1);

   void spawnParticle(Location var1, String var2, int var3, double var4, double var6, double var8, double var10);

   void spawnParticleForPlayer(Player var1, Location var2, String var3, int var4, double var5, double var7, double var9, double var11);

   void applyHideTooltip(ItemMeta var1, boolean var2);

   void applyUnbreakable(ItemMeta var1, boolean var2);

   void applyCustomModelData(ItemMeta var1, Integer var2);

   void applySkullOwner(ItemMeta var1, UUID var2);

   void applySkullOwner(ItemMeta var1, String var2);

   ItemStack ensurePlayerHeadItem(ItemStack var1);

   boolean isMainHandInteract(PlayerInteractEvent var1);

   String getBlockStateString(Block var1);

   void setFireworkSilent(Firework var1, boolean var2);

   boolean spawnHologramArmorStandForPlayer(Player var1, int var2, UUID var3, Location var4, String var5);

   boolean destroyEntitiesForPlayer(Player var1, int... var2);

   boolean spawnGlowingBlockMarkerForPlayer(Player var1, int var2, UUID var3, Location var4);

   void sendClickableCopyText(Player var1, String var2, String var3, String var4);

   void sendSkullUpdatePacket(Player var1, Location var2, String var3, String var4);
}
