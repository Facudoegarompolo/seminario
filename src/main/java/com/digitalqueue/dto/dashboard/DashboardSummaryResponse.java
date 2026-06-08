package com.digitalqueue.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {

    private long waiting;
    private String averageWait;
    private long servedToday;
    private long noShows;
    private List<RecentEventResponse> recent;
}
