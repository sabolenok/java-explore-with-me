package ru.practicum.explore_with_me.compilation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CompilationRepository extends JpaRepository<Compilation, Integer>, JpaSpecificationExecutor<Compilation> {
}
