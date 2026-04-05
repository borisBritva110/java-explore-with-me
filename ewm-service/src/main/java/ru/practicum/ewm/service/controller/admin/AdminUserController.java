package ru.practicum.ewm.service.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.dto.NewUserRequest;
import ru.practicum.ewm.service.dto.UserDto;
import ru.practicum.ewm.service.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        List<UserDto> users = userService.getUsersDto(ids, from, size);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Регистрация нового пользователя: {}", newUserRequest);
        UserDto userDto = userService.addUser(newUserRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        log.info("Удаление пользователя с id={}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}