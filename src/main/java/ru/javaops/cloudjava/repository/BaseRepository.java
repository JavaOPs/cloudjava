package ru.javaops.cloudjava.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import static ru.javaops.cloudjava.util.validation.ValidationUtil.checkExisted;
import static ru.javaops.cloudjava.util.validation.ValidationUtil.checkModification;

// https://stackoverflow.com/questions/42781264/multiple-base-repositories-in-spring-data-jpa
@NoRepositoryBean
public interface BaseRepository<T> extends JpaRepository<T, Integer> {

    //    https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query.spel-expressions
    @Transactional
    @Modifying
    @Query("DELETE FROM #{#entityName} e WHERE e.id=:id")
    int delete(int id);

    default void deleteExisted(int id) {
        checkModification(delete(id), id);
    }

    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    T get(int id);

    default T getExisted(int id) {
        return checkExisted(get(id), id);
    }
}