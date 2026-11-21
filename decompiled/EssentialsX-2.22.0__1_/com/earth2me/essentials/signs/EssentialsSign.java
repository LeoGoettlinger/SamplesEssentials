package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.MetaItemStack;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.MaterialUtil;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.SignBreakEvent;
import net.ess3.api.events.SignCreateEvent;
import net.ess3.api.events.SignInteractEvent;
import net.ess3.provider.SignDataProvider;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;

public class EssentialsSign {
   private static final String SIGN_OWNER_KEY = "sign-owner";
   protected static final BigDecimal MINTRANSACTION = new BigDecimal("0.01");
   private static final Set<Material> EMPTY_SET = new HashSet<>();
   protected final transient String signName;

   public EssentialsSign(final String signName) {
      this.signName = signName;
   }

   protected static boolean checkIfBlockBreaksSigns(final Block block) {
      Block sign = block.getRelative(BlockFace.UP);
      if (MaterialUtil.isSignPost(sign.getType()) && isValidSign(new EssentialsSign.BlockSign(sign))) {
         return true;
      } else {
         BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};

         for (BlockFace blockFace : directions) {
            Block signBlock = block.getRelative(blockFace);
            if (MaterialUtil.isWallSign(signBlock.getType()) || MaterialUtil.isWallHangingSign(signBlock.getType())) {
               try {
                  if (getWallSignFacing(signBlock) == blockFace && isValidSign(new EssentialsSign.BlockSign(signBlock))) {
                     return true;
                  }
               } catch (NullPointerException var9) {
               }
            }
         }

         return false;
      }
   }

   @Deprecated
   public static boolean isValidSign(final EssentialsSign.ISign sign) {
      return sign.getLine(0).matches("§1\\[.*]");
   }

   public static boolean isValidSign(final IEssentials ess, final EssentialsSign.ISign sign) {
      if (!sign.getLine(0).matches("§1\\[.*]")) {
         return false;
      } else {
         String signName = ChatColor.stripColor(sign.getLine(0)).replaceAll("[^a-zA-Z]", "");

         for (EssentialsSign essSign : ess.getSettings().enabledSigns()) {
            if (essSign.getName().equalsIgnoreCase(signName)) {
               return true;
            }
         }

         return false;
      }
   }

   private static BlockFace getWallSignFacing(final Block block) {
      if (VersionUtil.PRE_FLATTENING) {
         Sign signMat = (Sign)block.getState().getData();
         return signMat.getFacing();
      } else {
         Directional signData = (Directional)block.getState().getBlockData();
         return signData.getFacing();
      }
   }

   protected final boolean onSignCreate(final SignChangeEvent event, final IEssentials ess) {
      EssentialsSign.ISign sign = new EssentialsSign.EventSign(event);
      User user = ess.getUser(event.getPlayer());
      if (!user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".create")
         && !user.isAuthorized("essentials.signs.create." + this.signName.toLowerCase(Locale.ENGLISH))) {
         return true;
      } else {
         sign.setLine(0, ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("signFormatFail", this.signName)));
         SignCreateEvent signEvent = new SignCreateEvent(sign, this, user);
         ess.getServer().getPluginManager().callEvent(signEvent);
         if (signEvent.isCancelled()) {
            if (ess.getSettings().isDebug()) {
               ess.getLogger().info("SignCreateEvent cancelled for sign " + signEvent.getEssentialsSign().getName());
            }

            return false;
         } else {
            try {
               boolean ret = this.onSignCreate(sign, user, this.getUsername(user), ess);
               if (ret) {
                  sign.setLine(0, this.getSuccessName(ess));
               }

               return ret;
            } catch (SignException | ChargeException var7) {
               this.showError(ess, user.getSource(), var7, this.signName);
               this.setOwnerData(ess, user, sign);
               return true;
            }
         }
      }
   }

   public String getSuccessName(final IEssentials ess) {
      String successName = this.getSuccessName();
      if (successName == null) {
         ess.getLogger().severe("signFormatSuccess message must use the {0} argument.");
      }

      return successName;
   }

   public String getSuccessName() {
      String successName = AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("signFormatSuccess", this.signName));
      if (successName.isEmpty() || !successName.contains(this.signName)) {
         successName = null;
      }

      return successName;
   }

   public String getTemplateName() {
      return AdventureUtil.getAdventureFacet().miniToLegacy(I18n.tlLiteral("signFormatTemplate", this.signName));
   }

   public String getName() {
      return this.signName;
   }

   public String getUsername(final User user) {
      return user.getName().substring(0, Math.min(user.getName().length(), 13));
   }

   public void setOwner(final IEssentials ess, final User user, final EssentialsSign.ISign signProvider, final int nameIndex, final String namePrefix) {
      this.setOwnerData(ess, user, signProvider);
      signProvider.setLine(nameIndex, namePrefix + this.getUsername(user));
   }

   public void setOwnerData(final IEssentials ess, final User user, final EssentialsSign.ISign signProvider) {
      SignDataProvider dataProvider = ess.provider(SignDataProvider.class);
      if (dataProvider != null) {
         org.bukkit.block.Sign sign = (org.bukkit.block.Sign)signProvider.getBlock().getState();
         dataProvider.setSignData(sign, "sign-owner", user.getUUID().toString());
      }
   }

   public boolean isOwner(final IEssentials ess, final User user, final EssentialsSign.ISign signProvider, final int nameIndex, final String namePrefix) {
      SignDataProvider dataProvider = ess.provider(SignDataProvider.class);
      org.bukkit.block.Sign sign = (org.bukkit.block.Sign)signProvider.getBlock().getState();
      if (dataProvider != null && dataProvider.getSignData(sign, "sign-owner") != null) {
         if (user.getUUID().toString().equals(dataProvider.getSignData(sign, "sign-owner"))) {
            signProvider.setLine(nameIndex, namePrefix + this.getUsername(user));
            return true;
         } else {
            return false;
         }
      } else {
         boolean isLegacyOwner = FormatUtil.stripFormat(signProvider.getLine(nameIndex)).equalsIgnoreCase(this.getUsername(user));
         if (dataProvider != null && isLegacyOwner) {
            dataProvider.setSignData(sign, "sign-owner", user.getUUID().toString());
         }

         return isLegacyOwner;
      }
   }

   protected final boolean onSignInteract(final Block block, final Player player, final IEssentials ess) {
      EssentialsSign.ISign sign = new EssentialsSign.BlockSign(block);
      User user = ess.getUser(player);
      if (user.checkSignThrottle()) {
         return false;
      } else {
         try {
            if (!user.getBase().isDead()
               && (
                  user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".use")
                     || user.isAuthorized("essentials.signs.use." + this.signName.toLowerCase(Locale.ENGLISH))
               )) {
               SignInteractEvent signEvent = new SignInteractEvent(sign, this, user);
               ess.getServer().getPluginManager().callEvent(signEvent);
               return signEvent.isCancelled() ? false : this.onSignInteract(sign, user, this.getUsername(user), ess);
            } else {
               return false;
            }
         } catch (Exception var7) {
            this.showError(ess, user.getSource(), var7, this.signName);
            return false;
         }
      }
   }

   protected final boolean onSignBreak(final Block block, final Player player, final IEssentials ess) throws MaxMoneyException {
      EssentialsSign.ISign sign = new EssentialsSign.BlockSign(block);
      User user = ess.getUser(player);

      try {
         if (!user.isAuthorized("essentials.signs." + this.signName.toLowerCase(Locale.ENGLISH) + ".break")
            && !user.isAuthorized("essentials.signs.break." + this.signName.toLowerCase(Locale.ENGLISH))) {
            return false;
         } else {
            SignBreakEvent signEvent = new SignBreakEvent(sign, this, user);
            ess.getServer().getPluginManager().callEvent(signEvent);
            return signEvent.isCancelled() ? false : this.onSignBreak(sign, user, this.getUsername(user), ess);
         }
      } catch (SignException var7) {
         this.showError(ess, user.getSource(), var7, this.signName);
         return false;
      }
   }

   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException {
      return true;
   }

   protected boolean onSignBreak(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException {
      return true;
   }

   protected final boolean onBlockPlace(final Block block, final Player player, final IEssentials ess) {
      User user = ess.getUser(player);

      try {
         return this.onBlockPlace(block, user, this.getUsername(user), ess);
      } catch (SignException | ChargeException var6) {
         this.showError(ess, user.getSource(), var6, this.signName);
         return false;
      }
   }

   protected final boolean onBlockInteract(final Block block, final Player player, final IEssentials ess) {
      User user = ess.getUser(player);

      try {
         return this.onBlockInteract(block, user, this.getUsername(user), ess);
      } catch (SignException | ChargeException var6) {
         this.showError(ess, user.getSource(), var6, this.signName);
         return false;
      }
   }

   protected final boolean onBlockBreak(final Block block, final Player player, final IEssentials ess) throws MaxMoneyException {
      User user = ess.getUser(player);

      try {
         return this.onBlockBreak(block, user, this.getUsername(user), ess);
      } catch (SignException var6) {
         this.showError(ess, user.getSource(), var6, this.signName);
         return false;
      }
   }

   protected boolean onBlockBreak(final Block block, final IEssentials ess) {
      return true;
   }

   protected boolean onBlockExplode(final Block block, final IEssentials ess) {
      return true;
   }

   protected boolean onBlockBurn(final Block block, final IEssentials ess) {
      return true;
   }

   protected boolean onBlockIgnite(final Block block, final IEssentials ess) {
      return true;
   }

   protected boolean onBlockPush(final Block block, final IEssentials ess) {
      return true;
   }

   protected boolean onBlockPlace(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onBlockInteract(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      return true;
   }

   protected boolean onBlockBreak(final Block block, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException {
      return true;
   }

   public Set<Material> getBlocks() {
      return EMPTY_SET;
   }

   public boolean areHeavyEventRequired() {
      return false;
   }

   private String getSignText(final EssentialsSign.ISign sign, final int lineNumber) {
      return sign.getLine(lineNumber).trim();
   }

   protected final void validateTrade(final EssentialsSign.ISign sign, final int index, final IEssentials ess) throws SignException {
      String line = this.getSignText(sign, index);
      if (!line.isEmpty()) {
         Trade trade = this.getTrade(sign, index, 0, ess);
         BigDecimal money = trade.getMoney();
         if (money != null) {
            sign.setLine(index, NumberUtil.shortCurrency(money, ess));
         }
      }
   }

   protected final void validateTrade(final EssentialsSign.ISign sign, final int amountIndex, final int itemIndex, final User player, final IEssentials ess) throws SignException {
      String itemType = this.getSignText(sign, itemIndex);
      if (!itemType.equalsIgnoreCase("exp") && !itemType.equalsIgnoreCase("xp")) {
         Trade trade = this.getTrade(sign, amountIndex, itemIndex, player, ess);
         ItemStack item = trade.getItemStack();
         sign.setLine(amountIndex, Integer.toString(item.getAmount()));
         sign.setLine(itemIndex, itemType);
      } else {
         int amount = this.getIntegerPositive(this.getSignText(sign, amountIndex));
         sign.setLine(amountIndex, Integer.toString(amount));
         sign.setLine(itemIndex, "exp");
      }
   }

   protected final Trade getTrade(final EssentialsSign.ISign sign, final int amountIndex, final int itemIndex, final User player, final IEssentials ess) throws SignException {
      return this.getTrade(sign, amountIndex, itemIndex, player, false, ess);
   }

   protected final Trade getTrade(
      final EssentialsSign.ISign sign, final int amountIndex, final int itemIndex, final User player, final boolean allowId, final IEssentials ess
   ) throws SignException {
      String itemType = this.getSignText(sign, itemIndex);
      if (!itemType.equalsIgnoreCase("exp") && !itemType.equalsIgnoreCase("xp")) {
         ItemStack item = this.getItemStack(itemType, 1, allowId, ess);
         int amount = Math.min(
            this.getIntegerPositive(this.getSignText(sign, amountIndex)), item.getType().getMaxStackSize() * player.getBase().getInventory().getSize()
         );
         if (item.getType() != Material.AIR && amount >= 1) {
            item.setAmount(amount);
            return new Trade(item, ess);
         } else {
            throw new SignException("moreThanZero");
         }
      } else {
         int amount = this.getIntegerPositive(this.getSignText(sign, amountIndex));
         return new Trade(amount, ess);
      }
   }

   protected final void validateInteger(final EssentialsSign.ISign sign, final int index) throws SignException {
      String line = this.getSignText(sign, index);
      if (line.isEmpty()) {
         throw new SignException("emptySignLine", index + 1);
      } else {
         int quantity = this.getIntegerPositive(line);
         sign.setLine(index, Integer.toString(quantity));
      }
   }

   protected final int getIntegerPositive(final String line) throws SignException {
      int quantity = this.getInteger(line);
      if (quantity < 1) {
         throw new SignException("moreThanZero");
      } else {
         return quantity;
      }
   }

   protected final int getInteger(final String line) throws SignException {
      try {
         return Integer.parseInt(line);
      } catch (NumberFormatException var3) {
         throw new SignException("invalidSign");
      }
   }

   protected final ItemStack getItemStack(final String itemName, final int quantity, final IEssentials ess) throws SignException {
      return this.getItemStack(itemName, quantity, false, ess);
   }

   protected final ItemStack getItemStack(final String itemName, final int quantity, final boolean allowId, final IEssentials ess) throws SignException {
      if (allowId && ess.getSettings().allowOldIdSigns()) {
         Material newMaterial = ess.getItemDb().getFromLegacy(itemName);
         if (newMaterial != null) {
            return new ItemStack(newMaterial, quantity);
         }
      }

      try {
         ItemStack item = ess.getItemDb().get(itemName);
         item.setAmount(quantity);
         return item;
      } catch (Exception var7) {
         if (var7 instanceof TranslatableException) {
            TranslatableException te = (TranslatableException)var7;
            throw new SignException(var7, te.getTlKey(), te.getArgs());
         } else {
            throw new SignException(var7, "errorWithMessage", var7.getMessage());
         }
      }
   }

   protected final ItemStack getItemMeta(final ItemStack item, final String meta, final IEssentials ess) throws SignException {
      return this.getItemMeta(null, item, meta, ess);
   }

   protected final ItemStack getItemMeta(final CommandSource source, final ItemStack item, final String meta, final IEssentials ess) throws SignException {
      ItemStack stack = item;

      try {
         if (!meta.isEmpty()) {
            MetaItemStack metaStack = new MetaItemStack(stack);
            boolean allowUnsafe = ess.getSettings().allowUnsafeEnchantments();
            metaStack.addStringMeta(source, allowUnsafe, meta, ess);
            stack = metaStack.getItemStack();
         }

         return stack;
      } catch (Exception var8) {
         throw new SignException(var8, "errorWithMessage", var8.getMessage());
      }
   }

   protected final BigDecimal getMoney(final String line, final IEssentials ess) throws SignException {
      boolean isMoney = line.matches("^[^0-9-.]?[.0-9]+[^0-9-.]?$");
      return isMoney ? this.getBigDecimalPositive(line, ess) : null;
   }

   protected final BigDecimal getBigDecimalPositive(final String line, final IEssentials ess) throws SignException {
      BigDecimal quantity = this.getBigDecimal(line, ess);
      if (quantity.compareTo(MINTRANSACTION) < 0) {
         throw new SignException("moreThanZero");
      } else {
         return quantity;
      }
   }

   protected final BigDecimal getBigDecimal(final String line, final IEssentials ess) throws SignException {
      try {
         return new BigDecimal(NumberUtil.sanitizeCurrencyString(line, ess));
      } catch (NumberFormatException | ArithmeticException var4) {
         throw new SignException(var4, "errorWithMessage", var4.getMessage());
      }
   }

   protected final Trade getTrade(final EssentialsSign.ISign sign, final int index, final IEssentials ess) throws SignException {
      return this.getTrade(sign, index, 1, ess);
   }

   protected final Trade getTrade(final EssentialsSign.ISign sign, final int index, final int decrement, final IEssentials ess) throws SignException {
      return this.getTrade(sign, index, decrement, false, ess);
   }

   protected final Trade getTrade(final EssentialsSign.ISign sign, final int index, final int decrement, final boolean allowId, final IEssentials ess) throws SignException {
      String line = this.getSignText(sign, index);
      if (line.isEmpty()) {
         return new Trade(this.signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
      } else {
         BigDecimal money = this.getMoney(line, ess);
         if (money == null) {
            String[] split = line.split("[ :]+", 2);
            if (split.length != 2) {
               throw new SignException("invalidCharge");
            } else {
               int quantity = this.getIntegerPositive(split[0]);
               String item = split[1].toLowerCase(Locale.ENGLISH);
               if (item.equalsIgnoreCase("times")) {
                  sign.setLine(index, quantity - decrement + " times");
                  sign.updateSign();
                  return new Trade(this.signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
               } else if (!item.equalsIgnoreCase("exp") && !item.equalsIgnoreCase("xp")) {
                  ItemStack stack = this.getItemStack(item, quantity, allowId, ess);
                  sign.setLine(index, quantity + " " + item);
                  return new Trade(stack, ess);
               } else {
                  sign.setLine(index, quantity + " exp");
                  return new Trade(quantity, ess);
               }
            }
         } else {
            return new Trade(money, ess);
         }
      }
   }

   private void showError(final IEssentials ess, final CommandSource sender, final Throwable exception, final String signName) {
      ess.showError(sender, exception, "\\ sign: " + signName);
   }

   static class BlockSign implements EssentialsSign.ISign {
      private final transient Block block;
      private transient org.bukkit.block.Sign sign;

      BlockSign(final Block block) {
         this.block = block;
         this.sign = this.getSign();
      }

      @Override
      public final String getLine(final int index) {
         StringBuilder builder = new StringBuilder();

         for (char c : this.getSign().getLine(index).toCharArray()) {
            if (c < '\uf700' || c > '\uf747') {
               builder.append(c);
            }
         }

         return builder.toString();
      }

      @Override
      public final void setLine(final int index, final String text) {
         this.sign.setLine(index, text);
         this.updateSign();
      }

      @Override
      public final Block getBlock() {
         return this.block;
      }

      private org.bukkit.block.Sign getSign() {
         return (org.bukkit.block.Sign)this.block.getState();
      }

      @Override
      public final void updateSign() {
         this.sign.update();
         this.sign = this.getSign();
      }
   }

   static class EventSign implements EssentialsSign.ISign {
      private final transient SignChangeEvent event;
      private final transient Block block;
      private transient org.bukkit.block.Sign sign;

      EventSign(final SignChangeEvent event) {
         this.event = event;
         this.block = event.getBlock();
         this.sign = (org.bukkit.block.Sign)this.block.getState();
      }

      @Override
      public final String getLine(final int index) {
         StringBuilder builder = new StringBuilder();

         for (char c : this.event.getLine(index).toCharArray()) {
            if (c < '\uf700' || c > '\uf747') {
               builder.append(c);
            }
         }

         return builder.toString();
      }

      @Override
      public final void setLine(final int index, final String text) {
         this.event.setLine(index, text);
         this.sign.setLine(index, text);
         this.updateSign();
      }

      @Override
      public Block getBlock() {
         return this.block;
      }

      @Override
      public void updateSign() {
         this.sign.update();
         this.sign = (org.bukkit.block.Sign)this.block.getState();
      }
   }

   public interface ISign {
      String getLine(final int index);

      void setLine(final int index, final String text);

      Block getBlock();

      void updateSign();
   }
}
