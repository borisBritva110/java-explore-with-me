package ru.practicum.ewm.service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.service.dto.NewUserRequest;
import ru.practicum.ewm.service.dto.UserDto;
import ru.practicum.ewm.service.exception.NotFoundException;
import ru.practicum.ewm.service.mapper.UserMapper;
import ru.practicum.stats.dto.NotFound;
import ru.practicum.ewm.service.model.User;
import ru.practicum.ewm.service.repository.UserRepository;
import ru.practicum.ewm.service.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> getUsers(List<Long> ids, int from, int size) {
        log.info("Получаем пользователей с ids: {}, from: {}, size: {}", ids, from, size);

        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size, Sort.by("id").ascending());
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIds(ids, pageable).getContent();
        } else {
            users = userRepository.findAll(pageable).getContent();
        }
        log.info("Получили пользователей: {}", users);
        return users;
    }

    @Override
    public List<UserDto> getUsersDto(List<Long> ids, int from, int size) {
        log.info("Получаем DTO пользователей с ids: {}", ids);
        return getUsers(ids, from, size).stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto addUser(NewUserRequest newUserRequest) {
        log.info("Добавляем нового пользователя: {}", newUserRequest);
        User newUser = userRepository.save(UserMapper.toEntity(newUserRequest));
        log.info("Новый пользователь добавлен: {}", newUser);
        return UserMapper.toDto(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаляем пользователя id {}", userId);
        userRepository.deleteById(userId);
        log.info("Пользователь id {} удален", userId);
    }

    @Override
    public User getUserById(Long userId) {
        log.info("Получаем сущность пользователя id {}", userId);
        return getUserOrThrow(userId);
    }

    private User getUserOrThrow(Long userId) {
        log.info("Получаем либо ошибку, либо сущность пользователя id {}", userId);
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException(
                String.format(NotFound.USER, userId)));
    }
}