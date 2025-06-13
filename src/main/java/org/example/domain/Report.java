package org.example.domain;

import java.util.List;

public class Report extends Entity<Long>{
    Long user_id;
    Double start_lat;
    Double start_lng;
    Double end_lat;
    Double end_lng;
    Long created_at;
    List<Warning> warnings;

    public Report(){}

    public Report(Long user_id, Double start_lat, Double start_lng, Double end_lat, Double end_lng, Long created_at) {
        this.user_id = user_id;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.end_lat = end_lat;
        this.end_lng = end_lng;
        this.created_at = created_at;
    }

    public Report(Long user_id, Double start_lat, Double start_lng, Double end_lat, Double end_lng) {
        this.user_id = user_id;
        this.start_lat = start_lat;
        this.start_lng = start_lng;
        this.end_lat = end_lat;
        this.end_lng = end_lng;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Double getStart_lat() {
        return start_lat;
    }

    public void setStart_lat(Double start_lat) {
        this.start_lat = start_lat;
    }

    public Double getStart_lng() {
        return start_lng;
    }

    public void setStart_lng(Double start_lng) {
        this.start_lng = start_lng;
    }

    public Double getEnd_lat() {
        return end_lat;
    }

    public void setEnd_lat(Double end_lat) {
        this.end_lat = end_lat;
    }

    public Double getEnd_lng() {
        return end_lng;
    }

    public void setEnd_lng(Double end_lng) {
        this.end_lng = end_lng;
    }

    public Long getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Long created_at) {
        this.created_at = created_at;
    }

    public void setWarnings(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public List<Warning> getWarnings() {
        return warnings;
    }

    public void addWarning(Warning warning) {
        warnings.add(warning);
    }

    @Override
    public String toString() {
        return "Report{" +
                "user_id=" + user_id +
                ", start_lat=" + start_lat +
                ", start_lng=" + start_lng +
                ", end_lat=" + end_lat +
                ", end_lng=" + end_lng +
                ", created_at=" + created_at +
                ", warnings=" + warnings +
                '}';
    }
}
