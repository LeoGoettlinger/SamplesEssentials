package ch.njol.skript.config;

import ch.njol.skript.lang.util.common.AnyValued;
import java.util.Arrays;
import java.util.Map.Entry;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class EntryNode extends Node implements Entry<String, String>, AnyValued<String> {
   private String value;

   public EntryNode(String key, String value, String comment, SectionNode parent, int lineNum) {
      super(key, comment, parent, lineNum);
      this.value = value;
   }

   public EntryNode(String key, String value, SectionNode parent) {
      super(key, parent);
      this.value = value;
   }

   @Override
   public String getKey() {
      return this.key;
   }

   public String getValue() {
      return this.value;
   }

   @UnknownNullability
   public String value() {
      return this.getValue();
   }

   public String setValue(@Nullable String v) {
      if (v == null) {
         return this.value;
      } else {
         String r = this.value;
         this.value = v;
         return r;
      }
   }

   public void changeValue(String value) throws UnsupportedOperationException {
      this.setValue(value);
   }

   @Override
   public Class<String> valueType() {
      return String.class;
   }

   @Override
   public boolean supportsValueChange() {
      return false;
   }

   @Override
   String save_i() {
      return this.key + this.config.getSaveSeparator() + this.value;
   }

   @Nullable
   @Override
   public Node get(String step) {
      return null;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof EntryNode other ? Arrays.equals((Object[])this.getPathSteps(), (Object[])other.getPathSteps()) : false;
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.getPathSteps());
   }
}
