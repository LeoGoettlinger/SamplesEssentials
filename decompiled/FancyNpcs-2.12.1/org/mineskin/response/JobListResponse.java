package org.mineskin.response;

import java.util.List;
import org.mineskin.data.JobInfo;

public interface JobListResponse extends MineSkinResponse<List<JobInfo>> {
   List<JobInfo> getJobs();
}
