package CurseWord;

import CurseWord.Normalizer.HangulUtils;
import CurseWord.Normalizer.TextNormalizer;
import CurseWord.database.CurseWord;
import CurseWord.database.CurseWordRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import response.EmbedUtil;
import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MessageFilter extends ListenerAdapter {
    private final CurseWordRepo curseWordRepo;
    private final HangulUtils hangulUtils;
    private final TextNormalizer textNormalizer;

    // 한글 포함 여부 확인 패턴
    private static final Pattern HANGUL_PATTERN = Pattern.compile("[\\uAC00-\\uD7AF\\u3131-\\u3163]");

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // 멤버 변수 대신 지역 변수 사용
        String rawMessage = event.getMessage().getContentRaw().trim();

        // 관리자 명령어 예외 처리T
        if (rawMessage.startsWith(">") &&
                event.getMember() != null &&
                event.getMember().hasPermission(Permission.ADMINISTRATOR)) return;

        String content = rawMessage.toLowerCase();
        String guildId = event.getGuild().getId();
        List<CurseWord> bannedList = curseWordRepo.getAllBanned(guildId);

        // 위반 여부 확인
        if (isViolation(content, bannedList)) {
            executeDelete(event, rawMessage);
        }
    }


    //금지어 위반 여부 판단
    private boolean isViolation(String content, List<CurseWord> bannedList) {
        boolean hasHangul = HANGUL_PATTERN.matcher(content).find();
        String normalizedContent = textNormalizer.normalize(content);

        for (CurseWord bw : bannedList) {
            String target = bw.getWord().toLowerCase();
            String normalizedTarget = textNormalizer.normalize(target);

            // 1단계: 일반 텍스트 매칭
            if (normalizedContent.contains(normalizedTarget)) return true;

            // 2단계: 초성 대조 정밀 검사 (한글 포함 시)
            if (hasHangul) {
                String chosungContent = hangulUtils.getChosung(normalizedContent);
                String chosungTarget = hangulUtils.getChosung(normalizedTarget);
                if (chosungContent.contains(chosungTarget)) return true;
            }
        }
        return false;
    }

    //금지어 감지 메세지
    private void executeDelete(MessageReceivedEvent event, String detectedMessage) {
        event.getMessage().delete().queue();
        EmbedUtil embedUtil = new EmbedUtil(event);
        embedUtil.Embed("금지어 감지", Color.red, "금지어가 포함된 메시지 [" + detectedMessage + "]가 감지되었습니다.");
    }
}