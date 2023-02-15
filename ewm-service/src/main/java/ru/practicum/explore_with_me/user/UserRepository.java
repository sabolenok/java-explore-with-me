package ru.practicum.explore_with_me.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Integer> {
    Page<User> findAllByOrderById(Pageable pageable);

    Page<User> findAllByIdInOrderById(Collection<Integer> id, Pageable pageable);
}
