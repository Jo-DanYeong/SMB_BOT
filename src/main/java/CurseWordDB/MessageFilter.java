package CurseWordDB;

import CurseWordDB.Util.HangulUtils;
import CurseWordDB.Util.TextNormalizer;
import CurseWordDB.database.CurseWord;
import CurseWordDB.database.CurseWordRepo;
import lombok.RequiredArgsConstructor;
import my.bot.BotMain;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import response.Util.EmbedUtil;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MessageFilter extends ListenerAdapter {
    private final CurseWordRepo curseWordRepo;
    private final HangulUtils hangulUtils;
    private final TextNormalizer textNormalizer;

    private static final Pattern HANGUL_PATTERN = Pattern.compile("[\\uAC00-\\uD7AF\\u3131-\\u3163]");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String rawMessage = event.getMessage().getContentRaw().trim();

        // 명령어의 시작 '>'으로 시작하면 하거나 봇의 채팅이면 검사 안하기
        if (rawMessage.startsWith(BotMain.getPREFIX()) || event.getMessage().getAuthor().isBot()) return;

        String content = rawMessage.toLowerCase();
        String guildId = event.getGuild().getId();
        List<CurseWord> bannedList = curseWordRepo.getAllBanned(guildId);

        // 위반 여부 확인
        if (isViolation(content, bannedList)) {
            event.getMessage().delete().queue();
            EmbedUtil embedUtil = new EmbedUtil(event);
            embedUtil.Embed("금지어 감지", "금지어가 포함된 메시지 [" + rawMessage + "]가 감지되었습니다.", Color.red);
        }
    }


    //금지어 위반 여부 판단
    private boolean isViolation(String content, List<CurseWord> bannedList) {
        boolean hasHangul = HANGUL_PATTERN.matcher(content).find();
        String normalizedContent = textNormalizer.normalize(content);

        for (CurseWord bw : bannedList) {
            String targetWord = bw.getWord().toLowerCase();
            String normalizedTarget = textNormalizer.normalize(targetWord);

            if (normalizedContent.contains(normalizedTarget)) return true;

            if (hasHangul) {
                String chosungContent = hangulUtils.getChosung(normalizedContent);
                String chosungTarget = hangulUtils.getChosung(normalizedTarget);
                if (chosungContent.contains(chosungTarget)) return true;
            }
        }
        return false;
    }
}