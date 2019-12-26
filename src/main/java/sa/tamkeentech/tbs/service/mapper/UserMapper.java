package sa.tamkeentech.tbs.service.mapper;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import sa.tamkeentech.tbs.domain.*;
import sa.tamkeentech.tbs.repository.ClientRepository;
import sa.tamkeentech.tbs.repository.RoleRepository;
import sa.tamkeentech.tbs.service.dto.ClientRoleDTO;
import sa.tamkeentech.tbs.service.dto.UserDTO;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link User} and its DTO called {@link UserDTO}.
 *
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 */
@Service
public class UserMapper {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ClientRepository clientRepository;

    public List<UserDTO> usersToUserDTOs(List<User> users) {
        return users.stream()
            .filter(Objects::nonNull)
            .map(this::userToUserDTO)
            .collect(Collectors.toList());
    }

    public UserDTO userToUserDTO(User user) {
        return new UserDTO(user);
    }

    public List<User> userDTOsToUsers(List<UserDTO> userDTOs) {
        return userDTOs.stream()
            .filter(Objects::nonNull)
            .map(this::userDTOToUser)
            .collect(Collectors.toList());
    }

    public User userDTOToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        } else {
            User user = new User();
            user.setId(userDTO.getId());
            user.setLogin(userDTO.getLogin());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setImageUrl(userDTO.getImageUrl());
            user.setActivated(userDTO.isActivated());
            user.setLangKey(userDTO.getLangKey());
            Set<Authority> authorities = this.authoritiesFromStrings(userDTO.getAuthorities());
            // user.setAuthorities(authorities);
            Set<UserRole> managedRoles = user.getUserRoles();
            managedRoles.clear();
            managedRoles = userRoleFromDTO(userDTO.getClientRoles(), user);
            user.setUserRoles(managedRoles);
            return user;
        }
    }


    private Set<Authority> authoritiesFromStrings(Set<String> authoritiesAsString) {
        Set<Authority> authorities = new HashSet<>();

        if(authoritiesAsString != null){
            authorities = authoritiesAsString.stream().map(string -> {
                Authority auth = new Authority();
                auth.setName(string);
                return auth;
            }).collect(Collectors.toSet());
        }

        return authorities;
    }

    public User userFromId(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }

    public Set<UserRole> userRoleFromDTO(Set<ClientRoleDTO> clientRoleDTOS, User user) {
        Set<UserRole> managedRoles = null;
        if (CollectionUtils.isNotEmpty(clientRoleDTOS)) {
            clientRoleDTOS.forEach(clientRole -> {
                    Optional<Role> role = roleRepository.findById(clientRole.getRole().getId());
                    Optional<Client> client = clientRepository.findById(clientRole.getClient().getId());
                    if (role.isPresent() && client.isPresent()) {
                        managedRoles.add(UserRole.builder().Role(role.get()).client(client.get()).user(user).activated(true).build());
                    }
                });
        }
        return managedRoles;
    }
}
