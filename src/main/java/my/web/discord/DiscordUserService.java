package my.web.discord;

import my.bot.BotMain;
import my.web.dto.GuildUserResponse;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class DiscordUserService {
    public List<GuildUserResponse> listMembers(String guildId) {
        return getGuild(guildId)
                .map(guild -> loadMembers(guild).stream()
                        .filter(this::isVisibleMember)
                        .sorted(Comparator.comparing(Member::getEffectiveName, String.CASE_INSENSITIVE_ORDER))
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    public List<GuildUserResponse> searchMembers(String guildId, String query) {
        Guild guild = getGuild(guildId).orElse(null);
        if (guild == null || query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return loadMembers(guild).stream()
                .filter(this::isVisibleMember)
                .filter(member -> matches(member, normalizedQuery))
                .sorted(Comparator.comparing(Member::getEffectiveName, String.CASE_INSENSITIVE_ORDER))
                .limit(10)
                .map(this::toResponse)
                .toList();
    }

    public String displayName(String guildId, String userId) {
        return getGuild(guildId)
                .map(guild -> guild.getMemberById(userId))
                .map(Member::getEffectiveName)
                .orElse(userId);
    }

    public boolean isCurrentMember(String guildId, String userId) {
        return getGuild(guildId)
                .map(guild -> guild.getMemberById(userId))
                .filter(this::isVisibleMember)
                .isPresent();
    }

    private boolean matches(Member member, String query) {
        String effectiveName = member.getEffectiveName().toLowerCase(Locale.ROOT);
        String userName = member.getUser().getName().toLowerCase(Locale.ROOT);
        String userId = member.getId();
        return effectiveName.contains(query) || userName.contains(query) || userId.equals(query);
    }

    private List<Member> loadMembers(Guild guild) {
        try {
            return guild.loadMembers().get();
        } catch (RuntimeException exception) {
            return guild.getMembers();
        }
    }

    private boolean isVisibleMember(Member member) {
        String effectiveName = member.getEffectiveName();
        String userName = member.getUser().getName();
        return !member.getUser().isBot()
                && isDeletedAccountName(effectiveName)
                && isDeletedAccountName(userName);
    }

    private boolean isDeletedAccountName(String name) {
        String normalized = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        return !(normalized.isEmpty()
                || normalized.equals("deleted user")
                || normalized.startsWith("deleted_user")
                || normalized.startsWith("deleted-user"));
    }

    private Optional<Guild> getGuild(String guildId) {
        JDA jda = BotMain.getJda();
        if (jda == null) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(jda.getGuildById(guildId));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private GuildUserResponse toResponse(Member member) {
        return new GuildUserResponse(member.getId(), member.getEffectiveName(), member.getUser().getName());
    }
}
