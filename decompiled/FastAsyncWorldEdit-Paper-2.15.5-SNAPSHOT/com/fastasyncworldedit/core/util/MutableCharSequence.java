package com.fastasyncworldedit.core.util;

public class MutableCharSequence implements CharSequence {
   private String str;
   private int start;
   private int length;
   private static final ThreadLocal<MutableCharSequence> mutableChar = ThreadLocal.withInitial(MutableCharSequence::new);

   public static MutableCharSequence getTemporal() {
      return mutableChar.get();
   }

   public MutableCharSequence(String parent, int start, int length) {
      this.str = parent;
      this.start = start;
      this.length = length;
   }

   public MutableCharSequence() {
   }

   public void setSubstring(int start, int end) {
      this.start = start;
      this.length = end - start;
   }

   public void setString(String str) {
      this.str = str;
   }

   @Override
   public int length() {
      return this.length;
   }

   @Override
   public char charAt(int index) {
      return this.str.charAt(index + this.start);
   }

   @Override
   public CharSequence subSequence(int start, int end) {
      return new MutableCharSequence(this.str, this.start + start, end - start + 1);
   }

   @Override
   public int hashCode() {
      int h = 0;

      for (int i = 0; i < this.length; i++) {
         h = 31 * h + this.str.charAt(i + this.start);
      }

      return h;
   }

   @Override
   public String toString() {
      return this.str.substring(this.start, this.start + this.length);
   }

   public int indexOf(char c) {
      for (int i = 0; i < this.length; i++) {
         if (this.charAt(i) == c) {
            return i;
         }
      }

      return -1;
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
