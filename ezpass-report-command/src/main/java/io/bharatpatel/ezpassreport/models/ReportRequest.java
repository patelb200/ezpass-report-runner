package io.bharatpatel.ezpassreport.models;

import java.time.LocalDate;

public record ReportRequest(LocalDate startDate, LocalDate endDate) {
}
