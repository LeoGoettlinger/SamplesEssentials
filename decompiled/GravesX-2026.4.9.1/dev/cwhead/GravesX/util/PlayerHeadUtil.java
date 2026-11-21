package dev.cwhead.GravesX.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;

public final class PlayerHeadUtil {
   public static final String MARKER = "||GXHEAD||";

   private PlayerHeadUtil() {
   }

   public static String appendFromBlock(Block block, String existingReplaceData) {
      PlayerHeadUtil.HeadPayload p = extract(block);
      if (p == null) {
         return existingReplaceData;
      } else {
         String json = toJson(p);
         if (json.isEmpty()) {
            return existingReplaceData;
         } else {
            return existingReplaceData != null && !existingReplaceData.isEmpty() ? existingReplaceData + "||GXHEAD||" + json : "||GXHEAD||" + json;
         }
      }
   }

   public static Optional<PlayerHeadUtil.HeadPayload> parseFromReplaceData(String replaceData) {
      if (replaceData == null) {
         return Optional.empty();
      } else {
         int idx = replaceData.lastIndexOf("||GXHEAD||");
         if (idx >= 0 && idx + "||GXHEAD||".length() < replaceData.length()) {
            String json = replaceData.substring(idx + "||GXHEAD||".length()).trim();

            try {
               return Optional.of(parseJson(json));
            } catch (Throwable var4) {
               Bukkit.getLogger().warning("[GravesX] Failed to parse GXHEAD payload: " + var4.getMessage());
               return Optional.empty();
            }
         } else {
            return Optional.empty();
         }
      }
   }

   public static PlayerHeadUtil.HeadPayload extract(Block block) {
      if (block != null && isHead(block)) {
         PlayerHeadUtil.HeadPayload p = new PlayerHeadUtil.HeadPayload();
         p.m = safeMatName(block);
         p.bd = getBlockDataString(block);
         PlayerHeadUtil.MountFace mf = readMountAndFaces(block);
         p.mount = mf.mount;
         p.rf = mf.rotationFace;
         p.wf = mf.wallFacing;

         try {
            if (block.getClass().getMethod("getState").invoke(block) instanceof Skull skull) {
               try {
                  Object owning = Skull.class.getMethod("getOwningPlayer").invoke(skull);
                  if (owning != null) {
                     try {
                        p.ou = String.valueOf(owning.getClass().getMethod("getUniqueId").invoke(owning));
                     } catch (Throwable var16) {
                     }

                     try {
                        p.on = String.valueOf(owning.getClass().getMethod("getName").invoke(owning));
                     } catch (Throwable var15) {
                     }
                  }
               } catch (NoSuchMethodException var17) {
                  try {
                     Object legacy = Skull.class.getMethod("getOwner").invoke(skull);
                     if (legacy != null) {
                        p.on = String.valueOf(legacy);
                     }
                  } catch (Throwable var14) {
                  }
               }

               try {
                  Field profileField = skull.getClass().getDeclaredField("profile");
                  profileField.setAccessible(true);
                  Object gp = profileField.get(skull);
                  if (gp != null) {
                     if (p.ou == null) {
                        try {
                           Object id = gp.getClass().getMethod("getId").invoke(gp);
                           if (id != null) {
                              p.ou = String.valueOf(id);
                           }
                        } catch (Throwable var12) {
                        }
                     }

                     if (p.on == null) {
                        try {
                           Object nm = gp.getClass().getMethod("getName").invoke(gp);
                           if (nm != null) {
                              p.on = String.valueOf(nm);
                           }
                        } catch (Throwable var11) {
                        }
                     }

                     Collection<?> props;
                     try {
                        Object map = gp.getClass().getMethod("properties").invoke(gp);
                        props = getTextures(map);
                     } catch (NoSuchMethodException var10) {
                        Object mapx = gp.getClass().getMethod("getProperties").invoke(gp);
                        props = getTextures(mapx);
                     }

                     if (!props.isEmpty()) {
                        Object prop = props.iterator().next();
                        p.tx = callString(prop, "value", "getValue");
                        p.sg = callString(prop, "signature", "getSignature");
                     }
                  }
               } catch (Throwable var13) {
               }

               p.nm = tryReadCustomNameJson(skull);
            }
         } catch (Throwable var18) {
            Bukkit.getLogger().warning("[GravesX] Skull read error: " + var18.getMessage());
         }

         if (empty(p.bd)) {
            p.bd = null;
         }

         if (empty(p.mount)) {
            p.mount = null;
         }

         if (empty(p.rf)) {
            p.rf = null;
         }

         if (empty(p.wf)) {
            p.wf = null;
         }

         if (empty(p.ou)) {
            p.ou = null;
         }

         if (empty(p.on)) {
            p.on = null;
         }

         if (empty(p.tx)) {
            p.tx = null;
         }

         if (empty(p.sg)) {
            p.sg = null;
         }

         if (empty(p.nm)) {
            p.nm = null;
         }

         return p;
      } else {
         return null;
      }
   }

