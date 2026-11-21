package su.nightexpress.nightcore.ui.menu.item;

import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.ui.menu.Menu;
import su.nightexpress.nightcore.ui.menu.data.LinkHandler;
import su.nightexpress.nightcore.ui.menu.data.Linked;

@Deprecated
public class ItemHandler {
   public static final String RETURN = "return";
   public static final String CLOSE = "close";
   public static final String NEXT_PAGE = "page_next";
   public static final String PREVIOUS_PAGE = "page_previous";
   public static final String USER_SKIN = "user_skin";
   private final String name;
   private final ItemClick click;
   private final ItemOptions options;

   public ItemHandler(@NonNull String name) {
      this(name, null, null);
   }

   public ItemHandler(@NonNull String name, @Nullable ItemClick click) {
      this(name, click, null);
   }

   public ItemHandler(@NonNull String name, @Nullable ItemClick click, @Nullable ItemOptions options) {
      this.name = name.toLowerCase();
      this.click = click;
      this.options = options;
   }

   @NonNull
   private static String randomName() {
      return UUID.randomUUID().toString();
   }

   @NonNull
   public static ItemHandler forClick(@NonNull ItemClick click) {
      return forClick(click, null);
   }

   @NonNull
   public static ItemHandler forClick(@NonNull ItemClick click, @Nullable ItemOptions options) {
      return new ItemHandler(randomName(), click, options);
   }

   @NonNull
   public static ItemHandler forUserSkin(@NonNull Menu menu) {
      return new ItemHandler(
         "user_skin",
         (viewer, event) -> {},
         ItemOptions.builder().setDisplayModifier((viewer, nightItem) -> nightItem.setPlayerProfile(viewer.getPlayer())).build()
      );
   }

   @NonNull
   public static ItemHandler forNextPage(@NonNull Menu menu) {
      return new ItemHandler("page_next", (viewer, event) -> {
         if (viewer.getPage() < viewer.getPages()) {
            viewer.setPage(viewer.getPage() + 1);
            viewer.setRebuildMenu(true);
            menu.flush(viewer.getPlayer());
         }
      }, ItemOptions.builder().setVisibilityPolicy(viewer -> viewer.getPage() < viewer.getPages()).build());
   }

   @NonNull
   public static ItemHandler forPreviousPage(@NonNull Menu menu) {
      return new ItemHandler("page_previous", (viewer, event) -> {
         if (viewer.getPage() > 1) {
            viewer.setPage(viewer.getPage() - 1);
            viewer.setRebuildMenu(true);
            menu.flush(viewer.getPlayer());
         }
      }, ItemOptions.builder().setVisibilityPolicy(viewer -> viewer.getPage() > 1).build());
   }

   @NonNull
   public static ItemHandler forClose(@NonNull Menu menu) {
      return new ItemHandler("close", (viewer, event) -> menu.runNextTick(() -> viewer.getPlayer().closeInventory()));
   }

   @NonNull
   public static ItemHandler forReturn(@NonNull Menu menu, @NonNull ItemClick click) {
      return forReturn(menu, click, null);
   }

   @NonNull
   public static ItemHandler forReturn(@NonNull Menu menu, @NonNull ItemClick click, @Nullable ItemOptions options) {
      return new ItemHandler("return", click, options);
   }

   @NonNull
   public static <T> ItemHandler forLink(@NonNull Linked<T> menu, @NonNull LinkHandler<T> handler) {
      return forLink(menu, handler, null);
   }

   @NonNull
   public static <T> ItemHandler forLink(@NonNull Linked<T> menu, @NonNull LinkHandler<T> handler, @Nullable ItemOptions options) {
      return new ItemHandler(randomName(), menu.manageLink(handler), options);
   }

   @NonNull
   public static <T> ItemHandler forLink(@NonNull String name, @NonNull Linked<T> menu, @NonNull LinkHandler<T> handler) {
      return forLink(name, menu, handler, null);
   }

   @NonNull
   public static <T> ItemHandler forLink(@NonNull String name, @NonNull Linked<T> menu, @NonNull LinkHandler<T> handler, @Nullable ItemOptions options) {
      return new ItemHandler(name, menu.manageLink(handler), options);
   }

   @NonNull
   public String getName() {
      return this.name;
   }

   @NonNull
   public ItemClick getClick() {
      return this.click;
   }

   @Nullable
   public ItemOptions getOptions() {
      return this.options;
   }
}
