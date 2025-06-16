package org.example.persistence;
import org.example.domain.Report;
import java.util.List;

/**
 * Interface for managing Report entities in the repository.
 * Extends the generic Repository interface.
 */
public interface IReportRepository extends Repository<Long, Report> {
    /**
     * Retrieves all reports associated with a specific user.
     *
     * @param user the ID of the user
     * @return a list of reports belonging to the specified user
     * @throws RepositoryException if a data access error occurs
     */
    public List<Report> getAllOfUser(Long user);
}
