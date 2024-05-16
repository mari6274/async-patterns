package dev.maczkowski.jugpoznan.asyncpatterns.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {

    @Query("""
            select o from OutboxEntity o
            where o.status = 'NEW'
            and o.type = :type
            order by o.creationDate asc
            limit 1000
            """)
    List<OutboxEntity> findOutboxEntities(Type type);


    @Transactional
    @Modifying
    @Query("update OutboxEntity o set o.status = :status where o.id in :ids")
    void updateSetStatusWhereIdIn(List<Long> ids, Status status);
}
