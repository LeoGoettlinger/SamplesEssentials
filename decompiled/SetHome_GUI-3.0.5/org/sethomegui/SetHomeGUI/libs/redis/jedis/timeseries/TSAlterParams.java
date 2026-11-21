package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class TSAlterParams implements IParams {
   private Long retentionPeriod;
   private Long chunkSize;
   private DuplicatePolicy duplicatePolicy;
   private boolean ignore;
   private long ignoreMaxTimediff;
   private double ignoreMaxValDiff;
   private Map<String, String> labels;

   public static TSAlterParams alterParams() {
      return new TSAlterParams();
   }

   public TSAlterParams retention(long retentionPeriod) {
      this.retentionPeriod = retentionPeriod;
      return this;
   }

   public TSAlterParams chunkSize(long chunkSize) {
      this.chunkSize = chunkSize;
      return this;
   }

   public TSAlterParams duplicatePolicy(DuplicatePolicy duplicatePolicy) {
      this.duplicatePolicy = duplicatePolicy;
      return this;
   }

   public TSAlterParams ignore(long maxTimediff, double maxValDiff) {
      this.ignore = true;
      this.ignoreMaxTimediff = maxTimediff;
      this.ignoreMaxValDiff = maxValDiff;
      return this;
   }

   public TSAlterParams labels(Map<String, String> labels) {
      this.labels = labels;
      return this;
   }

   public TSAlterParams label(String label, String value) {
      if (this.labels == null) {
         this.labels = new LinkedHashMap<>();
      }

      this.labels.put(label, value);
      return this;
   }

   public TSAlterParams labelsReset() {
      return this.labels(Collections.emptyMap());
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.retentionPeriod != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.RETENTION).add(Protocol.toByteArray(this.retentionPeriod));
      }

      if (this.chunkSize != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.CHUNK_SIZE).add(Protocol.toByteArray(this.chunkSize));
      }

      if (this.duplicatePolicy != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.DUPLICATE_POLICY).add((Rawable)this.duplicatePolicy);
      }

      if (this.ignore) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.IGNORE).add(this.ignoreMaxTimediff).add(this.ignoreMaxValDiff);
      }

      if (this.labels != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.LABELS);
         this.labels.entrySet().forEach(entry -> args.add(entry.getKey()).add(entry.getValue()));
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TSAlterParams that = (TSAlterParams)o;
         return this.ignore == that.ignore
            && this.ignoreMaxTimediff == that.ignoreMaxTimediff
            && Double.compare(this.ignoreMaxValDiff, that.ignoreMaxValDiff) == 0
            && Objects.equals(this.retentionPeriod, that.retentionPeriod)
            && Objects.equals(this.chunkSize, that.chunkSize)
            && this.duplicatePolicy == that.duplicatePolicy
            && Objects.equals(this.labels, that.labels);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.retentionPeriod);
      result = 31 * result + Objects.hashCode(this.chunkSize);
      result = 31 * result + Objects.hashCode(this.duplicatePolicy);
      result = 31 * result + Boolean.hashCode(this.ignore);
      result = 31 * result + Long.hashCode(this.ignoreMaxTimediff);
      result = 31 * result + Double.hashCode(this.ignoreMaxValDiff);
      return 31 * result + Objects.hashCode(this.labels);
   }
}
