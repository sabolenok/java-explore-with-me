package ru.practicum.explore_with_me.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.compilation.Compilation;
import ru.practicum.explore_with_me.compilation.CompilationService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompilationPublicController {

    public final CompilationService compilationService;

    @GetMapping("/compilations/{compId}")
    public Compilation get(@PathVariable Integer compId) {
        return compilationService.getById(compId);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/compilations")
    public List<Compilation> getAll(@RequestParam(required = false) Boolean pinned,
                                             @RequestParam(required = false, defaultValue = "0") Integer from,
                                             @RequestParam(required = false, defaultValue = "10") Integer size) {
        return compilationService.getAll(pinned, from, size);
    }
}