   private static boolean isHead(Block b) {
      String n = safeMatName(b);
      return "PLAYER_HEAD".equals(n) || "PLAYER_WALL_HEAD".equals(n) || "SKULL".equals(n) || "LEGACY_SKULL".equals(n);
   }

   private static String safeMatName(Block b) {
      try {
         Material m = b.getType();
         return m.name();
      } catch (Throwable var2) {
         return null;
      }
   }

   private static boolean empty(String s) {
      return s == null || s.isEmpty();
   }

   private static String getBlockDataString(Block block) {
      try {
         Object bd = block.getClass().getMethod("getBlockData").invoke(block);
         if (bd != null) {
            Method asString = bd.getClass().getMethod("getAsString", boolean.class);
            return String.valueOf(asString.invoke(bd, true));
         }
      } catch (Throwable var3) {
      }

      return null;
   }

   private static PlayerHeadUtil.MountFace readMountAndFaces(Block block) {
      PlayerHeadUtil.MountFace mf = null;

      try {
         Object bd = block.getClass().getMethod("getBlockData").invoke(block);
         if (bd != null) {
            Class<?> rotatable = tryLoad("org.bukkit.block.data.Rotatable");
            Class<?> directional = tryLoad("org.bukkit.block.data.Directional");
            if (rotatable != null && rotatable.isInstance(bd)) {
               mf = new PlayerHeadUtil.MountFace();
               mf.mount = "FLOOR";
               Method getRotation = rotatable.getMethod("getRotation");
               Object face = getRotation.invoke(bd);
               mf.rotationFace = face != null ? face.toString() : null;
            }

            if (directional != null && directional.isInstance(bd)) {
               if (mf == null) {
                  mf = new PlayerHeadUtil.MountFace();
               }

               mf.mount = "WALL";
               Method getFacing = directional.getMethod("getFacing");
               Object face = getFacing.invoke(bd);
               mf.wallFacing = face != null ? cardinal(face.toString()) : null;
            }
         }
      } catch (Throwable var12) {
      }

      if (mf == null) {
         try {
            Object state = block.getClass().getMethod("getState").invoke(block);
            Method getData = state.getClass().getMethod("getData");
            Object matData = getData.invoke(state);
            Class<?> legacySkull = tryLoad("org.bukkit.material.Skull");
            if (legacySkull != null && legacySkull.isInstance(matData)) {
               mf = new PlayerHeadUtil.MountFace();
               Method getFacing = null;
               Method getRotation = null;

               try {
                  getFacing = legacySkull.getMethod("getFacing");
               } catch (Throwable var10) {
               }

               try {
                  getRotation = legacySkull.getMethod("getRotation");
               } catch (Throwable var9) {
               }

               if (getFacing != null) {
                  Object bf = getFacing.invoke(matData);
                  if (bf != null) {
                     mf.mount = "WALL";
                     mf.wallFacing = cardinal(bf.toString());
                  }
               }

               if (getRotation != null) {
                  Object bf = getRotation.invoke(matData);
                  if (bf != null) {
                     mf.mount = "FLOOR";
                     mf.rotationFace = bf.toString();
                  }
               }
            }
         } catch (Throwable var11) {
         }
      }

      if (mf == null) {
         mf = new PlayerHeadUtil.MountFace();
         mf.mount = "UNKNOWN";
      }

      return mf;
   }

   private static String cardinal(String face) {
      if (face == null) {
         return null;
      } else {
         String var1 = face.toUpperCase(Locale.ROOT);

         return switch (var1) {
            case "NORTH" -> "NORTH";
            case "SOUTH" -> "SOUTH";
            case "EAST" -> "EAST";
            case "WEST" -> "WEST";
            default -> null;
         };
      }
   }

   private static Class<?> tryLoad(String name) {
      try {
         return Class.forName(name);
      } catch (Throwable var2) {
         return null;
      }
   }

   private static Collection<?> getTextures(Object propertyMap) {
      if (propertyMap == null) {
         return Collections.emptyList();
      } else {
         try {
            boolean has = (Boolean)propertyMap.getClass().getMethod("containsKey", Object.class).invoke(propertyMap, "textures");
            if (has) {
               Object c = propertyMap.getClass().getMethod("get", Object.class).invoke(propertyMap, "textures");
               if (c instanceof Collection) {
                  return (Collection<?>)c;
               }
            }
         } catch (Throwable var4) {
         }

         try {
            Object c = propertyMap.getClass().getMethod("get", Object.class).invoke(propertyMap, "textures");
            if (c instanceof Collection) {
               return (Collection<?>)c;
            }
         } catch (Throwable var3) {
         }

         return Collections.emptyList();
      }
   }

