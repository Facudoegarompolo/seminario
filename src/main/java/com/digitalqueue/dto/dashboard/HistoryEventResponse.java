package com.digitalqueue.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryEventResponse {

    private String id;
    private String time;
    private String date;
    private String dateTime;
    private String text;
}
