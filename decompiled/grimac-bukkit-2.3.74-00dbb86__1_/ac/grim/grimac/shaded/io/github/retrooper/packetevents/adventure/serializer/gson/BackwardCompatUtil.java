package ac.grim.grimac.shaded.io.github.retrooper.packetevents.adventure.serializer.gson;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.jetbrains.annotations.Range;
import ac.grim.grimac.shaded.kyori.adventure.key.Key;
import ac.grim.grimac.shaded.kyori.adventure.nbt.api.BinaryTagHolder;
import ac.grim.grimac.shaded.kyori.adventure.text.BuildableComponent;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import ac.grim.grimac.shaded.kyori.adventure.text.ComponentBuilder;
import ac.grim.grimac.shaded.kyori.adventure.text.event.ClickEvent;
import ac.grim.grimac.shaded.kyori.adventure.text.event.HoverEvent;
import ac.grim.grimac.shaded.kyori.adventure.text.format.Style;
import ac.grim.grimac.shaded.kyori.adventure.text.object.ObjectContents;
import ac.grim.grimac.shaded.kyori.adventure.util.Codec;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

public final class BackwardCompatUtil {
   public static final boolean IS_4_10_0_OR_NEWER;
   public static final boolean IS_4_13_0_OR_NEWER;
   public static final boolean IS_4_15_0_OR_NEWER;
   public static final boolean IS_4_17_0_OR_NEWER;
   public static final boolean IS_4_18_0_OR_NEWER;
   public static final boolean IS_4_22_0_OR_NEWER;
   public static final boolean IS_4_23_0_OR_NEWER;
   public static final boolean IS_4_25_0_OR_NEWER;
   public static final boolean IS_4_26_0_OR_NEWER;
   private static final Method COMPONENT_BUILDER_BUILD;

   private BackwardCompatUtil() {
   }

   public static HoverEvent.ShowItem createShowItem(@NotNull Key item, @Range(from = 0L,to = 2147483647L) int count, @Nullable BinaryTagHolder nbt) {
      try {
         return HoverEvent.ShowItem.showItem(item, count, nbt);
      } catch (NoSuchMethodError var4) {
         return HoverEvent.ShowItem.of(item, count, nbt);
      }
   }

   public static HoverEvent.ShowEntity createShowEntity(@NotNull Key type, @NotNull UUID id, @Nullable Component name) {
      try {
         return HoverEvent.ShowEntity.showEntity(type, id, name);
      } catch (NoSuchMethodError var4) {
         return HoverEvent.ShowEntity.of(type, id, name);
      }
   }

   @NotNull
   public static <D, E, DX extends Throwable, EX extends Throwable> Codec<D, E, DX, EX> createCodec(
      @NotNull Codec.Decoder<D, E, DX> decoder, @NotNull Codec.Encoder<D, E, EX> encoder
   ) {
      try {
         return Codec.codec(decoder, encoder);
      } catch (NoSuchMethodError var3) {
         return Codec.of(decoder, encoder);
      }
   }

   public static ComponentBuilder<?, ?> toBuilder(Component component) {
      return IS_4_26_0_OR_NEWER ? component.toBuilder() : ((BuildableComponent)component).toBuilder();
   }

   public static Component build(ComponentBuilder<?, ?> builder) {
      try {
         return (Component)COMPONENT_BUILDER_BUILD.invoke(builder);
      } catch (ReflectiveOperationException var2) {
         throw new RuntimeException(var2);
      }
   }

   static {
      boolean is4_10_0OrNewer = false;

      try {
         BinaryTagHolder.binaryTagHolder("");
         is4_10_0OrNewer = true;
      } catch (Throwable var19) {
      }

      IS_4_10_0_OR_NEWER = is4_10_0OrNewer;
      boolean is4_13_0OrNewer = false;

      try {
         Component.translatable().fallback("");
         is4_13_0OrNewer = true;
      } catch (Throwable var18) {
      }

      IS_4_13_0_OR_NEWER = is4_13_0OrNewer;
      boolean is4_15_0OrNewer = false;

      try {
         Component.translatable().arguments(Component.empty());
         is4_15_0OrNewer = true;
      } catch (Throwable var17) {
      }

      IS_4_15_0_OR_NEWER = is4_15_0OrNewer;
      boolean is4_17_0OrNewer = false;

      try {
         HoverEvent.ShowItem.showItem(Key.key("air"), 1, Collections.emptyMap());
         is4_17_0OrNewer = true;
      } catch (Throwable var16) {
      }

      IS_4_17_0_OR_NEWER = is4_17_0OrNewer;
      boolean is4_18_0OrNewer = false;

      try {
         Style.empty().shadowColor();
         is4_18_0OrNewer = true;
      } catch (Throwable var15) {
      }

      IS_4_18_0_OR_NEWER = is4_18_0OrNewer;
      boolean is4_23_0OrNewer = false;

      try {
         ClickEvent.custom(Key.key("test"), BinaryTagHolder.binaryTagHolder("{test:true}"));
         is4_23_0OrNewer = true;
      } catch (Throwable var14) {
      }

      IS_4_23_0_OR_NEWER = is4_23_0OrNewer;
      boolean is4_22_0OrNewer = is4_23_0OrNewer;

      try {
         ClickEvent.custom(Key.key("test"), "{test:true}");
         is4_22_0OrNewer = true;
      } catch (Throwable var13) {
      }

      IS_4_22_0_OR_NEWER = is4_22_0OrNewer;
      boolean is4_25_0OrNewer = false;

      try {
         ObjectContents.playerHead();
         is4_25_0OrNewer = true;
      } catch (Throwable var12) {
      }

      IS_4_25_0_OR_NEWER = is4_25_0OrNewer;
      boolean is4_26_0OrNewer = false;

      try {
         Component component = Component.text(42);
         component.toBuilder();
         is4_26_0OrNewer = true;
      } catch (Throwable var11) {
      }

      IS_4_26_0_OR_NEWER = is4_26_0OrNewer;

      try {
         COMPONENT_BUILDER_BUILD = ComponentBuilder.class.getMethod("build");
      } catch (ReflectiveOperationException var10) {
         throw new RuntimeException(var10);
      }
   }

   public interface ShowAchievementToComponent {
      @NotNull
      Component convert(@NotNull String var1);
   }
}
