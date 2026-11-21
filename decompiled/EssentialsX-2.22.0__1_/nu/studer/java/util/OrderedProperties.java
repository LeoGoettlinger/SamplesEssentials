package nu.studer.java.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

public final class OrderedProperties implements Serializable {
   private static final long serialVersionUID = 1L;
   private transient Map<String, String> properties;
   private transient boolean suppressDate;

   public OrderedProperties() {
      this(new LinkedHashMap<>(), false);
   }

   private OrderedProperties(Map<String, String> properties, boolean suppressDate) {
      this.properties = properties;
      this.suppressDate = suppressDate;
   }

   public String getProperty(String key) {
      return this.properties.get(key);
   }

   public String getProperty(String key, String defaultValue) {
      String value = this.properties.get(key);
      return value == null ? defaultValue : value;
   }

   public String setProperty(String key, String value) {
      return this.properties.put(key, value);
   }

   public String removeProperty(String key) {
      return this.properties.remove(key);
   }

   public boolean containsProperty(String key) {
      return this.properties.containsKey(key);
   }

   public int size() {
      return this.properties.size();
   }

   public boolean isEmpty() {
      return this.properties.isEmpty();
   }

   public Enumeration<String> propertyNames() {
      return new Vector<>(this.properties.keySet()).elements();
   }

   public Set<String> stringPropertyNames() {
      return new LinkedHashSet<>(this.properties.keySet());
   }

   public Set<Entry<String, String>> entrySet() {
      return new LinkedHashSet<>(this.properties.entrySet());
   }

