package com.moviebooking.movie_booking.repository.customer.impl;

import com.moviebooking.movie_booking.model.request.search.CinemaSearchRequest;
import com.moviebooking.movie_booking.model.response.CinemaSearchResponse;
import com.moviebooking.movie_booking.repository.CinemaRepository;
import com.moviebooking.movie_booking.repository.customer.CinemaRepositoryCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CinemaRepositoryImpl implements CinemaRepositoryCustomer{
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<Object[]> findAll(CinemaSearchRequest request, Pageable pageable){
        StringBuilder sql =  new StringBuilder("""
                SELECT c.id, c.name, c.address, c.email, ct.name,
                (SELECT COUNT(*) FROM rooms r WHERE r.cinema_id = c.id) as numberOfRoom
                FROM cinemas c
                INNER JOIN cities ct ON ct.id = c.city_id
                """);
        Map<String, Object> params = new HashMap<>();

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if(request.getCityId()!=null){
            where.append(" AND c.city_id = :city_id");
            params.put("city_id",request.getCityId());
        }
        if(request.getName()!=null){
            where.append(" AND c.name LIKE :name");
            params.put("name","%"+request.getName()+"%");
        }
        if (pageable != null && pageable.isPaged()) {
            where.append(" LIMIT :limit OFFSET :offset ");
            params.put("limit", pageable.getPageSize());
            params.put("offset", pageable.getOffset());
        }
        sql.append(where.toString());

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public Long countTotal(CinemaSearchRequest request) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cinemas c");
        Map<String, Object> params = new HashMap<>();
        if(request.getCityId()!=null){
            sql.append(" INNER JOIN cities ct ON ct.id = c.city_id ");
        }

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        if(request.getCityId()!=null){
            where.append(" AND c.city_id = :city_id");
            params.put("city_id",request.getCityId());
        }
        if(request.getName()!=null){
            where.append(" AND c.name LIKE :name");
            params.put("name","%"+request.getName()+"%");
        }
        sql.append(where.toString());

        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }
}
