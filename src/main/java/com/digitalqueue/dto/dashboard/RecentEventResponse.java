package com.digitalqueue.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecentEventResponse {

    private String id;
    private Integer number;
    private String status;
    private String time;
    private String date;
    private String dateTime;
}
