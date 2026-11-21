package com.sk89q.util.yaml;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

public class YAMLNode {
   protected Map<String, Object> root;
   private boolean writeDefaults;

   public YAMLNode(Map<String, Object> root, boolean writeDefaults) {
      this.root = root;
      this.writeDefaults = writeDefaults;
   }

   @Nullable
   private static Integer castInt(Object o) {
      if (o == null) {
         return null;
      } else {
         return o instanceof Number ? ((Number)o).intValue() : null;
      }
   }

   @Nullable
   private static Double castDouble(Object o) {
      if (o == null) {
         return null;
      } else {
         return o instanceof Number ? ((Number)o).doubleValue() : null;
      }
   }

   @Nullable
   private static Boolean castBoolean(Object o) {
      if (o == null) {
         return null;
      } else {
         return o instanceof Boolean ? (Boolean)o : null;
      }
   }

   public Map<String, Object> getMap() {
      return this.root;
   }

   public void clear() {
      this.root.clear();
   }

   public Object getProperty(String path) {
      if (!path.contains(".")) {
         return this.root.get(path);
      } else {
         String[] parts = path.split("\\.");
         Map<String, Object> node = this.root;

         for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);
            if (o == null) {
               return null;
            }

            if (i == parts.length - 1) {
               return o;
            }

            try {
               node = (Map<String, Object>)o;
            } catch (ClassCastException var7) {
               return null;
            }
         }

