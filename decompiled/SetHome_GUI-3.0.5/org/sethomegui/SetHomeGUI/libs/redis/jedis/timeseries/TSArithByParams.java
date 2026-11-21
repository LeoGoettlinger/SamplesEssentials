package org.sethomegui.SetHomeGUI.libs.redis.jedis.timeseries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

class TSArithByParams<T extends TSArithByParams<?>> implements IParams {
   private Long timestamp;
   private Long retentionPeriod;
   private EncodingFormat encoding;
   private Long chunkSize;
   private DuplicatePolicy duplicatePolicy;
   private boolean ignore;
   private long ignoreMaxTimediff;
   private double ignoreMaxValDiff;
   private Map<String, String> labels;

   public T timestamp(long timestamp) {
      this.timestamp = timestamp;
      return (T)this;
   }

   public T retention(long retentionPeriod) {
      this.retentionPeriod = retentionPeriod;
      return (T)this;
   }

   public T encoding(EncodingFormat encoding) {
      this.encoding = encoding;
      return (T)this;
   }

   public T chunkSize(long chunkSize) {
      this.chunkSize = chunkSize;
      return (T)this;
   }

   public T duplicatePolicy(DuplicatePolicy duplicatePolicy) {
      this.duplicatePolicy = duplicatePolicy;
      return (T)this;
   }

   public T ignore(long maxTimediff, double maxValDiff) {
      this.ignore = true;
      this.ignoreMaxTimediff = maxTimediff;
      this.ignoreMaxValDiff = maxValDiff;
      return (T)this;
   }

   public T labels(Map<String, String> labels) {
      this.labels = labels;
      return (T)this;
   }

   public T label(String label, String value) {
      if (this.labels == null) {
         this.labels = new LinkedHashMap<>();
      }

      this.labels.put(label, value);
      return (T)this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.timestamp != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.TIMESTAMP).add(this.timestamp);
      }

      if (this.retentionPeriod != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.RETENTION).add(Protocol.toByteArray(this.retentionPeriod));
      }

      if (this.encoding != null) {
         args.add((Rawable)TimeSeriesProtocol.TimeSeriesKeyword.ENCODING).add((Rawable)this.encoding);
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
         TSArithByParams<?> that = (TSArithByParams<?>)o;
         return this.ignore == that.ignore
            && this.ignoreMaxTimediff == that.ignoreMaxTimediff
            && Double.compare(this.ignoreMaxValDiff, that.ignoreMaxValDiff) == 0
            && Objects.equals(this.timestamp, that.timestamp)
            && Objects.equals(this.retentionPeriod, that.retentionPeriod)
            && this.encoding == that.encoding
            && Objects.equals(this.chunkSize, that.chunkSize)
            && this.duplicatePolicy == that.duplicatePolicy
            && Objects.equals(this.labels, that.labels);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.timestamp);
      result = 31 * result + Objects.hashCode(this.retentionPeriod);
      result = 31 * result + Objects.hashCode(this.encoding);
      result = 31 * result + Objects.hashCode(this.chunkSize);
      result = 31 * result + Objects.hashCode(this.duplicatePolicy);
      result = 31 * result + Boolean.hashCode(this.ignore);
      result = 31 * result + Long.hashCode(this.ignoreMaxTimediff);
      result = 31 * result + Double.hashCode(this.ignoreMaxValDiff);
      return 31 * result + Objects.hashCode(this.labels);
   }
}
