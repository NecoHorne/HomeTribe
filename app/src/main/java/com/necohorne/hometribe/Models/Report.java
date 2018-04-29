package com.necohorne.hometribe.Models;

public class Report {

    private String keyRef;
    private String reportedBy;
    private String reasonForReport;
    private String reportDate;

    public Report(String keyRef, String reportedBy, String reasonForReport, String reportDate) {
        this.keyRef = keyRef;
        this.reportedBy = reportedBy;
        this.reasonForReport = reasonForReport;
        this.reportDate = reportDate;
    }

    public String getKeyRef() {
        return keyRef;
    }

    public void setKeyRef(String keyRef) {
        this.keyRef = keyRef;
    }

    public String getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy) {
        this.reportedBy = reportedBy;
    }

    public String getReasonForReport() {
        return reasonForReport;
    }

    public void setReasonForReport(String reasonForReport) {
        this.reasonForReport = reasonForReport;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }
}
