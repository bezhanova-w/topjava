package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;
import ru.javawebinar.topjava.util.ValidationUtil;

import javax.validation.constraints.NotNull;
import java.util.*;

@Repository
@Transactional(readOnly = true)
public class JdbcUserRepository implements UserRepository {

    private static final BeanPropertyRowMapper<User> ROW_MAPPER = BeanPropertyRowMapper.newInstance(User.class);
    private static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> Role.valueOf(rs.getString("role"));

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert insertUser;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        ValidationUtil.validate(user);

        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());

            //роли
            if (!CollectionUtils.isEmpty(user.getRoles())) {
                jdbcTemplate.batchUpdate("INSERT INTO user_roles (user_id, role) VALUES(?, ?)",
                        user.getRoles(),
                        user.getRoles().size(),
                        (ps, role) -> {
                            ps.setInt(1, user.getId());
                            ps.setString(2, role.name());
                        });
            }
        } else if (namedParameterJdbcTemplate.update(
                "UPDATE users SET name=:name, email=:email, password=:password, " +
                        "registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id", parameterSource) != 0) {

            //роли
            Set<Role> rolesFromDatabase = EnumSet.copyOf(getRoles(user));
            Set<Role> rolesFromDatabaseCopy = EnumSet.copyOf(rolesFromDatabase);
            Set<Role> rolesFromUser = user.getRoles() != null ? EnumSet.copyOf(user.getRoles()) : EnumSet.noneOf(Role.class);

            //роли к удалению из базы = которые сейчас в базе, но нет в юзере
            rolesFromDatabase.removeAll(rolesFromUser);
            deleteRoles(user, rolesFromDatabase);

            //роли к добавлению в базу = которые есть в юзере, но нет в базе
            rolesFromUser.removeAll(rolesFromDatabaseCopy);
            insertRoles(user, rolesFromUser);
        }
        else {
            return null;
        }
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE id=?", ROW_MAPPER, id);
        User user = DataAccessUtils.singleResult(users);
        if (user != null) {
            user.setRoles(getRoles(user));
        }
        return user;
    }

    @Override
    public User getByEmail(String email) {
//        return jdbcTemplate.queryForObject("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        List<User> users = jdbcTemplate.query("SELECT * FROM users WHERE email=?", ROW_MAPPER, email);
        User user = DataAccessUtils.singleResult(users);
        if (user != null) {
            user.setRoles(getRoles(user));
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        //все роли -> Map(userId, список ролей)
        Map<Integer, List<Role>> roles = new HashMap<>();
        jdbcTemplate.query("SELECT * FROM user_roles ORDER BY user_id, role", (rs, rowNum) -> {
            Role role = ROLE_ROW_MAPPER.mapRow(rs, rowNum);
            roles.computeIfAbsent(rs.getInt("user_id"), (user_id) -> new ArrayList<Role>()).add(role);
            return role;
        });

        //все пользователи
        return jdbcTemplate.query("SELECT * FROM users ORDER BY name, email", (rs, rowNum) -> {
            User user = ROW_MAPPER.mapRow(rs, rowNum);
            if (user != null) {
                user.setRoles(roles.get(user.getId()));
            }
            return user;
        });
    }

    private List<Role> getRoles(@NotNull User user) {
        return jdbcTemplate.query("SELECT DISTINCT * FROM user_roles WHERE user_id=?", ROLE_ROW_MAPPER, user.getId());
    }

    private void insertRoles(User user, Set<Role> roles) {
        if (!CollectionUtils.isEmpty(roles)) {
            jdbcTemplate.batchUpdate("INSERT INTO user_roles (user_id, role) VALUES(?, ?)",
                    roles,
                    roles.size(),
                    (ps, role) -> {
                        ps.setInt(1, user.getId());
                        ps.setString(2, role.name());
                    });
        }
    }

    private void deleteRoles(User user, Set<Role> roles) {
        if (!CollectionUtils.isEmpty(roles)) {
            jdbcTemplate.batchUpdate("DELETE FROM user_roles WHERE user_id=? AND role=?",
                    roles,
                    roles.size(),
                    (ps, role) -> {
                        ps.setInt(1, user.getId());
                        ps.setString(2, role.name());
                    });
        }
    }
}
