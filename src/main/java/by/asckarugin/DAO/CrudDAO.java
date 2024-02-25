package by.asckarugin.DAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CrudDAO<T> {
    Optional<T> findById(Long id) throws SQLException;

    List<T> findAll() throws SQLException;

    T save(T entity) throws SQLException;

    void update(T entity) throws SQLException;
}
