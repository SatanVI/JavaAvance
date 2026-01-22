
package projetjava.repositories;
import java.util.List;

public interface IRepository<T> {
    void save(T item);
    T findById(Long id);
    List<T> findAll();
    void delete(Long id);
}