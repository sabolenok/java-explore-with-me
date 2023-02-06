package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.compilation.Compilation;
import ru.practicum.explore_with_me.compilation.CompilationService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompilationPublicController {

    public final CompilationService compilationService;

    @GetMapping("/compilations/{compId}")
    public Compilation get(@PathVariable Integer compId) {
        return compilationService.getById(compId);
    }
}
