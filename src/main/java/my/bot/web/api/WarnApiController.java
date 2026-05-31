package my.bot.web.api;

import WarnDB.WarnCount;
import WarnDB.WarnEntity;
import WarnDB.WarnRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my.bot.web.discord.DiscordPermissionService;
import my.bot.web.discord.DiscordUserService;
import my.bot.web.dto.MuteRequest;
import my.bot.web.dto.WarnResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/guilds/{guildId}/warnings")
@RequiredArgsConstructor
public class WarnApiController {
    private final WarnRepo warnRepo;
    private final WarnEntity warnEntity;
    private final DiscordPermissionService discordPermissionService;
    private final DiscordUserService discordUserService;

    @GetMapping
    public List<WarnResponse> list(@PathVariable String guildId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        return warnEntity.findAllByGuildIdOrderByWarncntDesc(guildId).stream()
                .filter(warnCount -> discordUserService.isCurrentMember(guildId, warnCount.getUserId()))
                .map(warnCount -> WarnResponse.from(warnCount, discordUserService.displayName(guildId, warnCount.getUserId())))
                .toList();
    }

    @GetMapping("/{userId}")
    public WarnResponse get(@PathVariable String guildId, @PathVariable String userId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        return warnEntity.findByGuildIdAndUserId(guildId, userId)
                .map(warnCount -> WarnResponse.from(warnCount, discordUserService.displayName(guildId, warnCount.getUserId())))
                .orElse(new WarnResponse(null, guildId, userId, discordUserService.displayName(guildId, userId), 0, false));
    }

    @PostMapping("/{userId}/add")
    public WarnResponse add(@PathVariable String guildId, @PathVariable String userId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        WarnCount warnCount = warnRepo.addWarn(guildId, normalizeUserId(userId));
        return WarnResponse.from(warnCount, discordUserService.displayName(guildId, warnCount.getUserId()));
    }

    @PostMapping("/{userId}/remove")
    public WarnResponse remove(@PathVariable String guildId, @PathVariable String userId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        WarnCount warnCount = warnRepo.subWarn(guildId, normalizeUserId(userId));
        return WarnResponse.from(warnCount, discordUserService.displayName(guildId, warnCount.getUserId()));
    }

    @PatchMapping("/{userId}/mute")
    public WarnResponse setMuted(
            @PathVariable String guildId,
            @PathVariable String userId,
            @RequestBody MuteRequest request,
            HttpSession session
    ) {
        discordPermissionService.requireGuildManager(guildId, session);
        String normalizedUserId = normalizeUserId(userId);
        warnRepo.setMuted(guildId, normalizedUserId, request.muted());
        return get(guildId, normalizedUserId, session);
    }

    @DeleteMapping("/{userId}")
    public WarnResponse reset(@PathVariable String guildId, @PathVariable String userId, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        String normalizedUserId = normalizeUserId(userId);
        warnRepo.resetWarn(guildId, normalizedUserId);
        return get(guildId, normalizedUserId, session);
    }

    private String normalizeUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userId.trim();
    }
}
