package com.moviebooking.movie_booking.repository.customer.impl;


import com.moviebooking.movie_booking.model.dto.RoleDTO;
import com.moviebooking.movie_booking.model.dto.UserDTO;
import com.moviebooking.movie_booking.model.request.search.UserSearchRequest;
import com.moviebooking.movie_booking.repository.customer.UserRepositoryCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;

import java.util.*;

public class UserRepositoryImpl implements UserRepositoryCustomer {
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<UserDTO> findAll(UserSearchRequest request, Pageable pageable) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                        u.id,
                        u.fullname,
                        u.username,
                        u.email,
                        u.phone,
                        u.points,
                        r.name
                    FROM users u
                    LEFT JOIN user_role ur ON ur.user_id = u.id
                    LEFT JOIN roles r ON r.id = ur.role_id
    """);
        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if(request.getKeyword()!=null &&  !request.getKeyword().trim().isEmpty()){
            where.append("""
                            AND (u.fullname LIKE :keyword
                            OR u.username LIKE :keyword
                            OR u.email LIKE :keyword)
                             """ );
            params.put("keyword","%"+request.getKeyword()+"%");
        }
        if (request.getRoleName() != null  && !request.getRoleName().trim().isEmpty()) {
            where.append(" AND r.name = :roleName");
            params.put("roleName", request.getRoleName());
        }
        if (pageable != null && pageable.isPaged()) {
            where.append(" LIMIT :limit OFFSET :offset ");
            params.put("limit", pageable.getPageSize());
            params.put("offset", pageable.getOffset());
        }
        sql.append(where.toString());
        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        List<Object[]> rows = query.getResultList();
        Map<Long, UserDTO> userMap = new LinkedHashMap<>();
        for (Object[] row : rows) {

            Long userId = ((Number) row[0]).longValue();

            UserDTO user = userMap.get(userId);
            if (user == null) {
                user = new UserDTO();
                user.setId(userId);
                user.setFullname((String) row[1]);
                user.setUsername((String) row[2]);
                user.setEmail((String) row[3]);
                user.setPhone((String) row[4]);
                user.setPoints(((Number) row[5]).intValue());
                user.setRoles(new ArrayList<>());
                userMap.put(userId, user);
            }

            if (row[6] != null) {
                RoleDTO role = new RoleDTO();
                role.setName((String) row[6]);

                user.getRoles().add(role);
            }
        }

        return new ArrayList<>(userMap.values());
    }

    @Override
    public Long countTotal(UserSearchRequest request) {
        // Sửa COUNT(*) thành COUNT(DISTINCT u.id) để tránh trùng lặp khi user có nhiều role
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(DISTINCT u.id)
                    FROM users u
                    LEFT JOIN user_role ur ON ur.user_id = u.id
                    LEFT JOIN roles r ON r.id = ur.role_id
        """);

        Map<String, Object> params = new HashMap<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            where.append("""
                            AND (u.fullname LIKE :keyword
                            OR u.username LIKE :keyword
                            OR u.email LIKE :keyword)
                             """);
            params.put("keyword", "%" + request.getKeyword().trim() + "%");
        }

        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            where.append(" AND r.name = :roleName");
            params.put("roleName", request.getRoleName().trim());
        }

        sql.append(where.toString());
        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);

        return ((Number) query.getSingleResult()).longValue();
    }
}
