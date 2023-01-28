package ru.practicum.explore_with_me.stats;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.stats.dto.StatDto;
import ru.practicum.explore_with_me.stats.dto.StatWithCount;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@Slf4j
public class StatsService {

    @Autowired
    @Getter
    @Setter
    private StatsRepository repository;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public List<Stat> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StatWithCount> getStats(String start, String end, List<String> uris, boolean unique) {
        List<Stat> foundStats = new ArrayList<>();
        List<StatWithCount> stats = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.parse(start, formatter);
        LocalDateTime endDate = LocalDateTime.parse(end, formatter);
        StatWithCount statWithCount = null;
        String previousApp = new String();
        String previousUri = new String();

        if (uris.isEmpty()) {
            foundStats = repository.findByTimestampBetweenOrderByAppAscUriAsc(startDate, endDate);
        } else {
            foundStats = repository.findByTimestampBetweenAndUriInOrderByAppAscUriAsc(startDate, endDate, uris);
        }

        int hits = 0;
        for (Stat stat : foundStats) {
            if (!stat.getApp().equals(previousApp) || !stat.getUri().equals(previousUri)) {
                if (statWithCount != null) {
                    statWithCount.setHits(hits);
                    stats.add(statWithCount);
                }
                statWithCount = new StatWithCount();
                statWithCount.setApp(stat.getApp());
                statWithCount.setUri(stat.getUri());
                previousApp = stat.getApp();
                previousUri = stat.getUri();
                hits = 1;
            } else {
                hits++;
            }
        }
        if (statWithCount != null) {
            statWithCount.setHits(hits);
            stats.add(statWithCount);
        }
        stats.sort((o1, o2) -> o2.getHits().compareTo(o1.getHits()));
        return stats;
    }

    @Transactional
    public Stat create(Stat stat) {
        return repository.save(stat);
    }
}
