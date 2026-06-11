package my.web.dto;

import WarnDB.WarnCount;

public record WarnResponse(String id, String guildId, String userId, String displayName, int warncnt, boolean mute) {
    public static WarnResponse from(WarnCount warnCount, String displayName) {
        return new WarnResponse(
                warnCount.getId(),
                warnCount.getGuildId(),
                warnCount.getUserId(),
                displayName,
                warnCount.getWarncnt(),
                warnCount.isMute()
        );
    }
}
