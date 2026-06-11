package my.web.dto;

import CurseWordDB.database.CurseWord;

public record CurseWordResponse(String id, String guildId, String word, boolean banned) {
    public static CurseWordResponse from(CurseWord word) {
        return new CurseWordResponse(word.getId(), word.getGuildId(), word.getWord(), word.isBanned());
    }
}
