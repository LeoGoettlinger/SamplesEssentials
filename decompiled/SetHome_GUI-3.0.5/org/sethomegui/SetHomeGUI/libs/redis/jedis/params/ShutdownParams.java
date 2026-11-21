package org.sethomegui.SetHomeGUI.libs.redis.jedis.params;

import java.util.Objects;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.Protocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.SaveMode;

public class ShutdownParams implements IParams {
   private SaveMode saveMode;
   private boolean now;
   private boolean force;

   public static ShutdownParams shutdownParams() {
      return new ShutdownParams();
   }

   public ShutdownParams saveMode(SaveMode saveMode) {
      this.saveMode = saveMode;
      return this;
   }

   public ShutdownParams nosave() {
      return this.saveMode(SaveMode.NOSAVE);
   }

   public ShutdownParams save() {
      return this.saveMode(SaveMode.SAVE);
   }

   public ShutdownParams now() {
      this.now = true;
      return this;
   }

   public ShutdownParams force() {
      this.force = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.saveMode != null) {
         args.add((Rawable)this.saveMode);
      }

      if (this.now) {
         args.add((Rawable)Protocol.Keyword.NOW);
      }

      if (this.force) {
         args.add((Rawable)Protocol.Keyword.FORCE);
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ShutdownParams that = (ShutdownParams)o;
         return this.now == that.now && this.force == that.force && this.saveMode == that.saveMode;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.saveMode, this.now, this.force);
   }
}
