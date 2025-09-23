package challengers.momochat.infrastructure.outbox.repository;

import challengers.momochat.infrastructure.outbox.entity.Outbox;
import challengers.momochat.infrastructure.outbox.enums.OutboxStatus;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxRepository extends MongoRepository<Outbox, ObjectId> {

    List<Outbox> findByStatusIn(List<OutboxStatus> statuses);
}
