package ng.sciCloud.library.repository;

import jakarta.persistence.LockModeType;
import ng.sciCloud.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Book b where b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select b from Book b
            where (:category = '' or b.category = :category)
              and (:q = ''
                   or lower(b.title) like lower(concat('%', :q, '%'))
                   or lower(b.author) like lower(concat('%', :q, '%')))
            order by b.title
            """)
    List<Book> search(@Param("q") String q, @Param("category") String category);

    @Query("select distinct b.category from Book b order by b.category")
    List<String> findAllCategories();
}
