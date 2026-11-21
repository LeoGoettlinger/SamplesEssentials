package me.byteful.plugin.leveltools.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.NBTLevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.PDCLevelToolsItem;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.bukkit.BukkitScheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.folia.FoliaScheduler;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import me.byteful.plugin.leveltools.libs.nbtapi.NBTItem;
import me.byteful.plugin.leveltools.libs.xseries.messages.ActionBar;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.display.ProgressBarConfig;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardEntry;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LevelToolsUtil {
   public static final int MID_VERSION = LevelToolsUtil.MINECRAFT_VERSION.getCompatibilityMajor();
   private static final Pattern MINECRAFT_VERSION_PATTERN = Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:[-+].*|\\.(?!\\d).*)?$");
   private static final Pattern SERVER_MINECRAFT_VERSION_PATTERN = Pattern.compile("\\(MC:\\s*([^\\s)]+)\\)");
   private static final String LORE_PREFIX = "§§";
   private static final boolean IS_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
   private static final LevelToolsUtil.MinecraftVersion MINECRAFT_VERSION = resolveMinecraftVersion();

   private static boolean hasClass(String name) {
      try {
         Class.forName(name);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }

   public static boolean isSupportedTool(Material material) {
      LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
      if (instance == null) {
         return false;
      } else {
         ProfileManager profileManager = instance.getProfileManager();
         return profileManager == null ? false : profileManager.hasMaterialProfile(material);
      }
   }

   @Nullable
   public static ItemProfile getItemProfile(Material material) {
      LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
      if (instance == null) {
         return null;
      } else {
         ProfileManager profileManager = instance.getProfileManager();
         return profileManager == null ? null : profileManager.getProfileForMaterial(material);
      }
   }

   public static ItemStack getHand(Player player) {
      return supportsDualWielding() ? player.getInventory().getItemInMainHand().clone() : player.getItemInHand().clone();
   }

   public static void setHand(Player player, ItemStack stack) {
      if (supportsDualWielding()) {
         player.getInventory().setItemInMainHand(stack);
      } else {
         player.setItemInHand(stack);
      }
   }

   public static void setItemInSlot(@NotNull Player player, @Nullable TriggerSlot slot, @NotNull ItemStack stack) {
      if (slot == null || slot == TriggerSlot.HAND) {
         setHand(player, stack);
      } else if (slot == TriggerSlot.OFF_HAND) {
         if (supportsDualWielding()) {
            player.getInventory().setItemInOffHand(stack);
         } else {
            setHand(player, stack);
         }
      } else {
         switch (slot) {
            case HELMET:
               player.getInventory().setHelmet(stack);
               return;
            case CHESTPLATE:
               player.getInventory().setChestplate(stack);
               return;
            case LEGGINGS:
               player.getInventory().setLeggings(stack);
               return;
            case BOOTS:
               player.getInventory().setBoots(stack);
               return;
            default:
               setHand(player, stack);
         }
      }
   }

   public static String createProgressBar(double xp, double maxXp, @Nullable DisplayProfile displayProfile) {
      return displayProfile != null
         ? displayProfile.getProgressBar().buildProgressBar(xp, maxXp)
         : ProgressBarConfig.defaultConfig().buildProgressBar(xp, maxXp);
   }

   public static double round(double value, int places) {
      if (places < 0) {
         throw new IllegalArgumentException();
      } else {
         BigDecimal bd = BigDecimal.valueOf(value);
         bd = bd.setScale(places, RoundingMode.HALF_UP);
         return bd.doubleValue();
      }
   }

   public static int roundDown(double value) {
      BigDecimal bd = BigDecimal.valueOf(value);
      bd = bd.setScale(1, RoundingMode.DOWN);
      return bd.intValue();
   }

   public static LevelToolsItem createLevelToolsItem(ItemStack stack) {
      if (supportsPersistentDataContainer()) {
         if (shouldCheckLegacyNbtData()) {
            NBTItem nbt = new NBTItem(stack);
            if (nbt.getKeys().stream().anyMatch(s -> s.startsWith("levelTools"))) {
               return new NBTLevelToolsItem(stack);
            }
         }

         return new PDCLevelToolsItem(stack);
      } else {
         return new NBTLevelToolsItem(stack);
      }
   }

   public static String getServerVersion() {
      String version = Bukkit.getVersion();
      String[] split = version.split(" ");
      return split[split.length - 1].trim().replace(")", "");
   }

   public static boolean requiresLegacyAnvilListener() {
      return isMinecraftVersionBefore(1, 9);
   }

   public static boolean supportsDualWielding() {
      return isMinecraftVersionAtLeast(1, 9);
   }

   public static boolean supportsTranslatableItemDisplayNames() {
      return isMinecraftVersionAtLeast(1, 13);
   }

   public static boolean supportsBlockData() {
      return isMinecraftVersionAtLeast(1, 13);
   }

   public static boolean supportsPersistentDataContainer() {
      return isMinecraftVersionAtLeast(1, 14);
   }

   public static boolean shouldCheckLegacyNbtData() {
      return supportsPersistentDataContainer() && isMinecraftVersionBefore(1, 18);
   }

   public static boolean supportsSpigotActionBar() {
      return isMinecraftVersionAtLeast(1, 13);
   }

   private static boolean isMinecraftVersionAtLeast(int major, int minor) {
      return MINECRAFT_VERSION.compareTo(new LevelToolsUtil.MinecraftVersion(major, minor, 0)) >= 0;
   }

   private static boolean isMinecraftVersionBefore(int major, int minor) {
      return MINECRAFT_VERSION.compareTo(new LevelToolsUtil.MinecraftVersion(major, minor, 0)) < 0;
   }

   private static LevelToolsUtil.MinecraftVersion resolveMinecraftVersion() {
      String minecraftVersion = getMinecraftVersion();
      if (minecraftVersion != null) {
         return parseMinecraftVersion(minecraftVersion);
      } else {
         String serverVersion = getMinecraftVersionFromServerVersion(Bukkit.getVersion());
         return serverVersion != null ? parseMinecraftVersion(serverVersion) : parseMinecraftVersion(Bukkit.getBukkitVersion());
      }
   }

   @Nullable
   private static String getMinecraftVersion() {
      try {
         Method method = Bukkit.class.getMethod("getMinecraftVersion");
         Object version = method.invoke(null);
         if (version instanceof String) {
            String value = ((String)version).trim();
            if (!value.isEmpty()) {
               return value;
            }
         }
      } catch (SecurityException | ReflectiveOperationException var3) {
      }

      return null;
   }

   @Nullable
   private static String getMinecraftVersionFromServerVersion(@NotNull String version) {
      Matcher matcher = SERVER_MINECRAFT_VERSION_PATTERN.matcher(version);
      return !matcher.find() ? null : matcher.group(1);
   }

   static LevelToolsUtil.MinecraftVersion parseMinecraftVersion(@NotNull String version) {
      Matcher matcher = MINECRAFT_VERSION_PATTERN.matcher(version.trim());
      if (!matcher.matches()) {
         throw new IllegalStateException("Unable to parse Bukkit Minecraft version: " + version);
      } else {
         return new LevelToolsUtil.MinecraftVersion(Integer.parseInt(matcher.group(1)), parseVersionPart(matcher.group(2)), parseVersionPart(matcher.group(3)));
      }
   }

   private static int parseVersionPart(@Nullable String value) {
      return value == null ? 0 : Integer.parseInt(value);
   }

   public static ItemStack buildItemStack(ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp) {
      DisplayProfile displayProfile = getDisplayProfileForMaterial(stack.getType());
      return buildItemStack(stack, enchantments, level, xp, maxXp, displayProfile);
   }

   public static ItemStack buildItemStack(
      ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp, @Nullable DisplayProfile displayProfile
   ) {
      ItemMeta meta = stack.getItemMeta();

      assert meta != null : "ItemMeta is null! Should not happen.";

      String progressBar = createProgressBar(xp, maxXp, displayProfile);
      if (displayProfile != null) {
         DisplayProfile.NameDisplay nameDisplay = displayProfile.getNameDisplay();
         if (nameDisplay.isEnabled()) {
            String text = Text.colorize(
               nameDisplay.getText()
                  .replace("{level}", String.valueOf(level))
                  .replace("{xp}", String.valueOf(xp))
                  .replace("{max_xp}", String.valueOf(maxXp))
                  .replace("{max_xp_formatted}", Text.formatMoney(maxXp))
                  .replace("{xp_formatted}", Text.formatMoney(xp))
                  .replace("{progress_bar}", progressBar)
            );
            if (nameDisplay.getText().contains("{item}") && supportsTranslatableItemDisplayNames() && IS_PAPER) {
               AdventureHelper.setDisplayNameWithTranslatable(meta, text, stack);
            } else {
               meta.setDisplayName(text);
            }
         }

         DisplayProfile.LoreDisplay loreDisplay = displayProfile.getLoreDisplay();
         if (loreDisplay.isEnabled()) {
            List<String> lines = loreDisplay.getLines()
               .stream()
               .map(str -> "§§" + str)
               .map(
                  str -> Text.colorize(
                        str.replace("{level}", String.valueOf(level))
                           .replace("{xp}", String.valueOf(xp))
                           .replace("{max_xp}", String.valueOf(maxXp))
                           .replace("{progress_bar}", progressBar)
                     )
                     .replace("{max_xp_formatted}", Text.formatMoney(maxXp))
                     .replace("{xp_formatted}", Text.formatMoney(xp))
               )
               .collect(Collectors.toList());
            smartSetLore(meta, lines);
         }
      }

      for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
         meta.addEnchant(entry.getKey(), entry.getValue(), true);
      }

      if (LevelToolsPlugin.getInstance().getConfig().getBoolean("hide_attributes", true)) {
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
      }

      stack.setItemMeta(meta);
      return stack;
   }

   @Nullable
   private static DisplayProfile getDisplayProfileForMaterial(Material material) {
      ItemProfile itemProfile = getItemProfile(material);
      if (itemProfile == null) {
         return null;
      } else {
         LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
         if (instance == null) {
            return null;
         } else {
            ProfileManager profileManager = instance.getProfileManager();
            return profileManager == null ? null : profileManager.getDisplayProfileFor(itemProfile);
         }
      }
   }

   private static void smartSetLore(@NotNull ItemMeta meta, @NotNull List<String> toAdd) {
      List<String> lore = meta.getLore();
      if (meta.hasLore() && lore != null) {
         int[] bounds = findPrefixBounds(lore);
         int start = bounds[0];
         int end = bounds[1];
         if (start == -1) {
            lore.addAll(toAdd);
            meta.setLore(lore);
         } else if (end >= lore.size()) {
            meta.setLore(toAdd);
         } else {
            List<String> sub = lore.subList(start, end + 1);
            sub.clear();
            sub.addAll(toAdd);
            meta.setLore(lore);
         }
      } else {
         meta.setLore(toAdd);
      }
   }

   private static int[] findPrefixBounds(@NotNull List<String> lore) {
      int[] arr = new int[]{-1, -1};

      for (int i = 0; i < lore.size(); i++) {
         String text = lore.get(i);
         String line = Text.decolorize(text);
         if (line.startsWith("&&")) {
            if (arr[0] == -1) {
               arr[0] = i;
            }

            arr[1] = i;
         }
      }

      return arr;
   }

   public static void handleReward(LevelToolsItem tool, Player player) {
      Material material = tool.getItemStack().getType();
      ItemProfile itemProfile = getItemProfile(material);
      if (itemProfile != null) {
         LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
         if (instance != null) {
            ProfileManager profileManager = instance.getProfileManager();
            if (profileManager != null) {
               RewardProfile rewardProfile = profileManager.getRewardProfileFor(itemProfile);
               if (rewardProfile != null) {
                  int level = tool.getLevel();
                  if (rewardProfile.hasRewardsForLevel(level)) {
                     if (tool.getLastHandledReward() != level) {
                        tool.setLastHandledReward(level);
                        setHand(player, tool.getItemStack());

                        for (RewardEntry entry : rewardProfile.getRewardsForLevel(level)) {
                           entry.apply(tool, player);
                           if (entry.shouldUpdateItem()) {
                              setHand(player, tool.getItemStack());
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void sendActionBar(Player player, String msg) {
      if (supportsSpigotActionBar()) {
         player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
      } else {
         ActionBar.sendActionBar(player, msg);
      }
   }

   public static Scheduler createScheduler(LevelToolsPlugin plugin) {
      return (Scheduler)(isFolia() ? new FoliaScheduler(plugin) : new BukkitScheduler(plugin));
   }

   private static boolean isFolia() {
      try {
         Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
         return true;
      } catch (ClassNotFoundException var1) {
         return false;
      }
   }

   static final class MinecraftVersion implements Comparable<LevelToolsUtil.MinecraftVersion> {
      private final int major;
      private final int minor;
      private final int patch;

      private MinecraftVersion(int major, int minor, int patch) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
      }

      private int getCompatibilityMajor() {
         return this.major == 1 ? this.minor : this.major;
      }

      public int compareTo(@NotNull LevelToolsUtil.MinecraftVersion other) {
         if (this.major != other.major) {
            return Integer.compare(this.major, other.major);
         } else {
            return this.minor != other.minor ? Integer.compare(this.minor, other.minor) : Integer.compare(this.patch, other.patch);
         }
      }
   }
}
