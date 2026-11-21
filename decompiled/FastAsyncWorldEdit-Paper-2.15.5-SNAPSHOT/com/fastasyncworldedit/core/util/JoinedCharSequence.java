package com.fastasyncworldedit.core.util;

@Deprecated(
   forRemoval = true,
   since = "2.5.1"
)
public class JoinedCharSequence implements CharSequence {
   private char join;
   private int len2;
   private int len1;
   private int length;
   private String a;
   private String b;

   public JoinedCharSequence init(String a, char join, String b) {
      this.len1 = a.length();
      this.len2 = b.length();
      this.length = this.len1 + this.len2 + 1;
      this.join = join;
      this.a = a;
      this.b = b;
      return this;
   }

   @Override
   public int length() {
      return this.length;
   }

   @Override
   public char charAt(int index) {
      if (index < this.len1) {
         return this.a.charAt(index);
      } else {
         return index == this.len1 ? this.join : this.b.charAt(index - this.len1 - 1);
      }
   }

   @Override
   public CharSequence subSequence(int start, int end) {
      char[] chars = new char[end - start];
      int i = start;

      for (int j = 0; i < end; j++) {
         chars[j] = this.charAt(i);
         i++;
      }

      return new String(chars);
   }

   @Override
   public int hashCode() {
      int h = 0;

      for (int i = 0; i < this.length; i++) {
         h = 31 * h + this.charAt(i);
      }

      return h;
   }

   @Override
   public String toString() {
      return (String)this.subSequence(0, this.length);
   }

   @Override
   public boolean equals(Object obj) {
      CharSequence anotherString = (CharSequence)obj;
      if (this.length == anotherString.length()) {
         for (int i = this.length - 1; i >= 0; i--) {
            if (this.charAt(i) != anotherString.charAt(i)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
