package su.nightexpress.nightcore.util.bridge;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.bridge.bossbar.NightBarColor;
import su.nightexpress.nightcore.bridge.bossbar.NightBarFlag;
import su.nightexpress.nightcore.bridge.bossbar.NightBarOverlay;
import su.nightexpress.nightcore.bridge.bossbar.NightBossBar;
import su.nightexpress.nightcore.bridge.chat.UniversalChatListenerCallback;
import su.nightexpress.nightcore.bridge.dialog.response.DialogClickHandler;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.event.EventAdapter;
import su.nightexpress.nightcore.bridge.key.AdaptedKey;
import su.nightexpress.nightcore.bridge.scheduler.AdaptedScheduler;
import su.nightexpress.nightcore.bridge.text.adapter.TextComponentAdapter;
import su.nightexpress.nightcore.bridge.wrap.NightProfile;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;

@NullMarked
public interface Software {
   Software.Holder INSTANCE = new Software.Holder();

   @Deprecated
   static Software instance() {
      return get();
   }

   static Software get() {
      return INSTANCE.get();
   }

   boolean initialize();

   String getName();

   boolean isPaper();

   int nextEntityId();

   EventAdapter eventAdapter();

   Listener createChatListener(UniversalChatListenerCallback var1);

   Listener createDialogListener(DialogClickHandler var1);

   AdaptedScheduler getScheduler(JavaPlugin var1);

   void disallowLogin(AsyncPlayerPreLoginEvent var1, Result var2, NightComponent var3);

   void closeDialog(Player var1);

   void showDialog(Player var1, WrappedDialog var2);

   TextComponentAdapter<?> getTextComponentAdapter();

   SimpleCommandMap getCommandMap();

   Map<String, Command> getKnownCommands(SimpleCommandMap var1);

   InventoryView createView(MenuType var1, NightComponent var2, Player var3);

   boolean isValidKeyNamespace(String var1);

   boolean isValidKeyValue(String var1);

   boolean allowedInNamespace(char var1);

   boolean allowedInValue(char var1);

   AdaptedKey createKey(Plugin var1, String var2);

   AdaptedKey createKey(String var1, String var2);

   AdaptedKey createKey(String var1);

   AdaptedKey getKey(World var1);

   NightProfile createProfile(UUID var1);

   NightProfile createProfile(String var1);

   NightProfile createProfile(@Nullable UUID var1, @Nullable String var2);

   NightProfile getProfile(OfflinePlayer var1);

   void sendTitles(Player var1, NightComponent var2, NightComponent var3, int var4, int var5, int var6);

   Set<String> getCommonComponentsToHide();

   Set<String> getHiddenComponents(ItemStack var1);

   String getTranslationKey(Material var1);

   String getTranslationKey(Attribute var1);

   String getTranslationKey(Enchantment var1);

   String getTranslationKey(EntityType var1);

   String getTranslationKey(PotionEffectType var1);

   String getDisplayNameSerialized(Player var1);

   void setDisplayName(Player var1, @Nullable NightComponent var2);

   @Nullable
   String getPlayerListHeaderSerialized(Player var1);

   @Nullable
   String getPlayerListFooterSerialized(Player var1);

   void setPlayerListHeaderFooter(Player var1, @Nullable NightComponent var2, @Nullable NightComponent var3);

   String getPlayerListNameSerialized(Player var1);

   void setPlayerListName(Player var1, NightComponent var2);

   void kick(Player var1, @Nullable NightComponent var2);

   void setCustomName(Nameable var1, @Nullable NightComponent var2);

   @Nullable
   String getCustomName(Nameable var1);

   ItemStack setType(ItemStack var1, Material var2);

   void editMeta(ItemStack var1, Consumer<ItemMeta> var2);

   <T extends ItemMeta> void editMeta(ItemStack var1, Class<T> var2, Consumer<T> var3);

   @Nullable
   String getCustomName(ItemMeta var1);

   void setCustomName(ItemMeta var1, @Nullable NightComponent var2);

   @Nullable
   String getItemName(ItemMeta var1);

   void setItemName(ItemMeta var1, NightComponent var2);

   @Nullable
   List<String> getLore(ItemMeta var1);

   void setLore(ItemMeta var1, @Nullable List<NightComponent> var2);

   @Nullable
   NightProfile getOwnerProfile(ItemStack var1);

   void hideComponents(ItemStack var1);

   void hideComponents(ItemStack var1, Set<String> var2);

   NightBossBar createBossBar(NightComponent var1, NightBarColor var2, NightBarOverlay var3, NightBarFlag... var4);

   public static final class Holder {
      @Nullable
      private Software software;

      public void load(Software software) {
         if (this.software != null) {
            throw new IllegalStateException("Software is already initialized!");
         } else {
            this.software = software;
            this.software.initialize();
         }
      }

      public Software get() {
         if (this.software == null) {
            throw new IllegalStateException("Software is not yet initialized!");
         } else {
            return this.software;
         }
      }
   }
}