         return null;
      }
   }

   private Object prepareSerialization(Object value) {
      if (value instanceof Vector3) {
         Map<String, Double> out = new LinkedHashMap<>();
         Vector3 vec = (Vector3)value;
         out.put("x", vec.x());
         out.put("y", vec.y());
         out.put("z", vec.z());
         return out;
      } else if (value instanceof BlockVector3) {
         Map<String, Integer> out = new LinkedHashMap<>();
         BlockVector3 vec = (BlockVector3)value;
         out.put("x", vec.x());
         out.put("y", vec.y());
         out.put("z", vec.z());
         return out;
      } else if (value instanceof Vector2) {
         Map<String, Double> out = new LinkedHashMap<>();
         Vector2 vec = (Vector2)value;
         out.put("x", vec.x());
         out.put("z", vec.z());
         return out;
      } else if (value instanceof BlockVector2) {
         Map<String, Integer> out = new LinkedHashMap<>();
         BlockVector2 vec = (BlockVector2)value;
         out.put("x", vec.x());
         out.put("z", vec.z());
         return out;
      } else {
         return value;
      }
   }

   public void setProperty(String path, Object value) {
      value = this.prepareSerialization(value);
      if (!path.contains(".")) {
         this.root.put(path, value);
      } else {
         String[] parts = path.split("\\.");
         Map<String, Object> node = this.root;

         for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);
            if (i == parts.length - 1) {
               node.put(parts[i], value);
               return;
            }

            if (!(o instanceof Map)) {
               o = new LinkedHashMap();
               node.put(parts[i], o);
            }

            node = (Map<String, Object>)o;
         }
      }
   }

   public YAMLNode addNode(String path) {
      Map<String, Object> map = new LinkedHashMap<>();
      YAMLNode node = new YAMLNode(map, this.writeDefaults);
      this.setProperty(path, map);
      return node;
   }

   public String getString(String path) {
      Object o = this.getProperty(path);
      return o == null ? null : o.toString();
   }

   public Vector3 getVector(String path) {
      YAMLNode o = this.getNode(path);
      if (o == null) {
         return null;
      } else {
         Double x = o.getDouble("x");
         Double y = o.getDouble("y");
         Double z = o.getDouble("z");
         return x != null && y != null && z != null ? Vector3.at(x, y, z) : null;
      }
   }

   public Vector2 getVector2(String path) {
      YAMLNode o = this.getNode(path);
      if (o == null) {
         return null;
      } else {
         Double x = o.getDouble("x");
         Double z = o.getDouble("z");
         return x != null && z != null ? Vector2.at(x, z) : null;
      }
   }

   public Vector3 getVector(String path, Vector3 def) {
      Vector3 v = this.getVector(path);
      if (v == null) {
         if (this.writeDefaults) {
            this.setProperty(path, def);
         }

         return def;
      } else {
         return v;
      }
   }

   public String getString(String path, String def) {
      String o = this.getString(path);
      if (o == null) {
         if (this.writeDefaults) {
            this.setProperty(path, def);
         }

         return def;
      } else {
         return o;
      }
   }

   public Integer getInt(String path) {
      return castInt(this.getProperty(path));
   }

   public int getInt(String path, int def) {
      Integer o = castInt(this.getProperty(path));
      if (o == null) {
         if (this.writeDefaults) {
            this.setProperty(path, def);
         }

         return def;
      } else {
         return o;
      }
   }

   public Double getDouble(String path) {
      return castDouble(this.getProperty(path));
   }

   public double getDouble(String path, double def) {
      Double o = castDouble(this.getProperty(path));
      if (o == null) {
         if (this.writeDefaults) {
            this.setProperty(path, def);
         }

         return def;
      } else {
         return o;
      }
   }

   public Boolean getBoolean(String path) {
      return castBoolean(this.getProperty(path));
   }

   public boolean getBoolean(String path, boolean def) {
      Boolean o = castBoolean(this.getProperty(path));
      if (o == null) {
         if (this.writeDefaults) {
            this.setProperty(path, def);
         }

         return def;
      } else {
         return o;
      }
   }

   public List<String> getKeys(String path) {
      if (path == null) {
         return new ArrayList<>(this.root.keySet());
      } else {
         Object o = this.getProperty(path);
         if (o == null) {
            return null;
         } else {
            return o instanceof Map ? new ArrayList<>(((Map)o).keySet()) : null;
         }
      }
   }

   public List<Object> getList(String path) {
      Object o = this.getProperty(path);
      if (o == null) {
         return null;
      } else {
         return o instanceof List ? (List)o : null;
      }
   }

   public List<String> getStringList(String path, List<String> def) {
      List<Object> raw = this.getList(path);
      if (raw == null) {
         if (this.writeDefaults && def != null) {
            this.setProperty(path, def);
         }

         return (List<String>)(def != null ? def : new ArrayList<>());
      } else {
         List<String> list = new ArrayList<>();

         for (Object o : raw) {
            if (o != null) {
               list.add(o.toString());
            }
         }

         return list;
      }
   }

   public List<Integer> getIntList(String path, List<Integer> def) {
      List<Object> raw = this.getList(path);
      if (raw == null) {
         if (this.writeDefaults && def != null) {
            this.setProperty(path, def);
         }

         return (List<Integer>)(def != null ? def : new ArrayList<>());
      } else {
         List<Integer> list = new ArrayList<>();

         for (Object o : raw) {
            Integer i = castInt(o);
            if (i != null) {
               list.add(i);
            }
         }

         return list;
      }
   }

   public List<Double> getDoubleList(String path, List<Double> def) {
      List<Object> raw = this.getList(path);
      if (raw == null) {
         if (this.writeDefaults && def != null) {
            this.setProperty(path, def);
         }

         return (List<Double>)(def != null ? def : new ArrayList<>());
      } else {
         List<Double> list = new ArrayList<>();

         for (Object o : raw) {
            Double i = castDouble(o);
            if (i != null) {
               list.add(i);
            }
         }

         return list;
      }
   }

   public List<Boolean> getBooleanList(String path, List<Boolean> def) {
      List<Object> raw = this.getList(path);
      if (raw == null) {
         if (this.writeDefaults && def != null) {
            this.setProperty(path, def);
         }

         return (List<Boolean>)(def != null ? def : new ArrayList<>());
      } else {
         List<Boolean> list = new ArrayList<>();

         for (Object o : raw) {
            Boolean tetsu = castBoolean(o);
            if (tetsu != null) {
               list.add(tetsu);
            }
         }

         return list;
      }
   }

   public List<Vector3> getVectorList(String path, List<Vector3> def) {
      List<YAMLNode> raw = this.getNodeList(path, null);
      List<Vector3> list = new ArrayList<>();

      for (YAMLNode o : raw) {
         Double x = o.getDouble("x");
         Double y = o.getDouble("y");
         Double z = o.getDouble("z");
         if (x != null && y != null && z != null) {
            list.add(Vector3.at(x, y, z));
         }
      }

      return list;
   }

   public List<Vector2> getVector2List(String path, List<Vector2> def) {
      List<YAMLNode> raw = this.getNodeList(path, null);
      List<Vector2> list = new ArrayList<>();

      for (YAMLNode o : raw) {
         Double x = o.getDouble("x");
         Double z = o.getDouble("z");
         if (x != null && z != null) {
            list.add(Vector2.at(x, z));
         }
      }

      return list;
   }

   public List<BlockVector2> getBlockVector2List(String path, List<BlockVector2> def) {
      List<YAMLNode> raw = this.getNodeList(path, null);
      List<BlockVector2> list = new ArrayList<>();

      for (YAMLNode o : raw) {
         Double x = o.getDouble("x");
         Double z = o.getDouble("z");
         if (x != null && z != null) {
            list.add(BlockVector2.at(x, z));
         }
      }

      return list;
   }

   public List<YAMLNode> getNodeList(String path, List<YAMLNode> def) {
      List<Object> raw = this.getList(path);
      if (raw == null) {
         if (this.writeDefaults && def != null) {
            this.setProperty(path, def);
         }

         return (List<YAMLNode>)(def != null ? def : new ArrayList<>());
      } else {
         List<YAMLNode> list = new ArrayList<>();

         for (Object o : raw) {
            if (o instanceof Map) {
               list.add(new YAMLNode((Map<String, Object>)o, this.writeDefaults));
            }
         }

         return list;
      }
   }

   @Nullable
   public YAMLNode getNode(String path) {
      Object raw = this.getProperty(path);
      return raw instanceof Map ? new YAMLNode((Map<String, Object>)raw, this.writeDefaults) : null;
   }

   public Map<String, YAMLNode> getNodes(String path) {
      Object o = this.getProperty(path);
      if (o == null) {
         return null;
      } else if (o instanceof Map) {
         Map<String, YAMLNode> nodes = new LinkedHashMap<>();

         for (Entry<String, Object> entry : ((Map)o).entrySet()) {
            if (entry.getValue() instanceof Map) {
               nodes.put(entry.getKey(), new YAMLNode((Map<String, Object>)entry.getValue(), this.writeDefaults));
            }
         }

         return nodes;
      } else {
         return null;
      }
   }

   public void removeProperty(String path) {
      if (!path.contains(".")) {
         this.root.remove(path);
      } else {
         String[] parts = path.split("\\.");
         Map<String, Object> node = this.root;

         for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);
            if (i == parts.length - 1) {
               node.remove(parts[i]);
               return;
            }

            node = (Map<String, Object>)o;
         }
      }
   }

   public boolean writeDefaults() {
      return this.writeDefaults;
   }

   public void setWriteDefaults(boolean writeDefaults) {
      this.writeDefaults = writeDefaults;
   }
}
