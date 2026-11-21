package github.nighter.smartspawner.libs.mariadb.util;

public final class StringUtils {
   private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

   public static String byteArrayToHexString(byte[] bytes) {
      return bytes != null ? getHex(bytes) : "";
   }

   private static String getHex(byte[] raw) {
      StringBuilder hex = new StringBuilder(2 * raw.length);

      for (byte b : raw) {
         hex.append(hexArray[(b & 240) >> 4]).append(hexArray[b & 15]);
      }

      return hex.toString();
   }
}
