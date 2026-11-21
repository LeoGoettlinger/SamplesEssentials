package ch.njol.skript.util;

import java.io.Serializable;
import java.lang.invoke.StringConcatFactory;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public class Version implements Serializable, Comparable<Version> {
   private static final long serialVersionUID = 8687040355286333293L;
   private final Integer[] version = new Integer[3];
   @Nullable
   private final String postfix;
   public static final Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?");

   public Version(int... version) {
      if (version.length >= 1 && version.length <= 3) {
         for (int i = 0; i < version.length; i++) {
            this.version[i] = version[i];
         }

         this.postfix = null;
      } else {
         throw new IllegalArgumentException("Versions must have a minimum of 2 and a maximum of 3 numbers (" + version.length + " numbers given)");
      }
   }

   public Version(int major, int minor, @Nullable String postfix) {
      this.version[0] = major;
      this.version[1] = minor;
      this.postfix = postfix != null && !postfix.isEmpty() ? postfix : null;
   }

   public Version(String version) {
      Matcher m = versionPattern.matcher(version.trim());
      if (!m.matches()) {
         throw new IllegalArgumentException("'" + version + "' is not a valid version string");
      } else {
         for (int i = 0; i < 3; i++) {
            if (m.group(i + 1) != null) {
               this.version[i] = Utils.parseInt(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(m.group(i + 1)));
            }
         }

         this.postfix = m.group(4);
      }
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Version) ? false : this.compareTo((Version)obj) == 0;
      }
   }

   @Override
   public int hashCode() {
      return Arrays.hashCode((Object[])this.version) * 31 + (this.postfix == null ? 0 : this.postfix.hashCode());
   }

   public int compareTo(@Nullable Version other) {
      if (other == null) {
         return 1;
      } else {
         for (int i = 0; i < this.version.length; i++) {
            if (this.get(i) > other.get(i)) {
               return 1;
            }

            if (this.get(i) < other.get(i)) {
               return -1;
            }
         }

         return comparePostfixes(this.postfix, other.postfix);
      }
   }

   private static int comparePostfixes(@Nullable String postfixA, @Nullable String postfixB) {
      postfixA = postfixA == null ? null : postfixA.toLowerCase();
      postfixB = postfixB == null ? null : postfixB.toLowerCase();
      if (postfixA == null && postfixB == null) {
         return 0;
      } else if (postfixA == null) {
         return postfixB.startsWith("nightly") ? -1 : 1;
      } else if (postfixB == null) {
         return postfixA.startsWith("nightly") ? 1 : -1;
      } else if (postfixA.startsWith("nightly")) {
         return postfixB.startsWith("nightly") ? 0 : 1;
      } else if (postfixB.startsWith("nightly")) {
         return -1;
      } else {
         String[] prefixes = new String[]{"pre", "beta", "alpha"};

         for (String prefix : prefixes) {
            boolean aStarts = postfixA.startsWith(prefix);
            boolean bStarts = postfixB.startsWith(prefix);
            if (aStarts || bStarts) {
               if (aStarts && bStarts) {
                  String aNumberStr = postfixA.substring(prefix.length()).trim().split("-")[0];
                  String bNumberStr = postfixB.substring(prefix.length()).trim().split("-")[0];
                  int aNumber = Math.abs(Utils.parseInt(aNumberStr));
                  int bNumber = Math.abs(Utils.parseInt(bNumberStr));
                  return Integer.compare(aNumber, bNumber);
               } else {
                  return aStarts ? 1 : -1;
               }
            }
         }

         return 0;
      }
   }

   public int compareTo(int... other) {
      assert other.length >= 2 && other.length <= 3;

      for (int i = 0; i < this.version.length; i++) {
         if (this.get(i) > (i >= other.length ? 0 : other[i])) {
            return 1;
         }

         if (this.get(i) < (i >= other.length ? 0 : other[i])) {
            return -1;
         }
      }

      return 0;
   }

   private int get(int i) {
      return this.version[i] == null ? 0 : this.version[i];
   }

   public boolean isSmallerThan(Version other) {
      return this.compareTo(other) < 0;
   }

   public boolean isLargerThan(Version other) {
      return this.compareTo(other) > 0;
   }

   public boolean isStable() {
      return this.postfix == null;
   }

   public int getMajor() {
      return this.version[0];
   }

   public int getMinor() {
      return this.version[1];
   }

   public int getRevision() {
      return this.version[2] == null ? 0 : this.version[2];
   }

   @Override
   public String toString() {
      return this.version[0]
         + "."
         + this.version[1]
         + (this.version[2] == null ? "" : "." + this.version[2])
         + (this.postfix == null ? "" : "-" + this.postfix);
   }

   public static int compare(String v1, String v2) {
      return new Version(v1).compareTo(new Version(v2));
   }
}
