package ru.practicum.explore_with_me.user;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.user.dto.UserDto;

@Component
public class UserMapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static UserDto toUserDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    public static User toUser(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
