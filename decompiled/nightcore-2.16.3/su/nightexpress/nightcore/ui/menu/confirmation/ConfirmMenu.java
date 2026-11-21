package su.nightexpress.nightcore.ui.menu.confirmation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.ItemHandler;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.text.tag.Tags;

@Deprecated
public class ConfirmMenu extends LinkedMenu<NightPlugin, Confirmation> implements ConfigBased {
   public static final String FILE_NAME = "confirmation.yml";
   private int iconSlot;

   public ConfirmMenu(@NonNull NightPlugin plugin) {
      super(plugin, MenuType.HOPPER, Tags.BLACK.wrap("Are you sure?"));
      this.load(FileConfig.loadOrExtract(plugin, "/ui/", "confirmation.yml"));
   }

   @Override
   protected void onPrepare(@NonNull MenuViewer viewer, @NonNull InventoryView view) {
      Confirmation confirmation = this.getLink(viewer);
      NightItem icon = confirmation.getIcon();
      if (icon != null) {
         this.addItem(viewer, icon.toMenuItem().setSlots(this.iconSlot).setPriority(100));
      }
   }

   @Override
   protected void onReady(@NonNull MenuViewer viewer, @NonNull Inventory inventory) {
   }

   @Override
   public void loadConfiguration(@NonNull FileConfig config, @NonNull MenuLoader loader) {
      this.iconSlot = ConfigValue.create("Settings.IconSlot", 2).read(config);
      loader.addDefaultItem(
         NightItem.asCustomHead("27548362a24c0fa8453e4d93e68c5969ddbde57bf6666c0319c1ed1e84d89065")
            .setDisplayName(Tags.LIGHT_RED.wrap(Tags.BOLD.wrap("Cancel")))
            .toMenuItem()
            .setPriority(10)
            .setSlots(0)
            .setHandler(new ItemHandler("decline", (viewer, event) -> this.getLink(viewer).handleReturn(viewer, event)))
      );
      loader.addDefaultItem(
         NightItem.asCustomHead("a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756")
            .setDisplayName(Tags.LIGHT_GREEN.wrap(Tags.BOLD.wrap("Accept")))
            .toMenuItem()
            .setPriority(10)
            .setSlots(4)
            .setHandler(new ItemHandler("accept", (viewer, event) -> this.getLink(viewer).handleAccept(viewer, event)))
      );
   }
}
