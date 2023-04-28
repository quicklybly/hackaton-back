package ru.cpf.back.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.cpf.back.dto.SportsmanDto;
import ru.cpf.back.dto.UserDto;
import ru.cpf.back.entity.AppUser;
import ru.cpf.back.exception.AppException;
import ru.cpf.back.mapper.UserMapper;
import ru.cpf.back.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDto getUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(AppException.CODE.USER_NOT_FOUND::get);
        return getSpecificUserDtoByBasicEntity(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::userEntityToDto)
                .collect(Collectors.toList());
    }

    public UserDto getProfile(AppUser user) {
        return getSpecificUserDtoByBasicEntity(user);
    }

    private UserDto getSpecificUserDtoByBasicEntity(AppUser user) {
        var id = user.getId();
        switch (user.getRole().getName()) {
            case ROLE_SPORTSMAN -> {
                var sportsman = userRepository.findSportsmenById(id)
                        .orElseThrow(AppException.CODE.USER_NOT_FOUND::get);
                return userMapper.sportsmanEntityToDto(sportsman);
            }
            case ROLE_ADMIN, ROLE_PARTNER, ROLE_REGIONAL_REPRESENTATIVE -> {
                return userMapper.userEntityToDto(user);
            }
        }
        throw AppException.CODE.USER_NOT_FOUND.get();
    }

    public SportsmanDto editProfileSportsman(AppUser user, SportsmanDto sportsmanDto) {
        if (user == null) {
            throw AppException.CODE.USER_UNAUTHORIZED.get();
        }
        var sportsman = userMapper.sportsmanDtoToEntity(sportsmanDto);
        sportsman.setOrganization(sportsmanDto.getOrganization());
        sportsman.setId(user.getId());
        sportsman.setRole(user.getRole());
        sportsman.setUsername(user.getUsername());
        sportsman.setEmail(user.getEmail());
        sportsman.setPassword(user.getPassword());
        sportsman = userRepository.save(sportsman);
        return userMapper.sportsmanEntityToDto(sportsman);
    }
}
