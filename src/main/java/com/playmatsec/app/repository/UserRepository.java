package com.playmatsec.app.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;

import com.playmatsec.app.repository.model.User;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.UserSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.UserConsts;
import io.micrometer.common.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final UserJpaRepository repository;

    public List<User> getUsers() {
        return repository.findAll();
    }

    public User getById(UUID id) {
        return repository.findById(id).orElse(null);
    }

    public User getByEmail(String email) {
        return repository.findByEmail(email);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public void delete(User user) {
        repository.delete(user);
    }

    public List<User> search(String provider, String providerId, String email, String name, String role) {
        UserSearchCriteria spec = new UserSearchCriteria();
        if (StringUtils.isNotBlank(provider)) {
            spec.add(new SearchStatement(UserConsts.PROVIDER, provider, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(providerId)) {
            spec.add(new SearchStatement(UserConsts.PROVIDER_ID, providerId, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(email)) {
            spec.add(new SearchStatement(UserConsts.EMAIL, email, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(name)) {
            spec.add(new SearchStatement(UserConsts.NAME, name, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(role)) {
            spec.add(new SearchStatement(UserConsts.ROLE, role, SearchOperation.EQUAL));
        }
        return repository.findAll(spec);
    }
}
