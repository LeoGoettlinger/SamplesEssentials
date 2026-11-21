package org.postgresql.hostchooser;

import java.util.Iterator;

public interface HostChooser extends Iterable<CandidateHost> {
   @Override
   Iterator<CandidateHost> iterator();
}
