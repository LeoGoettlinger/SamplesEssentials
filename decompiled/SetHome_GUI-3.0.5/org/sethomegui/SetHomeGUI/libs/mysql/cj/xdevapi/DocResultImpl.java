package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.function.Supplier;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.conf.PropertySet;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.protocol.ProtocolEntity;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.result.RowList;

public class DocResultImpl extends AbstractDataResult<DbDoc> implements DocResult {
   public DocResultImpl(RowList rows, Supplier<ProtocolEntity> completer, PropertySet pset) {
      super(rows, completer, new DbDocFactory(pset));
   }
}
