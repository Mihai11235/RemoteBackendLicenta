package org.example.domain;
import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class for all domain entities with an ID field.
 *
 * @param <ID> The type of the identifier used for the entity (e.g., Long, String).
 */
public class Entity<ID> implements Serializable {
    protected ID id;

    /**
     * Returns the identifier of the entity.
     *
     * @return The ID of the entity.
     */
    public ID getId() {
        return id;
    }

    /**
     * Sets the identifier of the entity.
     *
     * @param id The ID to set for the entity.
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Checks equality based on the ID field.
     *
     * @param o The object to compare.
     * @return True if the other object is an Entity with the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity)) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * Computes the hash code based on the ID.
     *
     * @return The hash code of the entity.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Returns a string representation of the entity.
     *
     * @return A string containing the class name and ID.
     */
    @Override
    public String toString() {
        return "Entity{" +
                "id=" + id +
                '}';
    }
}