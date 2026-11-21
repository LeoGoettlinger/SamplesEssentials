package org.sethomegui.SetHomeGUI.libs.redis.jedis.resps;

import java.util.List;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.ScanParams;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public class ScanResult<T> {
   private byte[] cursor;
   private List<T> results;

   public ScanResult(String cursor, List<T> results) {
      this(SafeEncoder.encode(cursor), results);
   }

   public ScanResult(byte[] cursor, List<T> results) {
      this.cursor = cursor;
      this.results = results;
   }

   public String getCursor() {
      return SafeEncoder.encode(this.cursor);
   }

   public boolean isCompleteIteration() {
      return ScanParams.SCAN_POINTER_START.equals(this.getCursor());
   }

   public byte[] getCursorAsBytes() {
      return this.cursor;
   }

   public List<T> getResult() {
      return this.results;
   }
}
