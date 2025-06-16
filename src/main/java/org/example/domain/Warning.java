package org.example.domain;

/**
 * Represents a warning associated with a specific report.
 * Contains descriptive text and geolocation data.
 */
public class Warning extends Entity<Long>{
    Long report_id; // ID of the associated report
    String text; // Description of the warning
    Double lat; // Latitude of the warning location
    Double lng; // Longitude of the warning location
    Long created_at; // Timestamp when the warning was recorded (ms since epoch)

    /**
     * Default constructor.
     */
    public Warning(){}

    /**
     * Constructs a warning with the specified report ID, text, location, and timestamp.
     *
     * @param report_id   the ID of the associated report
     * @param text        description of the warning
     * @param lat         latitude of the warning
     * @param lng         longitude of the warning
     * @param created_at  timestamp in milliseconds
     */
    public Warning(Long report_id, String text, Double lat, Double lng, Long created_at) {
        this.report_id = report_id;
        this.text = text;
        this.lat = lat;
        this.lng = lng;
        this.created_at = created_at;
    }

    // Standard getters/setters for each field

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

    /**
     * Returns a string representation of the warning.
     *
     * @return a formatted string with warning details
     */
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
