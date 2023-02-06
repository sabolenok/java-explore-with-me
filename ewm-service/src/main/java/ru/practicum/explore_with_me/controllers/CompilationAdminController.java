package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.compilation.Compilation;
import ru.practicum.explore_with_me.compilation.CompilationService;
import ru.practicum.explore_with_me.compilation.dto.NewCompilationDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompilationAdminController {

    public final CompilationService compilationService;

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public Compilation add(@RequestBody NewCompilationDto newCompilationDto) {
        return compilationService.create(newCompilationDto);
    }
}
