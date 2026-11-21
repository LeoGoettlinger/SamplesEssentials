package su.nightexpress.nightcore.menu.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.menu.MenuViewer;
import su.nightexpress.nightcore.menu.api.Menu;
import su.nightexpress.nightcore.menu.click.ClickAction;

@Deprecated
public class ItemHandler {
   public static final String RETURN = "return";
   public static final String CLOSE = "close";
   public static final String NEXT_PAGE = "page_next";
   public static final String PREVIOUS_PAGE = "page_previous";
   private final String name;
   private final List<ClickAction> clickActions;
   private final Predicate<MenuViewer> visibilityPolicy;

   public ItemHandler() {
      this("default", null, null);
   }

   public ItemHandler(@NonNull String name) {
      this(name, null, null);
   }

   public ItemHandler(@NonNull String name, @Nullable ClickAction clickAction) {
      this(name, clickAction, null);
   }

   public ItemHandler(@NonNull String name, @Nullable ClickAction clickAction, @Nullable Predicate<MenuViewer> visibilityPolicy) {
      this.name = name.toLowerCase();
      this.clickActions = new ArrayList<>();
      this.visibilityPolicy = visibilityPolicy;
      if (clickAction != null) {
         this.getClickActions().add(clickAction);
      }
   }

   @NonNull
   public static ItemHandler forClick(@NonNull ClickAction action) {
      return new ItemHandler(UUID.randomUUID().toString(), action);
   }

   @NonNull
   public static ItemHandler forNextPage(@NonNull Menu menu) {
      return new ItemHandler("page_next", (viewer, event) -> {
         if (viewer.getPage() < viewer.getPages()) {
            viewer.setPage(viewer.getPage() + 1);
            viewer.setUpdateTitle(true);
            menu.open(viewer.getPlayer());
         }
      }, viewer -> viewer.getPage() < viewer.getPages());
   }

   @NonNull
   public static ItemHandler forPreviousPage(@NonNull Menu menu) {
      return new ItemHandler("page_previous", (viewer, event) -> {
         if (viewer.getPage() > 1) {
            viewer.setPage(viewer.getPage() - 1);
            viewer.setUpdateTitle(true);
            menu.open(viewer.getPlayer());
         }
      }, viewer -> viewer.getPage() > 1);
   }

   @NonNull
   public static ItemHandler forClose(@NonNull Menu menu) {
      return new ItemHandler("close", (viewer, event) -> menu.runNextTick(() -> viewer.getPlayer().closeInventory()));
   }

   @NonNull
   public static ItemHandler forReturn(@NonNull Menu menu, @NonNull ClickAction action) {
      return new ItemHandler("return", action);
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   @NonNull
   public List<ClickAction> getClickActions() {
      return this.clickActions;
   }

   @Nullable
   public Predicate<MenuViewer> getVisibilityPolicy() {
      return this.visibilityPolicy;
   }
}
