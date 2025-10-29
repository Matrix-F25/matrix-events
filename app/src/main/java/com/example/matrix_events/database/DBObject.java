package com.example.matrix_events.database;

public abstract class DBObject {
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return this.id.equals(((DBObject) obj).id);
    }
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}