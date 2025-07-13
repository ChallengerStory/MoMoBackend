package challengers.momochat.chat.command.domain.repository;

import challengers.momochat.chat.command.domain.aggregate.entity.ChatMessage;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommandChatMessageRepository extends MongoRepository<ChatMessage, ObjectId> {

}
