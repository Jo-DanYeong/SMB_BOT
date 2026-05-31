package my.bot.web.api;

import CurseWordDB.database.CurseWordRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import my.bot.web.discord.DiscordPermissionService;
import my.bot.web.dto.CurseWordResponse;
import my.bot.web.dto.WordRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/guilds/{guildId}/words")
@RequiredArgsConstructor
public class CurseWordApiController {
    private final CurseWordRepo curseWordRepo;
    private final DiscordPermissionService discordPermissionService;

    @GetMapping
    public List<CurseWordResponse> list(@PathVariable String guildId) {
        return curseWordRepo.getAllBanned(guildId).stream()
                .map(CurseWordResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CurseWordResponse add(@PathVariable String guildId, @RequestBody WordRequest request, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        String word = normalizeWord(request.word());
        curseWordRepo.ban(guildId, word);
        return new CurseWordResponse(null, guildId, word, true);
    }

    @DeleteMapping("/{word}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable String guildId, @PathVariable String word, HttpSession session) {
        discordPermissionService.requireGuildManager(guildId, session);
        curseWordRepo.unban(guildId, normalizeWord(word));
    }

    private String normalizeWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "word is required");
        }
        return word.trim();
    }
}
