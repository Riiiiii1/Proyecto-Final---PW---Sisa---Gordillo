package ec.edu.ups.icc.proyect.sessions.dto;

import java.time.OffsetDateTime;

public class SessionResponseDTO {

    private Long id;
    private String title;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String location;
    private Long eventId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public OffsetDateTime getStartAt() { return startAt; }
    public void setStartAt(OffsetDateTime startAt) { this.startAt = startAt; }

    public OffsetDateTime getEndAt() { return endAt; }
    public void setEndAt(OffsetDateTime endAt) { this.endAt = endAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}