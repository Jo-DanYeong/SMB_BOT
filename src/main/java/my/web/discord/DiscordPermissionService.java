package my.web.discord;

import jakarta.servlet.http.HttpSession;
import my.bot.BotMain;
import my.web.auth.AdminSession;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DiscordPermissionService {
    public boolean canManageGuild(String guildId, HttpSession session) {
        String userId = getSessionUserId(session);
        if (userId == null) {
            return false;
        }

        JDA jda = BotMain.getJda();
        if (jda == null) {
            return false;
        }

        Guild guild;
        try {
            guild = jda.getGuildById(guildId);
        } catch (NumberFormatException exception) {
            return false;
        }

        if (guild == null) {
            return false;
        }

        try {
            Member member = guild.retrieveMemberById(userId).complete();
            return member != null && member.hasPermission(
                    Permission.ADMINISTRATOR,
                    Permission.MANAGE_SERVER,
                    Permission.MODERATE_MEMBERS,
                    Permission.MANAGE_ROLES,
                    Permission.MESSAGE_MANAGE,
                    Permission.BAN_MEMBERS
            );
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public void requireGuildManager(String guildId, HttpSession session) {
        if (!canManageGuild(guildId, session)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "server permission required");
        }
    }

    private String getSessionUserId(HttpSession session) {
        Object userId = session.getAttribute(AdminSession.USER_ID);
        return userId instanceof String value ? value : null;
    }
}
