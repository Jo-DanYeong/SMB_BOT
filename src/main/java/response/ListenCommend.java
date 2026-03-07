package response;
//뮤트 경고문 정리 할것
import CurseWord.database.CurseWordRepo;
import Warn.WarnCount;
import Warn.WarnRepo;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListenCommend extends ListenerAdapter {

    private final CurseWordRepo curseWordRepo;
    private final WarnRepo warnRepo;
    private static final String PREFIX = ">";
    private String comment = "";
    private String command = "";
    private MessageReceivedEvent messageReceivedEvent;
    private final String PREFIX = BotMain.getPREFIX();
    EmbedUtil embedUtil;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().trim();
        embedUtil = new EmbedUtil(event);
        // 접두사로 시작하지 않으면 무시
        if (!message.startsWith(PREFIX)) {
            return;
        }

        // ">" 만 입력했을 때 도움말 출력
        if (message.equals(PREFIX)) {
            readhelp(event);
            return;
        }

        String[] args = message.substring(PREFIX.length()).trim().split("\\s+");
        command = args[0].toLowerCase();
        messageReceivedEvent = event;

        //커맨드 처리
        if(ChackOp()){
            switch (command) {
                case "h":
                case "help":
                case "도움말":
                    readhelp(event);
                    break;

                case "f":
                case"filter":
                case "금지어":
                    CurseWordCommand(event,args);
                    break;

                case "w":
                case "경고":
                    WarnCommand(event,args,"add");
                    break;

                case "rw":
                case"경고 취소":
                case "경고회수":
                    WarnCommand(event,args,"sub");
                    break;

                case "m":
                case "mute":
                case "뮤트":
                    MuteCommand(event,args,true);
                    break;

                case "um":
                case "언뮤트":
                    MuteCommand(event,args,false);
                    break;

                case "c":
                case "clear":
                case "클린":
                    CleanCommand(event,args);

                    break;

                default:
                    embedUtil.Embed("알 수 없는 명령어",Color.RED,command + "(은)는 알 수 없습니다.\n" +
                            "도움말은 >help를 사용하여 확인하실 수 있습니다.",true,5);
            }
        }
    }

    private boolean ChackOp() {
        /*만약에 관리자 권한이 있으면 true 반환
        * 관리자 권한이 없으면 false 반환*/
        boolean hasAdmin = messageReceivedEvent.getMember() != null && messageReceivedEvent.getMember().hasPermission(Permission.ADMINISTRATOR);
        if (!hasAdmin) {
            String getHighestPerm = HighestPerm.GetHighestPerm(messageReceivedEvent);
            String Description = "현재 권한 : `" + getHighestPerm
                    + "`\n필요한 권한 : `" + "ADMINISTRATOR"
                    + "`\n사용하려는 명령어 : `" + command + "`";

            embedUtil.Embed("권한 부족",Color.RED,Description);
        }
        return hasAdmin;
    }
}