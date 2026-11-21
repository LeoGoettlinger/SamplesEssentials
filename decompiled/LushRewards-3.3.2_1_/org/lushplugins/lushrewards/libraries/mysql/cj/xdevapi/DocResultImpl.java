package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.function.Supplier;
import org.lushplugins.lushrewards.libraries.mysql.cj.conf.PropertySet;
import org.lushplugins.lushrewards.libraries.mysql.cj.protocol.ProtocolEntity;
import org.lushplugins.lushrewards.libraries.mysql.cj.result.RowList;

public class DocResultImpl extends AbstractDataResult<DbDoc> implements DocResult {
   public DocResultImpl(RowList rows, Supplier<ProtocolEntity> completer, PropertySet pset) {
      super(rows, completer, new DbDocFactory(pset));
   }
}
