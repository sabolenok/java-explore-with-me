package ru.practicum.explore_with_me.stats;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.stats.dto.StatDto;
import ru.practicum.explore_with_me.stats.dto.StatWithCount;

import java.util.List;
import java.util.stream.Collectors;

@RestController
//@RequestMapping()
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    //private final StatsClient statsClient;
    private final StatsService statsService;

    @PostMapping("/hit")
    //public ResponseEntity<Object> addStat(@RequestBody StatDto statDto) {
    public StatDto addStat(@RequestBody Stat stat) {
        log.info("Creating stat {}", stat);
        return StatsMapper.toStatDto(statsService.create(stat));
    }

    @GetMapping("/stats")
    //public ResponseEntity<Object> getStats() {
    public List<StatWithCount> getStats(@RequestParam String start,
                                        @RequestParam String end,
                                        @RequestParam(required = false) List<String> uris,
                                        @RequestParam(required = false, defaultValue = "false") boolean unique) {
        log.info("Get all stats");
        return statsService.getStats(start, end, uris, unique);
    }
}
