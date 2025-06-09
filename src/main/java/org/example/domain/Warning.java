package org.example.domain;

public class Warning extends Entity<Long>{
    Long report_id;
    String text;
    Double lat;
    Double lng;
    Long created_at;

    public Warning(){}

    public Warning(Long report_id, String text, Double lat, Double lng, Long created_at) {
        this.report_id = report_id;
        this.text = text;
        this.lat = lat;
        this.lng = lng;
        this.created_at = created_at;
    }

    public Long getReport_id() {
        return report_id;
    }

    public void setReport_id(Long report_id) {
        this.report_id = report_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "Warning{" +
                "report_id=" + report_id +
                ", text='" + text + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", created_at=" + created_at +
                '}';
    }
}
