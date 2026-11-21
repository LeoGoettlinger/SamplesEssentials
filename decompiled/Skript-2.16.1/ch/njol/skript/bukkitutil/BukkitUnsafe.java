package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.util.EnumTypeAdapter;
import com.google.common.io.ByteStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.UnsafeValues;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BukkitUnsafe {
   @Nullable
   private static final UnsafeValues unsafe = Bukkit.getUnsafe();
   @Nullable
   private static Map<Integer, Material> idMappings;

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Nullable
   public static Material getMaterialFromMinecraftId(String id) {
      return getMaterialFromNamespacedId(id);
   }

   @Nullable
   public static Material getMaterialFromNamespacedId(String id) {
      return Material.matchMaterial(id.toLowerCase().startsWith("minecraft:") ? id : id.replace(":", "_"));
   }

   public static void modifyItemStack(ItemStack stack, String arguments) {
      if (unsafe == null) {
         throw new IllegalStateException("modifyItemStack could not be performed as UnsafeValues are not available.");
      } else {
         unsafe.modifyItemStack(stack, arguments);
      }
   }

   private static void initIdMappings() {
      try {
         try (InputStream is = Skript.getInstance().getResource("materials/ids.json")) {
            if (is == null) {
               throw new AssertionError("missing id mappings");
            }

            String data = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            Type type = (new TypeToken<Map<Integer, String>>() {}).getType();
            Map<Integer, String> rawMappings = (Map<Integer, String>)new GsonBuilder()
               .registerTypeAdapterFactory(EnumTypeAdapter.factory)
               .create()
               .fromJson(data, type);
            Map<Integer, Material> parsed = new HashMap<>(rawMappings.size());

            for (Entry<Integer, String> entry : rawMappings.entrySet()) {
               parsed.put(entry.getKey(), Material.matchMaterial(entry.getValue(), true));
            }

            idMappings = parsed;
         }
      } catch (IOException var9) {
         throw new AssertionError(var9);
      }
   }

   @Nullable
   public static Material getMaterialFromId(int id) {
      if (idMappings == null) {
         initIdMappings();
      }

      assert idMappings != null;

      return idMappings.get(id);
   }

   static {
      if (unsafe == null) {
         throw new Error("UnsafeValues are not available.");
      }
   }
}
