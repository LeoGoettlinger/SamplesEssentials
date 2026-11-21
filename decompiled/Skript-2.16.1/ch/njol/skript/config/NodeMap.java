package ch.njol.skript.config;

import java.lang.invoke.StringConcatFactory;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

public class NodeMap {
   private final Map<String, Node> map = new HashMap<>();

   public static boolean inMap(Node n) {
      return n instanceof EntryNode || n instanceof SectionNode;
   }

   private static String getKey(Node n) {
      String key = n.getKey();
      if (key == null) {
         assert false : n;

         return "";
      } else {
         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH));
      }
   }

   private static String getKey(String key) {
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH));
   }

   public void put(Node n) {
      if (inMap(n)) {
         this.map.put(getKey(n), n);
      }
   }

   @Nullable
   public Node remove(Node n) {
      return this.remove(getKey(n));
   }

   @Nullable
   public Node remove(@Nullable String key) {
      return key == null ? null : this.map.remove(getKey(key));
   }

   @Nullable
   public Node get(@Nullable String key) {
      return key == null ? null : this.map.get(getKey(key));
   }
}
