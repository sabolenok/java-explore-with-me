package ru.practicum.explore_with_me.stats;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.dto.Stat;
import ru.practicum.explore_with_me.dto.StatDto;

@Component
public class StatsMapper {
    private static final ModelMapper modelMapper = new ModelMapper();

    public static StatDto toStatDto(Stat stat) {
        return modelMapper.map(stat, StatDto.class);
    }

    public static Stat toStat(StatDto statDto) {
        return modelMapper.map(statDto, Stat.class);
    }
}
