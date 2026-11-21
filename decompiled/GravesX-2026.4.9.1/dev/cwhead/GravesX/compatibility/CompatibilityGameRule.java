package dev.cwhead.GravesX.compatibility;

import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import java.lang.reflect.Method;
import org.bukkit.GameRule;
import org.bukkit.World;

public final class CompatibilityGameRule {
   public static GameRule getByName(String ruleName) {
      if (ruleName != null && !ruleName.isBlank()) {
         try {
            Method method = GameRule.class.getMethod("getByName", String.class);
            Object value = method.invoke(null, ruleName);
            return value instanceof GameRule ? (GameRule)value : null;
         } catch (NoSuchMethodException var3) {
            throw new GravesXIllegalArgumentException("GameRule lookup is not supported on this server version.");
         } catch (Exception var4) {
            throw new GravesXIllegalArgumentException("An issue occurred while retrieving gamerule " + ruleName);
         }
      } else {
         return null;
      }
   }

   public static Object getValue(World world, String ruleName) {
      if (world != null && ruleName != null && !ruleName.isBlank()) {
         GameRule rule = getByName(ruleName);
         if (rule != null) {
            try {
               Method byRule = World.class.getMethod("getGameRuleValue", GameRule.class);
               return byRule.invoke(world, rule);
            } catch (NoSuchMethodException var6) {
            } catch (Exception var7) {
               throw new GravesXIllegalArgumentException("An issue occurred while reading gamerule " + ruleName + " from world " + world.getName());
            }
         }

         try {
            Method byString = World.class.getMethod("getGameRuleValue", String.class);
            return byString.invoke(world, ruleName);
         } catch (NoSuchMethodException var4) {
            throw new GravesXIllegalArgumentException("GameRule values are not supported on this server version.");
         } catch (Exception var5) {
            throw new GravesXIllegalArgumentException("An issue occurred while reading gamerule " + ruleName + " from world " + world.getName());
         }
      } else {
         return null;
      }
   }

   public static boolean getBoolean(World world, String ruleName) {
      Object value = getValue(world, ruleName);
      if (value instanceof Boolean b) {
         return b;
      } else if (value instanceof String s) {
         return Boolean.parseBoolean(s);
      } else {
         throw new GravesXIllegalArgumentException(
            "GameRule " + ruleName + " is not a boolean gamerule on world " + (world != null ? world.getName() : "unknown")
         );
      }
   }

   public static int getInt(World world, String ruleName) {
      Object value = getValue(world, ruleName);
      if (value instanceof Integer i) {
         return i;
      } else if (value instanceof Number n) {
         return n.intValue();
      } else {
         if (value instanceof String s) {
            try {
               return Integer.parseInt(s.trim());
            } catch (NumberFormatException var5) {
            }
         }

         throw new GravesXIllegalArgumentException(
            "GameRule " + ruleName + " is not an integer gamerule on world " + (world != null ? world.getName() : "unknown")
         );
      }
   }

   public static void setValue(World world, String ruleName, Object value) {
      if (world != null && ruleName != null && !ruleName.isBlank()) {
         GameRule rule = getByName(ruleName);
         if (rule != null) {
            try {
               for (Method m : World.class.getMethods()) {
                  if (m.getName().equals("setGameRule")) {
                     Class<?>[] p = m.getParameterTypes();
                     if (p.length == 2 && p[0] == GameRule.class) {
                        m.invoke(world, rule, value);
                        return;
                     }
                  }
               }
            } catch (Exception var11) {
               throw new GravesXIllegalArgumentException("An issue occurred while setting gamerule " + ruleName + " on world " + world.getName());
            }
         }

         try {
            Method setString = World.class.getMethod("setGameRuleValue", String.class, String.class);
            setString.invoke(world, ruleName, String.valueOf(value));
         } catch (NoSuchMethodException var9) {
            throw new GravesXIllegalArgumentException("Setting GameRules is not supported on this server version.");
         } catch (Exception var10) {
            throw new GravesXIllegalArgumentException("An issue occurred while setting gamerule " + ruleName + " on world " + world.getName());
         }
      }
   }
}
