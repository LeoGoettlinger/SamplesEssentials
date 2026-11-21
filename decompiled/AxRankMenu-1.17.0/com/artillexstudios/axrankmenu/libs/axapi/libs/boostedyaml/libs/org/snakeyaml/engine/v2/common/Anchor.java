package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.EmitterException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Anchor {
   private static final Set<Character> INVALID_ANCHOR = new HashSet<>();
   private static final Pattern SPACES_PATTERN = Pattern.compile("\\s");
   private final String value;

   public Anchor(String value) {
      Objects.requireNonNull(value);
      if (value.isEmpty()) {
         throw new IllegalArgumentException("Empty anchor.");
      } else {
         for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (INVALID_ANCHOR.contains(ch)) {
               throw new EmitterException("Invalid character '" + ch + "' in the anchor: " + value);
            }
         }

         Matcher matcher = SPACES_PATTERN.matcher(value);
         if (matcher.find()) {
            throw new EmitterException("Anchor may not contain spaces: " + value);
         } else {
            this.value = value;
         }
      }
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return this.value;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Anchor anchor1 = (Anchor)o;
         return Objects.equals(this.value, anchor1.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }

   static {
      INVALID_ANCHOR.add('[');
      INVALID_ANCHOR.add(']');
      INVALID_ANCHOR.add('{');
      INVALID_ANCHOR.add('}');
      INVALID_ANCHOR.add(',');
      INVALID_ANCHOR.add('*');
      INVALID_ANCHOR.add('&');
   }
}
