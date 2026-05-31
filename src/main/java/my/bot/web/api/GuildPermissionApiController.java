package my.bot.web.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my.bot.web.discord.DiscordPermissionService;
import my.bot.web.dto.GuildPermissionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guilds/{guildId}/permissions")
@RequiredArgsConstructor
public class GuildPermissionApiController {
    private final DiscordPermissionService discordPermissionService;

    @GetMapping("/me")
    public GuildPermissionResponse me(@PathVariable String guildId, HttpSession session) {
        return new GuildPermissionResponse(discordPermissionService.canManageGuild(guildId, session));
    }
}
