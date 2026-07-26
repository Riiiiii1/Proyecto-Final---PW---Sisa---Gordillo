package ec.edu.ups.icc.proyect.events.dto;

import ec.edu.ups.icc.proyect.events.enums.EventStatus;

public class EventFilterDTO {

    private String title;
    private Long categoryId;
    private EventStatus status;

    public EventFilterDTO() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }
}