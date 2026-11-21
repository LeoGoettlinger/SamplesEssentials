package ac.grim.grimac.api.storage;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record DeletionReport(int sessionsDeleted, int violationsDeleted, int settingsDeleted, int identitiesDeleted, int blobsDeleted) {
   public static final DeletionReport EMPTY = new DeletionReport(0, 0, 0, 0, 0);

   public DeletionReport plus(DeletionReport other) {
      return new DeletionReport(
         this.sessionsDeleted + other.sessionsDeleted,
         this.violationsDeleted + other.violationsDeleted,
         this.settingsDeleted + other.settingsDeleted,
         this.identitiesDeleted + other.identitiesDeleted,
         this.blobsDeleted + other.blobsDeleted
      );
   }

   public int total() {
      return this.sessionsDeleted + this.violationsDeleted + this.settingsDeleted + this.identitiesDeleted + this.blobsDeleted;
   }
}
