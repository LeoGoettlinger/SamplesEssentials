package ch.njol.skript.variables;

import ch.njol.util.StringUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.IndexTrackingTreeMap;

final class VariablesMap {
   static final Comparator<String> VARIABLE_NAME_COMPARATOR = (s1, s2) -> {
      if (s1 == null) {
         return s2 == null ? 0 : -1;
      } else if (s2 == null) {
         return 1;
      } else {
         int len1 = s1.length();
         int len2 = s2.length();
         char firstChar1 = len1 > 0 ? s1.charAt(0) : 0;
         char firstChar2 = len2 > 0 ? s2.charAt(0) : 0;
         if (firstChar1 >= '1' && firstChar1 <= '9' && firstChar2 >= '1' && firstChar2 <= '9') {
            int i = 1;

            while (i < len1 && isDigit(s1.charAt(i))) {
               i++;
            }

            if (i == len1) {
               i = 1;

               while (i < len2 && isDigit(s2.charAt(i))) {
                  i++;
               }

               if (i == len2) {
                  if (len1 != len2) {
                     return len1 - len2;
                  }

                  return s1.compareTo(s2);
               }
            }
         }

         int i = 0;
         int j = 0;
         boolean lastNumberNegative = false;
         boolean afterDecimalPoint = false;

         while (i < len1 && j < len2) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(j);
            if (isDigit(c1) && isDigit(c2)) {
               int i2 = StringUtils.findLastDigit(s1, i);
               int j2 = StringUtils.findLastDigit(s2, j);
               int z1 = 0;
               int z2 = 0;
               if (!afterDecimalPoint) {
                  if (c1 == '0') {
                     while (i < i2 - 1 && s1.charAt(i) == '0') {
                        i++;
                        z1++;
                     }
                  }

                  if (c2 == '0') {
                     while (j < j2 - 1 && s2.charAt(j) == '0') {
                        j++;
                        z2++;
                     }
                  }
               }

               boolean previousNegative = lastNumberNegative;
               lastNumberNegative = i - z1 > 0 && s1.charAt(i - z1 - 1) == '-';
               int isPositive = lastNumberNegative | previousNegative ? -1 : 1;
               if (!afterDecimalPoint && i2 - i != j2 - j) {
                  return (i2 - i - (j2 - j)) * isPositive;
               }

               while (i < i2 && j < j2) {
                  char d1 = s1.charAt(i);
                  char d2 = s2.charAt(j);
                  if (d1 != d2) {
                     return (d1 - d2) * isPositive;
                  }

                  i++;
                  j++;
               }

               if (afterDecimalPoint && i2 - i != j2 - j) {
                  return (i2 - i - (j2 - j)) * isPositive;
               }

               if (z1 != z2) {
                  return (z1 - z2) * isPositive;
               }

               afterDecimalPoint = true;
            } else {
               if (c1 != c2) {
                  return c1 - c2;
               }

               if (c1 != '.') {
                  lastNumberNegative = false;
                  afterDecimalPoint = false;
               }

               i++;
               j++;
            }
         }

         if (i < len1) {
            return lastNumberNegative ? -1 : 1;
         } else if (j < len2) {
            return lastNumberNegative ? 1 : -1;
         } else {
            return 0;
         }
      }
   };
   final HashMap<String, Object> hashMap = new HashMap<>();
   final TreeMap<String, Object> treeMap = new TreeMap<>();

   private static boolean isDigit(char c) {
      return '0' <= c && c <= '9';
   }

   @Nullable
   Object getVariable(String name) {
      if (!name.endsWith("*")) {
         return this.hashMap.get(name);
      } else {
         String[] split = Variables.splitVariableName(name);
         Map<String, Object> parent = this.treeMap;

         for (int i = 0; i < split.length; i++) {
            String n = split[i];
            if (n.equals("*")) {
               assert i == split.length - 1;

               return parent;
            }

            Object childNode = parent.get(n);
            if (childNode == null) {
               return null;
            }

            if (!(childNode instanceof Map)) {
               return null;
            }

            parent = (Map<String, Object>)childNode;

            assert i != split.length - 1;
         }

         return null;
      }
   }

   void setVariable(String name, @Nullable Object value) {
      if (!name.endsWith("*")) {
         if (value == null) {
            this.hashMap.remove(name);
         } else {
            this.hashMap.put(name, value);
         }
      }

      String[] split = Variables.splitVariableName(name);
      TreeMap<String, Object> parent = this.treeMap;

      for (int i = 0; i < split.length; i++) {
         String childNodeName = split[i];
         Object childNode = parent.get(childNodeName);
         if (childNode == null) {
            if (i == split.length - 1) {
               if (value != null) {
                  parent.put(childNodeName, value);
               }
               break;
            }

            if (value == null) {
               break;
            }

            childNode = new IndexTrackingTreeMap(VARIABLE_NAME_COMPARATOR);
            parent.put(childNodeName, childNode);
            parent = (TreeMap<String, Object>)childNode;
         } else if (childNode instanceof TreeMap<String, Object> childNodeMap) {
            if (i == split.length - 1) {
               if (value == null) {
                  childNodeMap.remove(null);
               } else {
                  childNodeMap.put(null, value);
               }
               break;
            }

            if (i == split.length - 2 && split[i + 1].equals("*")) {
               assert value == null;

               this.deleteFromHashMap(StringUtils.join(split, "::", 0, i + 1), childNodeMap);
               Object currentChildValue = childNodeMap.get(null);
               if (currentChildValue == null) {
                  parent.remove(childNodeName);
               } else {
                  parent.put(childNodeName, currentChildValue);
               }
               break;
            }

            parent = childNodeMap;
         } else {
            if (i == split.length - 1) {
               if (value == null) {
                  parent.remove(childNodeName);
               } else {
                  parent.put(childNodeName, value);
               }
               break;
            }

            if (value == null) {
               break;
            }

            TreeMap<String, Object> newChildNodeMap = new IndexTrackingTreeMap<>(VARIABLE_NAME_COMPARATOR);
            newChildNodeMap.put(null, childNode);
            parent.put(childNodeName, newChildNodeMap);
            parent = newChildNodeMap;
         }
      }
   }

   void deleteFromHashMap(String parent, TreeMap<String, Object> current) {
      for (Entry<String, Object> e : current.entrySet()) {
         if (e.getKey() != null) {
            String childName = parent + "::" + e.getKey();
            this.hashMap.remove(childName);
            Object val = e.getValue();
            if (val instanceof TreeMap) {
               this.deleteFromHashMap(childName, (TreeMap<String, Object>)val);
            }
         }
      }
   }

   public VariablesMap copy() {
      VariablesMap copy = new VariablesMap();
      copy.hashMap.putAll(this.hashMap);
      TreeMap<String, Object> treeMapCopy = copyTreeMap(this.treeMap);
      copy.treeMap.putAll(treeMapCopy);
      return copy;
   }

   private static TreeMap<String, Object> copyTreeMap(TreeMap<String, Object> original) {
      TreeMap<String, Object> copy = new IndexTrackingTreeMap<>(VARIABLE_NAME_COMPARATOR);

      for (Entry<String, Object> child : original.entrySet()) {
         String key = child.getKey();
         Object value = child.getValue();
         if (value instanceof TreeMap) {
            value = copyTreeMap((TreeMap<String, Object>)value);
         }

         copy.put(key, value);
      }

      return copy;
   }
}
