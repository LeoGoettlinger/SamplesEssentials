package me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit;

import com.destroystokyo.paper.Title.Builder;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import me.clip.placeholderapi.libs.kyori.adventure.platform.facet.Facet;
import me.clip.placeholderapi.libs.kyori.adventure.platform.facet.FacetBase;
import me.clip.placeholderapi.libs.kyori.adventure.platform.facet.Knob;
import me.clip.placeholderapi.libs.kyori.adventure.text.Component;
import me.clip.placeholderapi.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PaperFacet<V extends CommandSender> extends FacetBase<V> {
   private static final boolean SUPPORTED = Knob.isEnabled("paper", true);
   static final Class<?> NATIVE_COMPONENT_CLASS = MinecraftReflection.findClass(String.join(".", "net", "kyori", "adventure", "text", "Component"));
   private static final MethodHandle PAPER_ADVENTURE_AS_VANILLA = findAsVanillaMethod();
   private static final Class<?> NATIVE_GSON_COMPONENT_SERIALIZER_CLASS = MinecraftReflection.findClass(
      String.join(".", "net", "kyori", "adventure", "text", "serializer", "gson", "GsonComponentSerializer")
   );
   private static final Class<?> NATIVE_GSON_COMPONENT_SERIALIZER_IMPL_CLASS = MinecraftReflection.findClass(
      String.join(".", "net", "kyori", "adventure", "text", "serializer", "gson", "GsonComponentSerializerImpl")
   );
   private static final MethodHandle NATIVE_GSON_COMPONENT_SERIALIZER_GSON_GETTER = MinecraftReflection.findStaticMethod(
      NATIVE_GSON_COMPONENT_SERIALIZER_CLASS, "gson", NATIVE_GSON_COMPONENT_SERIALIZER_CLASS
   );
   private static final MethodHandle NATIVE_GSON_COMPONENT_SERIALIZER_DESERIALIZE_METHOD = findNativeDeserializeMethod();

   @Nullable
   private static MethodHandle findAsVanillaMethod() {
      try {
         Class<?> paperAdventure = MinecraftReflection.findClass("io.papermc.paper.adventure.PaperAdventure");
         Method method = paperAdventure.getDeclaredMethod("asVanilla", NATIVE_COMPONENT_CLASS);
         return MinecraftReflection.lookup().unreflect(method);
      } catch (IllegalAccessException | NullPointerException | NoSuchMethodException var2) {
         return null;
      }
   }

   @Nullable
   private static MethodHandle findNativeDeserializeMethod() {
      try {
         Method method = NATIVE_GSON_COMPONENT_SERIALIZER_IMPL_CLASS.getDeclaredMethod("deserialize", String.class);
         method.setAccessible(true);
         return MinecraftReflection.lookup().unreflect(method);
      } catch (IllegalAccessException | NullPointerException | NoSuchMethodException var1) {
         return null;
      }
   }

   protected PaperFacet(@Nullable final Class<? extends V> viewerClass) {
      super(viewerClass);
   }

   @Override
   public boolean isSupported() {
      return super.isSupported() && SUPPORTED;
   }

   static class TabList extends CraftBukkitFacet.TabList {
      private static final boolean SUPPORTED = MinecraftReflection.hasField(CLASS_CRAFT_PLAYER, PaperFacet.NATIVE_COMPONENT_CLASS, "playerListHeader")
         && MinecraftReflection.hasField(CLASS_CRAFT_PLAYER, PaperFacet.NATIVE_COMPONENT_CLASS, "playerListFooter");
      private static final MethodHandle NATIVE_GSON_COMPONENT_SERIALIZER_DESERIALIZE_METHOD_BOUND = createBoundNativeDeserializeMethodHandle();

      @Nullable
      private static MethodHandle createBoundNativeDeserializeMethodHandle() {
         if (SUPPORTED) {
            try {
               return PaperFacet.NATIVE_GSON_COMPONENT_SERIALIZER_DESERIALIZE_METHOD
                  .bindTo((Object)PaperFacet.NATIVE_GSON_COMPONENT_SERIALIZER_GSON_GETTER.invoke());
            } catch (Throwable var1) {
               Knob.logError(var1, "Failed to access native GsonComponentSerializer");
               return null;
            }
         } else {
            return null;
         }
      }

      @Override
      public boolean isSupported() {
         return SUPPORTED
            && super.isSupported()
            && (
               CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER != null && CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER != null
                  || PaperFacet.PAPER_ADVENTURE_AS_VANILLA != null
            );
      }

      @Override
      protected Object create117Packet(final Player viewer, @Nullable final Object header, @Nullable final Object footer) throws Throwable {
         if (CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER == null && CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER == null) {
            return (Object)CLIENTBOUND_TAB_LIST_PACKET_CTOR.invoke(
               (Object)(Object)PaperFacet.PAPER_ADVENTURE_AS_VANILLA.invoke((Object)(header == null ? this.createMessage(viewer, Component.empty()) : header)),
               (Object)(Object)PaperFacet.PAPER_ADVENTURE_AS_VANILLA.invoke((Object)(footer == null ? this.createMessage(viewer, Component.empty()) : footer))
            );
         } else {
            Object packet = (Object)CLIENTBOUND_TAB_LIST_PACKET_CTOR.invoke((Void)null, (Void)null);
            CLIENTBOUND_TAB_LIST_PACKET_SET_HEADER.invoke((Object)packet, (Object)(header == null ? this.createMessage(viewer, Component.empty()) : header));
            CLIENTBOUND_TAB_LIST_PACKET_SET_FOOTER.invoke((Object)packet, (Object)(footer == null ? this.createMessage(viewer, Component.empty()) : footer));
            return packet;
         }
      }

      @Nullable
      public Object createMessage(@NotNull final Player viewer, @NotNull final Component message) {
         try {
            return (Object)NATIVE_GSON_COMPONENT_SERIALIZER_DESERIALIZE_METHOD_BOUND.invoke((String)GsonComponentSerializer.gson().serialize(message));
         } catch (Throwable var4) {
            Knob.logError(var4, "Failed to create native Component message");
            return null;
         }
      }
   }

   static class Title extends SpigotFacet.Message<Player> implements Facet.Title<Player, BaseComponent[], Builder, com.destroystokyo.paper.Title> {
      private static final boolean SUPPORTED = MinecraftReflection.hasClass("com.destroystokyo.paper.Title");

      protected Title() {
         super(Player.class);
      }

      @Override
      public boolean isSupported() {
         return super.isSupported() && SUPPORTED;
      }

      @NotNull
      public Builder createTitleCollection() {
         return com.destroystokyo.paper.Title.builder();
      }

      public void contributeTitle(@NotNull final Builder coll, @NotNull final BaseComponent[] title) {
         coll.title(title);
      }

      public void contributeSubtitle(@NotNull final Builder coll, @NotNull final BaseComponent[] subtitle) {
         coll.subtitle(subtitle);
      }

      public void contributeTimes(@NotNull final Builder coll, final int inTicks, final int stayTicks, final int outTicks) {
         if (inTicks > -1) {
            coll.fadeIn(inTicks);
         }

         if (stayTicks > -1) {
            coll.stay(stayTicks);
         }

         if (outTicks > -1) {
            coll.fadeOut(outTicks);
         }
      }

      @Nullable
      public com.destroystokyo.paper.Title completeTitle(@NotNull final Builder coll) {
         return coll.build();
      }

      public void showTitle(@NotNull final Player viewer, @NotNull final com.destroystokyo.paper.Title title) {
         viewer.sendTitle(title);
      }

      public void clearTitle(@NotNull final Player viewer) {
         viewer.hideTitle();
      }

      public void resetTitle(@NotNull final Player viewer) {
         viewer.resetTitle();
      }
   }
}
