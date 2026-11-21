package org.apache.commons.lang3;

import org.apache.commons.lang3.builder.AbstractSupplier;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

public abstract class Strings {
   public static final Strings CI = new Strings.CiStrings(true);
   public static final Strings CS = new Strings.CsStrings(true);
   private final boolean ignoreCase;
   private final boolean nullIsLess;

   public static final Strings.Builder builder() {
      return new Strings.Builder();
   }

   private static boolean containsAny(ToBooleanBiFunction<CharSequence, CharSequence> test, CharSequence cs, CharSequence... searchCharSequences) {
      if (!StringUtils.isEmpty(cs) && !ArrayUtils.isEmpty((Object[])searchCharSequences)) {
         for (CharSequence searchCharSequence : searchCharSequences) {
            if (test.applyAsBoolean(cs, searchCharSequence)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean eq(Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
   }

   private Strings(boolean ignoreCase, boolean nullIsLess) {
      this.ignoreCase = ignoreCase;
      this.nullIsLess = nullIsLess;
   }

   public String appendIfMissing(String str, CharSequence suffix, CharSequence... suffixes) {
      if (str != null && !StringUtils.isEmpty(suffix) && !this.endsWith(str, suffix)) {
         if (ArrayUtils.isNotEmpty((CharSequence[])suffixes)) {
            for (CharSequence s : suffixes) {
               if (this.endsWith(str, s)) {
                  return str;
               }
            }
         }

         return str + suffix;
      } else {
         return str;
      }
   }

   public abstract int compare(String var1, String var2);

   public abstract boolean contains(CharSequence var1, CharSequence var2);

   public boolean containsAny(CharSequence cs, CharSequence... searchCharSequences) {
      return containsAny(this::contains, cs, searchCharSequences);
   }

   public boolean endsWith(CharSequence str, CharSequence suffix) {
      if (str != null && suffix != null) {
         int sufLen = suffix.length();
         return sufLen > str.length() ? false : CharSequenceUtils.regionMatches(str, this.ignoreCase, str.length() - sufLen, suffix, 0, sufLen);
      } else {
         return str == suffix;
      }
   }

   public boolean endsWithAny(CharSequence sequence, CharSequence... searchStrings) {
      if (!StringUtils.isEmpty(sequence) && !ArrayUtils.isEmpty((Object[])searchStrings)) {
         for (CharSequence searchString : searchStrings) {
            if (this.endsWith(sequence, searchString)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public abstract boolean equals(CharSequence var1, CharSequence var2);

   public abstract boolean equals(String var1, String var2);

   public boolean equalsAny(CharSequence string, CharSequence... searchStrings) {
      if (ArrayUtils.isNotEmpty((CharSequence[])searchStrings)) {
         for (CharSequence next : searchStrings) {
            if (this.equals(string, next)) {
               return true;
            }
         }
      }

      return false;
   }

   public int indexOf(CharSequence seq, CharSequence searchSeq) {
      return this.indexOf(seq, searchSeq, 0);
   }

   public abstract int indexOf(CharSequence var1, CharSequence var2, int var3);

   public boolean isCaseSensitive() {
      return !this.ignoreCase;
   }

   boolean isNullIsLess() {
      return this.nullIsLess;
   }

   public int lastIndexOf(CharSequence str, CharSequence searchStr) {
      return str == null ? -1 : this.lastIndexOf(str, searchStr, str.length());
   }

   public abstract int lastIndexOf(CharSequence var1, CharSequence var2, int var3);

   public String prependIfMissing(String str, CharSequence prefix, CharSequence... prefixes) {
      if (str != null && !StringUtils.isEmpty(prefix) && !this.startsWith(str, prefix)) {
         if (ArrayUtils.isNotEmpty((CharSequence[])prefixes)) {
            for (CharSequence p : prefixes) {
               if (this.startsWith(str, p)) {
                  return str;
               }
            }
         }

         return prefix + str;
      } else {
         return str;
      }
   }

   public String remove(String str, String remove) {
      return this.replace(str, remove, "", -1);
   }

   public String removeEnd(String str, CharSequence remove) {
      if (StringUtils.isEmpty(str) || StringUtils.isEmpty(remove)) {
         return str;
      } else {
         return this.endsWith(str, remove) ? str.substring(0, str.length() - remove.length()) : str;
      }
   }

   public String removeStart(String str, CharSequence remove) {
      return str != null && this.startsWith(str, remove) ? str.substring(StringUtils.length(remove)) : str;
   }

   public String replace(String text, String searchString, String replacement) {
      return this.replace(text, searchString, replacement, -1);
   }

   public String replace(String text, String searchString, String replacement, int max) {
      if (!StringUtils.isEmpty(text) && !StringUtils.isEmpty(searchString) && replacement != null && max != 0) {
         if (this.ignoreCase) {
            searchString = searchString.toLowerCase();
         }

         int start = 0;
         int end = this.indexOf(text, searchString, start);
         if (end == -1) {
            return text;
         } else {
            int replLength = searchString.length();
            int increase = Math.max(replacement.length() - replLength, 0);
            increase *= max < 0 ? 16 : Math.min(max, 64);

            StringBuilder buf;
            for (buf = new StringBuilder(text.length() + increase); end != -1; end = this.indexOf(text, searchString, start)) {
               buf.append(text, start, end).append(replacement);
               start = end + replLength;
               if (--max == 0) {
                  break;
               }
            }

            buf.append(text, start, text.length());
            return buf.toString();
         }
      } else {
         return text;
      }
   }

   public String replaceOnce(String text, String searchString, String replacement) {
      return this.replace(text, searchString, replacement, 1);
   }

   public boolean startsWith(CharSequence str, CharSequence prefix) {
      if (str != null && prefix != null) {
         int preLen = prefix.length();
         return preLen > str.length() ? false : CharSequenceUtils.regionMatches(str, this.ignoreCase, 0, prefix, 0, preLen);
      } else {
         return str == prefix;
      }
   }

   public boolean startsWithAny(CharSequence sequence, CharSequence... searchStrings) {
      if (!StringUtils.isEmpty(sequence) && !ArrayUtils.isEmpty((Object[])searchStrings)) {
         for (CharSequence searchString : searchStrings) {
            if (this.startsWith(sequence, searchString)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static class Builder extends AbstractSupplier<Strings, Strings.Builder, RuntimeException> {
      private boolean ignoreCase;
      private boolean nullIsLess;

      private Builder() {
      }

      public Strings get() {
         return (Strings)(this.ignoreCase ? new Strings.CiStrings(this.nullIsLess) : new Strings.CsStrings(this.nullIsLess));
      }

      public Strings.Builder setIgnoreCase(boolean ignoreCase) {
         this.ignoreCase = ignoreCase;
         return this.asThis();
      }

      public Strings.Builder setNullIsLess(boolean nullIsLess) {
         this.nullIsLess = nullIsLess;
         return this.asThis();
      }
   }

   private static final class CiStrings extends Strings {
      private CiStrings(boolean nullIsLess) {
         super(true, nullIsLess);
      }

      @Override
      public int compare(String s1, String s2) {
         if (s1 == s2) {
            return 0;
         } else if (s1 == null) {
            return this.isNullIsLess() ? -1 : 1;
         } else if (s2 == null) {
            return this.isNullIsLess() ? 1 : -1;
         } else {
            return s1.compareToIgnoreCase(s2);
         }
      }

      @Override
      public boolean contains(CharSequence str, CharSequence searchStr) {
         if (str != null && searchStr != null) {
            int len = searchStr.length();
            int max = str.length() - len;

            for (int i = 0; i <= max; i++) {
               if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public boolean equals(CharSequence cs1, CharSequence cs2) {
         if (cs1 == cs2) {
            return true;
         } else if (cs1 != null && cs2 != null) {
            return cs1.length() != cs2.length() ? false : CharSequenceUtils.regionMatches(cs1, true, 0, cs2, 0, cs1.length());
         } else {
            return false;
         }
      }

      @Override
      public boolean equals(String s1, String s2) {
         return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
      }

      @Override
      public int indexOf(CharSequence str, CharSequence searchStr, int startPos) {
         if (str != null && searchStr != null) {
            if (startPos < 0) {
               startPos = 0;
            }

            int endLimit = str.length() - searchStr.length() + 1;
            if (startPos > endLimit) {
               return -1;
            } else if (searchStr.length() == 0) {
               return startPos;
            } else {
               for (int i = startPos; i < endLimit; i++) {
                  if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) {
                     return i;
                  }
               }

               return -1;
            }
         } else {
            return -1;
         }
      }

      @Override
      public int lastIndexOf(CharSequence str, CharSequence searchStr, int startPos) {
         if (str != null && searchStr != null) {
            int searchStrLength = searchStr.length();
            int strLength = str.length();
            if (startPos > strLength - searchStrLength) {
               startPos = strLength - searchStrLength;
            }

            if (startPos < 0) {
               return -1;
            } else if (searchStrLength == 0) {
               return startPos;
            } else {
               for (int i = startPos; i >= 0; i--) {
                  if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStrLength)) {
                     return i;
                  }
               }

               return -1;
            }
         } else {
            return -1;
         }
      }
   }

   private static final class CsStrings extends Strings {
      private CsStrings(boolean nullIsLess) {
         super(false, nullIsLess);
      }

      @Override
      public int compare(String s1, String s2) {
         if (s1 == s2) {
            return 0;
         } else if (s1 == null) {
            return this.isNullIsLess() ? -1 : 1;
         } else if (s2 == null) {
            return this.isNullIsLess() ? 1 : -1;
         } else {
            return s1.compareTo(s2);
         }
      }

      @Override
      public boolean contains(CharSequence seq, CharSequence searchSeq) {
         return CharSequenceUtils.indexOf(seq, searchSeq, 0) >= 0;
      }

      @Override
      public boolean equals(CharSequence cs1, CharSequence cs2) {
         if (cs1 == cs2) {
            return true;
         } else if (cs1 != null && cs2 != null) {
            if (cs1.length() != cs2.length()) {
               return false;
            } else if (cs1 instanceof String && cs2 instanceof String) {
               return cs1.equals(cs2);
            } else {
               int length = cs1.length();

               for (int i = 0; i < length; i++) {
                  if (cs1.charAt(i) != cs2.charAt(i)) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      }

      @Override
      public boolean equals(String s1, String s2) {
         return Strings.eq(s1, s2);
      }

      @Override
      public int indexOf(CharSequence seq, CharSequence searchSeq, int startPos) {
         return CharSequenceUtils.indexOf(seq, searchSeq, startPos);
      }

      @Override
      public int lastIndexOf(CharSequence seq, CharSequence searchSeq, int startPos) {
         return CharSequenceUtils.lastIndexOf(seq, searchSeq, startPos);
      }
   }
}
