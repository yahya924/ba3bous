package com.igatn.extranet.rest.historyActivity.service;

import com.igatn.extranet.domainjpa.api.data.demand.DemandRepository;
import com.igatn.extranet.domainjpa.api.data.demand.TypeDemandRepository;
import com.igatn.extranet.domainjpa.impl.domain.demand.Demand;
import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivity;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivityListParams;
import com.igatn.extranet.rest.historyActivity.models.HistoryActivityWsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HistoryActivityService {

    @Autowired
    DemandRepository demandRepository;

    @Autowired
    TypeDemandRepository typeDemandRepository;
    
    @PersistenceContext
    EntityManager entityManager;

    /**
     * Build WS response for get history activities operation
     * 
     * @param params
     * @return
     */
    public HistoryActivityWsResponse search(HistoryActivityListParams params) {

        TypedQuery<Demand> query = getHistoryActivitiesQuery(params);
       
        int totalResults = query.getResultList().size();

        List<Demand> activities = query
            .setFirstResult(params.getIndexFrom())
            .setMaxResults(params.getIndexTo())
            .getResultList();
        
        List<HistoryActivity> historyActivities = activities
            .stream()
            .map(mapSource -> {
                HistoryActivity target = new HistoryActivity();

                BeanUtils.copyProperties(mapSource, target);

                try {
                    String createdAtAsString = DateTimeFormatter
                        .ofPattern("dd/MM/yyyy HH:mm")
                        .withZone(ZoneId.systemDefault())
                        .format(mapSource.getCreatedAt());

                    target.setCreatedAt(createdAtAsString);

                    //WARN: type is NOT nullable
                    target.setType(mapSource.getType().getCode());

                } catch (DateTimeException e) {
                    final String ERR_MSG = "An error has occurred while reading 'createdAt' date of history-activity with ID: ";
                    final String Error = ERR_MSG + mapSource.getId();
                    
                    log.error(Error);
                    // use the next line for Dev purposes
//                    log.error(e.ex);
                    throw new RuntimeException(Error);
                }

                return target;
            })
            .toList();

        HistoryActivityWsResponse wsResponse = HistoryActivityWsResponse.getSuccessInstance(
            historyActivities, totalResults
        );
        
        return wsResponse;
    }

    /**
     * Revert History activities typed query
     * 
     * @param params
     * @return
     */
    private TypedQuery<Demand> getHistoryActivitiesQuery(HistoryActivityListParams params) {
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Demand> demandCriteriaQuery = cb.createQuery(Demand.class);
        Root<Demand> demandRoot = demandCriteriaQuery.from(Demand.class);

        // This helps to combine multiple criteria predicates
        Predicate criteria = cb.conjunction();

        //user
        criteria = cb.and(
            criteria,
            cb.equal(
                demandRoot.get("sender"),
                params.getUser()
            )
        );

        criteria = criteriaWithKeyword(params.getKeyword(), cb, demandRoot, criteria);

        criteria = criteriaWithType(params.getType(), cb, demandRoot, criteria);

        criteria = criteriaWithDates(params, cb, demandRoot, criteria);

        demandCriteriaQuery.orderBy(cb.desc(demandRoot.get("createdAt")));

        TypedQuery<Demand> result = entityManager
            .createQuery(demandCriteriaQuery.where(criteria)
                .orderBy(cb.desc(demandRoot.get("createdAt"))
                )
            );
        
        return result;
    }

    /**
     * Build criteria with dates
     * 
     * @param params
     * @param cb
     * @param demandRoot
     * @param criteria
     * @return
     */
    private Predicate criteriaWithDates(HistoryActivityListParams params, CriteriaBuilder cb, Root<Demand> demandRoot, Predicate criteria) {
       
        boolean hasDateFrom = Optional
            .ofNullable(params.getDateFrom())
            .isPresent();
        
        if (hasDateFrom) {
            
            Instant dateFromDate = LocalDate
                .parse(params.getDateFrom())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

            criteria = cb.and(
                criteria,
                cb.greaterThanOrEqualTo(demandRoot.get("createdAt"), dateFromDate)
            );

        }

        boolean hasDateTo = Optional
            .ofNullable(params.getDateTo())
            .isPresent();
        
        if (hasDateTo) {

            Instant dateToDate = LocalDate
                .parse(params.getDateTo())
                // We add a day here to avoid issues between
                // datepicker which sends date format without time
                // from client-side and dateTime format stored in db
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

            criteria = cb.and(
                criteria,
                cb.lessThanOrEqualTo(demandRoot.get("createdAt"), dateToDate)
            );

        }
        
        return criteria;
    }

    /**
     * build search criteria with keyword
     * 
     * @param keyword
     * @param cb
     * @param demandRoot
     * @param criteria
     * @return
     */
    private Predicate criteriaWithKeyword(String keyword, CriteriaBuilder cb, Root<Demand> demandRoot, Predicate criteria) {
        
        boolean hasKeyword = Optional
            .ofNullable(keyword)
            .isPresent();

        if(hasKeyword){
            final String PATTERN = "%"+keyword+"%";

            criteria = cb.and(
                criteria,
                cb.or(
                    cb.like(demandRoot.get("subject"), PATTERN),
                    cb.like(demandRoot.get("message"), PATTERN)
                )
            );
        }
        
        return criteria;
    }

    /**
     * Build criteria with type
     * 
     * @param type
     * @param cb
     * @param demandRoot
     * @param criteria
     * @return
     */
    private Predicate criteriaWithType(String type, CriteriaBuilder cb, Root<Demand> demandRoot, Predicate criteria) {

        boolean hasType = Optional
            .ofNullable(type)
            .isPresent();

        if (hasType) {
            
            TypeDemand typeDemand = Optional.ofNullable(
                typeDemandRepository.findByCode(type)
            ).orElseThrow(
                () -> new IllegalArgumentException("Now such demand type: '" + type + "'")
            );

            criteria = cb.and(
                criteria,
                cb.equal(demandRoot.get("type"), typeDemand)
            );
        }
        
        return criteria;
    }
    
}
