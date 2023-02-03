package ru.practicum.explore_with_me.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explore_with_me.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class UserController {

    public final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto add(@RequestBody UserDto userDto) {
        return UserMapper.toUserDto(userService.create(UserMapper.toUser(userDto)));
    }

    @GetMapping
    public List<UserDto> getAll(@RequestParam(required = false) Integer[] ids,
                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                @RequestParam(required = false, defaultValue = "100") Integer size) {
        return userService.findAll(ids, from, size)
                .stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }

}