   private static String callString(Object target, String modern, String legacy) {
      if (target == null) {
         return null;
      } else {
         try {
            return String.valueOf(target.getClass().getMethod(modern).invoke(target));
         } catch (Throwable var5) {
            try {
               return String.valueOf(target.getClass().getMethod(legacy).invoke(target));
            } catch (Throwable var4) {
               return null;
            }
         }
      }
   }

   private static String tryReadCustomNameJson(Skull skull) {
      try {
         Method m = skull.getClass().getMethod("customName");
         Object comp = m.invoke(skull);
         if (comp != null) {
            Class<?> serClazz = Class.forName("com.ranull.graves.libraries.kyori.adventure.text.serializer.gson.GsonComponentSerializer");
            Method gson = serClazz.getMethod("gson");
            Object serializer = gson.invoke(null);
            Method serialize = serializer.getClass().getMethod("serialize", Class.forName("com.ranull.graves.libraries.kyori.adventure.text.Component"));
            Object json = serialize.invoke(serializer, comp);
            return json != null ? json.toString() : null;
         }
      } catch (Throwable var9) {
      }

      try {
         Method m = skull.getClass().getMethod("getCustomName");
         Object s = m.invoke(skull);
         if (s != null) {
            String str = String.valueOf(s);
            if (!str.isEmpty()) {
               return "{\"text\":\"" + str.replace("\"", "\\\"") + "\"}";
            }
         }
      } catch (Throwable var8) {
      }

      return null;
   }

   private static String toJson(PlayerHeadUtil.HeadPayload p) {
      StringBuilder sb = new StringBuilder(256);
      sb.append('{');
      writeInt(sb, "v", p.v, true);
      writeStr(sb, "m", p.m);
      writeStr(sb, "bd", p.bd);
      writeStr(sb, "mount", p.mount);
      writeStr(sb, "rf", p.rf);
      writeStr(sb, "wf", p.wf);
      writeStr(sb, "ou", p.ou);
      writeStr(sb, "on", p.on);
      writeStr(sb, "tx", p.tx);
      writeStr(sb, "sg", p.sg);
      writeStr(sb, "nm", p.nm);
      if (sb.charAt(sb.length() - 1) == ',') {
         sb.setLength(sb.length() - 1);
      }

      sb.append('}');
      return sb.toString();
   }

   private static void writeInt(StringBuilder sb, String k, int v, boolean always) {
      if (always) {
         sb.append('"').append(k).append('"').append(':').append(v).append(',');
      }
   }

   private static void writeStr(StringBuilder sb, String k, String v) {
      if (v != null) {
         sb.append('"').append(k).append('"').append(':').append('"').append(escape(v)).append('"').append(',');
      }
   }

   private static String escape(String s) {
      return s.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private static PlayerHeadUtil.HeadPayload parseJson(String json) {
      PlayerHeadUtil.HeadPayload p = new PlayerHeadUtil.HeadPayload();
      Map<String, String> map = new HashMap<>();
      String body = json.trim();
      if (body.startsWith("{")) {
         body = body.substring(1);
      }

      if (body.endsWith("}")) {
         body = body.substring(0, body.length() - 1);
      }

      List<String> pairs = new ArrayList<>();
      StringBuilder cur = new StringBuilder();
      boolean inStr = false;

      for (int i = 0; i < body.length(); i++) {
         char c = body.charAt(i);
         if (c == '"' && (i == 0 || body.charAt(i - 1) != '\\')) {
            inStr = !inStr;
         }

         if (!inStr && c == ',') {
            pairs.add(cur.toString());
            cur.setLength(0);
         } else {
            cur.append(c);
         }
      }

      if (cur.length() > 0) {
         pairs.add(cur.toString());
      }

      for (String pair : pairs) {
         int colon = pair.indexOf(58);
         if (colon >= 0) {
            String key = unquote(pair.substring(0, colon).trim());
            String val = pair.substring(colon + 1).trim();
            map.put(key, unquote(val));
         }
      }

      if (map.containsKey("v")) {
         try {
            p.v = Integer.parseInt(map.get("v"));
         } catch (Throwable var12) {
         }
      }

      p.m = map.get("m");
      p.bd = map.get("bd");
      p.mount = map.get("mount");
      p.rf = map.get("rf");
      p.wf = map.get("wf");
      p.ou = map.get("ou");
      p.on = map.get("on");
      p.tx = map.get("tx");
      p.sg = map.get("sg");
      p.nm = map.get("nm");
      return p;
   }

   private static String unquote(String s) {
      s = s.trim();
      if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
         s = s.substring(1, s.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
      }

      return s;
   }

   public static final class HeadPayload {
      int v = 1;
      String m;
      String bd;
      String mount;
      String rf;
      String wf;
      String ou;
      String on;
      String tx;
      String sg;
      String nm;
   }

   private static final class MountFace {
      String mount;
      String rotationFace;
      String wallFacing;
   }
}
