package WarnDB;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarnRepo {

    private final WarnEntity repo;

    // 경고 1회 추가
    public WarnCount addWarn(String guildId, String userId) {
        WarnCount warnCount = repo.findByGuildIdAndUserId(guildId, userId)
                .orElse(new WarnCount(guildId, userId, 0, false));
        warnCount.increaseWarn();
        return repo.save(warnCount);
    }

    // 경고 회수
    public WarnCount subWarn(String guildId, String userId) {
        WarnCount warnCount = repo.findByGuildIdAndUserId(guildId,userId)
                .orElse(new WarnCount(guildId,userId,0,false));
        warnCount.decreaseWarn();
        return repo.save(warnCount);
    }

    // 경고 정보 조회
    public int getWarn(String guildId, String userId){
        WarnCount warnCount = repo.findByGuildIdAndUserId(guildId,userId)
                .orElse(new WarnCount(guildId,userId,0,false));
        return warnCount.getWarncnt();
    }

    //뮤트 유뮤 조회
    public boolean getMute(String guildId, String userId){
        WarnCount warnCount = repo.findByGuildIdAndUserId(guildId,userId)
                .orElse(new WarnCount(guildId,userId,0,false));
        return warnCount.isMute();
    }

    // 경고 초기화
    public void resetWarn(String guildId, String userId) {
        repo.findByGuildIdAndUserId(guildId, userId)
                .ifPresent(wc -> {
                    wc.setWarncnt(0);
                    wc.setMute(false);
                    repo.save(wc);
                });
    }

    // 뮤트 플래그 설정
    public void setMuted(String guildId, String userId, boolean muted) {
        WarnCount warnCount = repo.findByGuildIdAndUserId(guildId, userId)
                .orElse(new WarnCount(guildId, userId, 0, muted));
        warnCount.setMute(muted);
        repo.save(warnCount);
    }
}

