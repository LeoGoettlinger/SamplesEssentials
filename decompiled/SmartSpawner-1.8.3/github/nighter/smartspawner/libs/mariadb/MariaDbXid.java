package github.nighter.smartspawner.libs.mariadb;

import java.util.Arrays;
import java.util.Objects;
import javax.transaction.xa.Xid;

public class MariaDbXid implements Xid {
   private final int formatId;
   private final byte[] globalTransactionId;
   private final byte[] branchQualifier;

   public MariaDbXid(int formatId, byte[] globalTransactionId, byte[] branchQualifier) {
      this.formatId = formatId;
      this.globalTransactionId = globalTransactionId;
      this.branchQualifier = branchQualifier;
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof Xid)) {
         return false;
      } else {
         Xid other = (Xid)obj;
         return this.formatId == other.getFormatId()
            && Arrays.equals(this.globalTransactionId, other.getGlobalTransactionId())
            && Arrays.equals(this.branchQualifier, other.getBranchQualifier());
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hash(this.formatId);
      result = 31 * result + Arrays.hashCode(this.globalTransactionId);
      return 31 * result + Arrays.hashCode(this.branchQualifier);
   }

   @Override
   public int getFormatId() {
      return this.formatId;
   }

   @Override
   public byte[] getGlobalTransactionId() {
      return this.globalTransactionId;
   }

   @Override
   public byte[] getBranchQualifier() {
      return this.branchQualifier;
   }
}
