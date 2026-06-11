package my.web.api;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my.web.discord.DiscordPermissionService;
import my.web.discord.DiscordUserService;
import my.web.dto.GuildUserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/guilds/{guildId}/users")
@RequiredArgsConstructor
public class GuildUserApiController {
    private final DiscordPermissionService discordPermissionService;
    private final DiscordUserService discordUserService;

    @GetMapping
    public List<GuildUserResponse> list(@PathVariable String guildId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        return discordUserService.listMembers(guildId);
    }

    @GetMapping("/search")
    public List<GuildUserResponse> search(
            @PathVariable String guildId,
            @RequestParam String query,
            HttpSession session
    ) {
        discordPermissionService.requireGuildManager(guildId, session);
        return discordUserService.searchMembers(guildId, query);
    }
}
