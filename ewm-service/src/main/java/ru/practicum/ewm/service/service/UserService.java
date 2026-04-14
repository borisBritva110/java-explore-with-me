package ru.practicum.ewm.service.service;

import ru.practicum.ewm.service.dto.NewUserRequest;
import ru.practicum.ewm.service.dto.UserDto;
import ru.practicum.ewm.service.model.User;

import java.util.List;

public interface UserService {

    List<User> getUsers(List<Long> ids, int from, int size);

    List<UserDto> getUsersDto(List<Long> ids, int from, int size);

    User getUserById(Long userId);

    UserDto addUser(NewUserRequest newUserRequest);

    void deleteUser(Long userId);

}