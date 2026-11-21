package de.theredend2000.advancedhunt.lib.cronutils.model.definition;

import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import java.io.Serializable;
import java.util.Objects;

public abstract class CronConstraint implements Serializable {
   private static final long serialVersionUID = 6866660085991775528L;
   private final String description;

   public CronConstraint(String description) {
      this.description = description;
   }

   public abstract boolean validate(Cron var1);

   public String getDescription() {
      return this.description;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CronConstraint that = (CronConstraint)o;
         return Objects.equals(this.description, that.description);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.description);
   }
}
