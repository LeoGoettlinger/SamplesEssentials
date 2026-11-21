package net.coreprotect.bukkit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public interface BukkitInterface {
   boolean isAttached(Block var1, Block var2, BlockData var3, int var4);

   int getMinHeight(World var1);

   int getLegacyBlockId(Material var1);

   Material getBucketContents(Material var1);

   boolean isItemFrame(Material var1);

   boolean isInvisible(Material var1);

   boolean isDecoratedPot(Material var1);

   boolean isSuspiciousBlock(Material var1);

   boolean isSign(Material var1);

   boolean isChiseledBookshelf(Material var1);

   boolean isBookshelfBook(Material var1);

   Material getPlantSeeds(Material var1);

   ItemStack adjustIngredient(MerchantRecipe var1, ItemStack var2);

   boolean getItemMeta(ItemMeta var1, List<Map<String, Object>> var2, List<List<Map<String, Object>>> var3, int var4);

   boolean setItemMeta(Material var1, ItemStack var2, List<Map<String, Object>> var3);

   ItemStack getChiseledBookshelfBook(BlockState var1, PlayerInteractEvent var2);

   ItemStack getArrowMeta(Arrow var1, ItemStack var2);

   boolean getEntityMeta(LivingEntity var1, List<Object> var2);

   boolean setEntityMeta(Entity var1, Object var2, int var3);

   void getWolfVariant(Wolf var1, List<Object> var2);

   void setWolfVariant(Wolf var1, Object var2);

   Material getFrameType(Entity var1);

   Material getFrameType(EntityType var1);

   EntityType getEntityType(Material var1);

   Class<?> getFrameClass(Material var1);

   boolean isGlowing(Sign var1, boolean var2);

   boolean isWaxed(Sign var1);

   void setGlowing(Sign var1, boolean var2, boolean var3);

   void setColor(Sign var1, boolean var2, int var3);

   void setWaxed(Sign var1, boolean var2);

   int getColor(Sign var1, boolean var2);

   String getLine(Sign var1, int var2);

   void setLine(Sign var1, int var2, String var3);

   boolean isSignFront(SignChangeEvent var1);

   Object getRegistryKey(Object var1);

   Object getRegistryValue(String var1, Object var2);

   String parseLegacyName(String var1);

   boolean isCrafter(InventoryType var1);

   boolean isCopperChest(Material var1);

   Set<Material> copperChestMaterials();
}