   public void load(InputStream stream) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.load(stream);
   }

   public void load(Reader reader) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.load(reader);
   }

   public void loadFromXML(InputStream stream) throws IOException, InvalidPropertiesFormatException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.loadFromXML(stream);
   }

   public void store(OutputStream stream, String comments) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      if (this.suppressDate) {
         this.store(stream, comments, customProperties);
      } else {
         customProperties.store(stream, comments);
      }
   }

   private void store(OutputStream stream, String comments, OrderedProperties.CustomProperties properties) throws IOException {
      if (!commentRequiresEscaping(comments) && !propertiesRequireEscaping(properties)) {
         properties.store(new OrderedProperties.DateSuppressingPropertiesBufferedWriter(new OutputStreamWriter(stream, "8859_1")), comments);
      } else {
         storeViaReflection(stream, comments, properties);
      }
   }

   private static boolean commentRequiresEscaping(String comments) {
      return comments != null && comments.chars().anyMatch(c -> c > 255);
   }

   private static boolean propertiesRequireEscaping(OrderedProperties.CustomProperties properties) {
      Map<String, String> p = properties.targetProperties;
      return !p.isEmpty() && (p.keySet().stream().anyMatch(k -> keyValueRequiresEscaping(k)) || p.values().stream().anyMatch(v -> keyValueRequiresEscaping(v)));
   }

   private static boolean keyValueRequiresEscaping(String s) {
      return s != null && s.chars().anyMatch(c -> c < 32 || c > 126);
   }

   private static void storeViaReflection(OutputStream stream, String comments, OrderedProperties.CustomProperties customProperties) throws IOException {
      try {
         Method method = Properties.class.getDeclaredMethod("store0", BufferedWriter.class, String.class, boolean.class);
         method.setAccessible(true);
         method.invoke(
            customProperties, new OrderedProperties.DateSuppressingPropertiesBufferedWriter(new OutputStreamWriter(stream, "8859_1")), comments, true
         );
      } catch (IllegalAccessException | SecurityException | InvocationTargetException | NoSuchMethodException var4) {
         throw new RuntimeException(var4.getCause());
      }
   }

   public void store(Writer writer, String comments) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      if (this.suppressDate) {
         customProperties.store(new OrderedProperties.DateSuppressingPropertiesBufferedWriter(writer), comments);
      } else {
         customProperties.store(writer, comments);
      }
   }

   public void storeToXML(OutputStream stream, String comment) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.storeToXML(stream, comment);
   }

   public void storeToXML(OutputStream stream, String comment, String encoding) throws IOException {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.storeToXML(stream, comment, encoding);
   }

   public void list(PrintStream stream) {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.list(stream);
   }

   public void list(PrintWriter writer) {
      OrderedProperties.CustomProperties customProperties = new OrderedProperties.CustomProperties(this.properties);
      customProperties.list(writer);
   }

   public Properties toJdkProperties() {
      Properties jdkProperties = new Properties();

      for (Entry<String, String> entry : this.entrySet()) {
         jdkProperties.put(entry.getKey(), entry.getValue());
      }

      return jdkProperties;
   }

   @Override
   public boolean equals(Object other) {
      if (this == other) {
         return true;
      } else if (other != null && this.getClass() == other.getClass()) {
         OrderedProperties that = (OrderedProperties)other;
         return Arrays.equals(this.properties.entrySet().toArray(), that.properties.entrySet().toArray());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode(this.properties.entrySet().toArray());
   }

   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.properties);
      stream.writeBoolean(this.suppressDate);
   }

   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      this.properties = (Map<String, String>)stream.readObject();
      this.suppressDate = stream.readBoolean();
   }

   private void readObjectNoData() throws InvalidObjectException {
      throw new InvalidObjectException("Stream data required");
   }

   @Override
   public String toString() {
      return this.properties.toString();
   }

   public static OrderedProperties copyOf(OrderedProperties source) {
      OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
      builder.withSuppressDateInComment(source.suppressDate);
      if (source.properties instanceof TreeMap) {
         builder.withOrdering(((TreeMap)source.properties).comparator());
      }

      OrderedProperties result = builder.build();

      for (Entry<String, String> entry : source.entrySet()) {
         result.setProperty(entry.getKey(), entry.getValue());
      }

      return result;
   }

   private static final class CustomProperties extends Properties {
      private final Map<String, String> targetProperties;

      private CustomProperties(Map<String, String> targetProperties) {
         this.targetProperties = targetProperties;
      }

      @Override
      public Object get(Object key) {
         return this.targetProperties.get(key);
      }

      @Override
      public Object put(Object key, Object value) {
         return this.targetProperties.put((String)key, (String)value);
      }

      @Override
      public String getProperty(String key) {
         return this.targetProperties.get(key);
      }

      @Override
      public Enumeration<Object> keys() {
         return new Vector<>(this.targetProperties.keySet()).elements();
      }

      @Override
      public Set<Object> keySet() {
         return new LinkedHashSet<>(this.targetProperties.keySet());
      }

      @Override
      public Set<Entry<Object, Object>> entrySet() {
         return this.targetProperties.entrySet();
      }
   }

   private static final class DateSuppressingPropertiesBufferedWriter extends BufferedWriter {
      private final String LINE_SEPARATOR = System.getProperty("line.separator");
      private StringBuilder currentComment;
      private String previousComment;

      private DateSuppressingPropertiesBufferedWriter(Writer out) {
         super(out);
      }

      @Override
      public void write(String string) throws IOException {
         if (this.currentComment != null) {
            this.currentComment.append(string);
            if (string.endsWith(this.LINE_SEPARATOR)) {
               if (this.previousComment != null) {
                  super.write(this.previousComment);
               }

               this.previousComment = this.currentComment.toString();
               this.currentComment = null;
            }
         } else if (string.startsWith("#")) {
            this.currentComment = new StringBuilder(string);
         } else {
            super.write(string);
         }
      }
   }

   public static final class OrderedPropertiesBuilder {
      private Comparator<? super String> comparator;
      private boolean suppressDate;

      public OrderedProperties.OrderedPropertiesBuilder withOrdering(Comparator<? super String> comparator) {
         this.comparator = comparator;
         return this;
      }

      public OrderedProperties.OrderedPropertiesBuilder withSuppressDateInComment(boolean suppressDate) {
         this.suppressDate = suppressDate;
         return this;
      }

      public OrderedProperties build() {
         Map<String, String> properties = (Map<String, String>)(this.comparator != null ? new TreeMap<>(this.comparator) : new LinkedHashMap<>());
         return new OrderedProperties(properties, this.suppressDate);
      }
   }
}
