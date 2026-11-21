package com.sk89q.worldedit.util.formatting.text.adapter.bukkit;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class SpigotAdapter implements Adapter {
   private static final boolean BOUND = bind();

   private static boolean bind() {
      try {
         Field factoriesField = field(Gson.class, "factories");
         Field builderFactoriesField = field(GsonBuilder.class, "factories");
         Field builderHierarchyFactoriesField = field(GsonBuilder.class, "hierarchyFactories");

         Gson tmpGson;
         try {
            Field gsonField = field(ComponentSerializer.class, "gson");
            tmpGson = (Gson)gsonField.get(null);
         } catch (NoSuchFieldException var14) {
            Class<?> versionedComponentSerializerClass = Class.forName("net.md_5.bungee.chat.VersionedComponentSerializer");
            Class<?> chatVersionClass = Class.forName("net.md_5.bungee.chat.ChatVersion");
            Object arg = chatVersionClass.getField("V1_21_5").get(null);
            Object versionedSerializer = versionedComponentSerializerClass.getMethod("forVersion", chatVersionClass).invoke(null, arg);
            tmpGson = (Gson)versionedComponentSerializerClass.getMethod("getGson").invoke(versionedSerializer);
         }

         GsonBuilder builder = GsonComponentSerializer.populate(new GsonBuilder());
         List<TypeAdapterFactory> existingFactories = (List<TypeAdapterFactory>)factoriesField.get(tmpGson);
         List<TypeAdapterFactory> newFactories = new ArrayList<>();
         newFactories.addAll((List)builderFactoriesField.get(builder));
         Collections.reverse(newFactories);
         newFactories.addAll((List)builderHierarchyFactoriesField.get(builder));
         List<TypeAdapterFactory> modifiedFactories = new ArrayList<>(existingFactories);
         int index = findExcluderIndex(modifiedFactories);

         for (TypeAdapterFactory newFactory : Lists.reverse(newFactories)) {
            modifiedFactories.add(index, newFactory);
         }

         Class<?> treeTypeAdapterClass;
         try {
            treeTypeAdapterClass = Class.forName("com.google.gson.internal.bind.TreeTypeAdapter");
         } catch (ClassNotFoundException var13) {
            treeTypeAdapterClass = Class.forName("com.google.gson.TreeTypeAdapter");
         }

         Method newFactoryWithMatchRawTypeMethod = treeTypeAdapterClass.getMethod("newFactoryWithMatchRawType", TypeToken.class, Object.class);
         TypeAdapterFactory adapterComponentFactory = (TypeAdapterFactory)newFactoryWithMatchRawTypeMethod.invoke(
            null, TypeToken.get(SpigotAdapter.AdapterComponent.class), new SpigotAdapter.Serializer()
         );
         modifiedFactories.add(index, adapterComponentFactory);
         factoriesField.set(tmpGson, modifiedFactories);
         return true;
      } catch (Throwable var15) {
         return false;
      }
   }

   private static Field field(final Class<?> klass, final String name) throws NoSuchFieldException {
      Field field = klass.getDeclaredField(name);
      field.setAccessible(true);
      return field;
   }

   private static int findExcluderIndex(final List<TypeAdapterFactory> factories) {
      int i = 0;

      for (int size = factories.size(); i < size; i++) {
         TypeAdapterFactory factory = factories.get(i);
         if (factory instanceof Excluder) {
            return i + 1;
         }
      }

      return 0;
   }

   @Override
   public void sendMessage(final List<? extends CommandSender> viewers, final Component component) {
      if (BOUND) {
         send(viewers, component, (viewer, components) -> viewer.spigot().sendMessage(components));
      }
   }

   @Override
   public void sendActionBar(final List<? extends CommandSender> viewers, final Component component) {
      if (BOUND) {
         send(viewers, component, (viewer, components) -> viewer.spigot().sendMessage(ChatMessageType.ACTION_BAR, components));
      }
   }

   private static void send(final List<? extends CommandSender> viewers, final Component component, final BiConsumer<Player, BaseComponent[]> consumer) {
      if (BOUND) {
         BaseComponent[] components = new BaseComponent[]{new SpigotAdapter.AdapterComponent(component)};
         Iterator<? extends CommandSender> it = viewers.iterator();

         while (it.hasNext()) {
            CommandSender viewer = it.next();
            if (viewer instanceof Player) {
               try {
                  consumer.accept((Player)viewer, components);
                  it.remove();
               } catch (Throwable var7) {
                  var7.printStackTrace();
               }
            }
         }
      }
   }

   static BaseComponent[] toBungeeCord(final Component component) {
      return BOUND
         ? new BaseComponent[]{new SpigotAdapter.AdapterComponent(component)}
         : ComponentSerializer.parse(GsonComponentSerializer.INSTANCE.serialize(component));
   }

   public static final class AdapterComponent extends BaseComponent {
      private final Component component;

      AdapterComponent(final Component component) {
         this.component = component;
      }

      public BaseComponent duplicate() {
         return this;
      }

      public String toLegacyText() {
         return LegacyComponentSerializer.legacy().serialize(this.component);
      }
   }

   public static class Serializer implements JsonSerializer<SpigotAdapter.AdapterComponent> {
      public JsonElement serialize(final SpigotAdapter.AdapterComponent src, final Type typeOfSrc, final JsonSerializationContext context) {
         return context.serialize(src.component);
      }
   }
}
