package WarnDB;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;


public interface WarnEntity extends MongoRepository<WarnCount,String> {
    //guildId와 userId로 경고 횟수 검색
    Optional<WarnCount> findByGuildIdAndUserId(String guildId,String userId);
    List<WarnCount> findAllByGuildIdOrderByWarncntDesc(String guildId);
    //Optional<WarnCount> findbyGuildIdAndUserIdAndmuteFalse(String guildId, String userId, boolean mute);
}
