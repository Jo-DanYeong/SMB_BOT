package response;

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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListenCommend extends ListenerAdapter {

    private final CurseWordRepo curseWordRepo;
    private final WarnRepo warnRepo;
    private static final String PREFIX = ">";
    private String comment = "", command = "";
    private MessageReceivedEvent messageReceivedEvent;
    EmbedUtil embedUtil;

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        embedUtil = new EmbedUtil(event);
        String message = event.getMessage().getContentRaw().trim();
        // 접두사로 시작하지 않으면 무시
        if (!message.startsWith(PREFIX)) {
            return;
        }

        // ">" 만 입력했을 때 도움말 출력
        if (message.equals(PREFIX)) {
            event.getChannel().sendMessage("도움말이 필요하시면 '>help 입력하세요").queue();
            return;
        }

        String[] args = message.substring(PREFIX.length()).trim().split("\\s+");
        command = args[0].toLowerCase();
        messageReceivedEvent = event;

        //커맨드 처리
        switch (command) {
            case "h":
            case "help":
            case "도움말":
                readhelp("HelpMd/help.md");
                break;

            case "f":
            case"filter":
            case "금지어":
                CurseWordCommand(args);
                break;

            case "w":
            case "경고":
                WarnCommand(args,"add");
                break;

            case "rw":
            case"경고 취소":
            case "경고회수":
                WarnCommand(args,"sub");
                break;

            case "m":
            case "mute":
            case "뮤트":
                MuteCommand(args,true);
                break;

            case "um":
            case "언뮤트":
                MuteCommand(args,false);
                break;

            case "c":
            case "clear":
            case "클린":
                CleanCommand(args);
                break;

            default:
                embedUtil.Embed("알 수 없는 명령어",Color.RED,command + "(은)는 알 수 없습니다.\n" +
                                                                        "도움말은 >help를 사용하여 확인하실 수 있습니다.",true,5);
        }
    }

    // 금지어 관련 커맨드 처리
    private void CurseWordCommand(String[] args) {
        //권한 체크
        if (ChackOp()) return;

        String word = (args.length > 1 ? args[1] : "").toLowerCase();
        String OnOff = (args.length > 2 ? args[2] : "").toLowerCase();
        String guildId = messageReceivedEvent.getGuild().getId();

        //금지어 등록 이외의 명령어 처리
        switch (word) {
            case "목록":
            case "리스트":
            case "ls":
            case "list":
                String list = curseWordRepo.listWords(guildId);
                embedUtil.Embed("금지어 목록",Color.green,list);
                return;

            case "도움":
            case "h":
            case "help":
                readhelp();
                return;
            default:
                if(OnOff.isEmpty()){
                    embedUtil.Embed("알 수 없는 명령어",Color.cyan,word + "(은)는 알 수 없습니다.\n" +
                                                                                    "도움말은 >help를 사용하여 확인하실 수 있습니다.",true);
                }
        }

        // 금지어 등록/해제/재활성화 처리
        switch (OnOff) {
            case "new":
                if (curseWordRepo.exists(guildId, word)) {
                    comment = "이미 등록된 금지어입니다.";
                } else {
                    curseWordRepo.ban(guildId, word);
                    comment = "금지어 '" + word + "'가 등록되었습니다.";
                }
                break;
            case "off":
                if (!curseWordRepo.exists(guildId, word)) {
                    comment = "먼저 'new'로 금지어를 등록한 뒤 'off'를 사용할 수 있습니다.";
                    break;
                }
                curseWordRepo.unban(guildId, word);
                comment = "금지어 '" + word + "'가 해제되었습니다.";
                break;
            case "on":
                if (!curseWordRepo.exists(guildId, word)) {
                    comment = "먼저 'new'로 금지어를 등록한 뒤 'on'을 사용할 수 있습니다.";
                    break;
                }
                curseWordRepo.reban(guildId, word);
                comment = "금지어 '" + word + "'가 활성화되었습니다.";
                break;
        }
        messageReceivedEvent.getMessage().reply(comment).queue();
    }

    // 경고 커맨드 처리: >경고 @유저 [사유...]
    private void WarnCommand(String[] args,String Warning) {
        if (ChackOp()) return;

        if (messageReceivedEvent.getMessage().getMentions().getMembers().isEmpty()) {
            messageReceivedEvent.getMessage().reply("경고를 줄 대상을 멘션해주세요.").queue();
            return;
        }

        Member target = messageReceivedEvent.getMessage().getMentions().getMembers().get(0);
        String guildId = messageReceivedEvent.getGuild().getId();
        String userId = target.getId();
        String reason = (args.length > 2 ? args[2] : "").toLowerCase();

        WarnCount warnCount;
        int count;

        switch (reason){
            case "조회":
            case "list":
                String title = target.getUser().getName() + "님의 누적 경고 횟수";
                count = warnRepo.getWarn(guildId,userId);
                comment = "경고 횟수 : "+ count + "\n뮤트 : " + warnRepo.getMute(guildId,userId);
                embedUtil.Embed(title,Color.cyan,comment);
                return;

            case "reset":
            case "초기화":
                warnRepo.resetWarn(guildId,userId);
                return;
        }

        switch (Warning){
            case "add":
                warnCount = warnRepo.addWarn(guildId, userId);
                count = warnCount.getWarncnt();

                if(reason.isEmpty()){
                    messageReceivedEvent.getChannel()
                            .sendMessage(target.getAsMention() + "님께 경고 " + count + "회가 누적되었습니다.")
                            .queue();
                }
                else{
                    messageReceivedEvent.getChannel()
                            .sendMessage(target.getAsMention() + "님께 경고 " + count + "회가 누적되었습니다. (사유: " + reason + ")")
                            .queue();
                }

                // 타임아웃(뮤트)
                try {
                    if (count >= 5 && !warnCount.isMute()) {
                        int timeout = count * 2;
                        target.getGuild().timeoutFor(target, Duration.ofMinutes(timeout))
                                .reason("경고 " + count + "회 누적")
                                .queue(
                                        v -> {
                                            warnRepo.setMuted(guildId, userId, true);
                                            messageReceivedEvent.getChannel().sendMessage(target.getAsMention() + "님이 10분 동안 뮤트되었습니다.").queue();
                                        },
                                        e -> messageReceivedEvent.getChannel().sendMessage("뮤트에 실패했습니다: " + e.getMessage()).queue()
                                );
                    }
                }catch (HierarchyException e){
                    System.out.println("해당 유저는 뮤트할 수 없습니다.");
                    embedUtil.Embed("",Color.cyan,target.getAsMention()+"(은)는 뮤트할 수 없습니다");
                    messageReceivedEvent.getMessage().reply(target.getAsMention()+"(은)는 뮤트할 수 없습니다").queue();
                }
                break;

            case "sub":
                count = warnRepo.getWarn(guildId, userId);

                if (count <= 0) {
                    messageReceivedEvent.getMessage().reply(target.getAsMention() + "(은)는 경고 수가 0입니다.").queue();
                }
                else {
                    warnCount = warnRepo.subWarn(guildId, userId);
                    count = warnCount.getWarncnt();
                    String title = "경고 횟수 감소";
                    comment = target.getAsMention() + "님의 경고횟수가 1감소했습니다.\n" + "현재 경고 회수 : " + count;

                    embedUtil.Embed(title, Color.cyan, comment);
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + Warning);
        }
    }

    // 뮤트 (>mute @유저 )
    private void MuteCommand(String[] args,boolean mute) {
        if (ChackOp()) return;

        //뮤트
        if (mute){
            String muteTime = (args.length > 2 ? args[2] : "").toLowerCase();

            if(messageReceivedEvent.getMessage().getMentions().getMembers().isEmpty()) {
                messageReceivedEvent.getMessage().reply("뮤트할 대상을 멘션해주세요.").queue();
                return;
            }
            else if(muteTime.isEmpty()){
                messageReceivedEvent.getMessage().reply("뮤트 시간을 입력해 주세요").queue();
            }

            Member target = messageReceivedEvent.getMessage().getMentions().getMembers().get(0);
            String guildId = messageReceivedEvent.getGuild().getId();
            String userId = target.getId();

            target.getGuild().timeoutFor(target, Duration.ofMinutes(10))
                    .queue(
                            v -> {
                                warnRepo.setMuted(guildId, userId, true);
                                messageReceivedEvent.getChannel().sendMessage(target.getAsMention() + "님이 뮤트되었습니다.").queue();
                            },
                            e -> messageReceivedEvent.getChannel().sendMessage("뮤트에 실패했습니다: " + e.getMessage()).queue()
                    );
        }

        //언뮤트
        else{
            if (messageReceivedEvent.getMessage().getMentions().getMembers().isEmpty()) {
                messageReceivedEvent.getMessage().reply("언뮤트할 대상을 멘션해주세요.").queue();
                return;
            }

            Member target = messageReceivedEvent.getMessage().getMentions().getMembers().get(0);
            String guildId = messageReceivedEvent.getGuild().getId();
            String userId = target.getId();

            target.getGuild().removeTimeout(target)
                    .queue(
                            v -> {
                                warnRepo.setMuted(guildId, userId, false);
                                messageReceivedEvent.getChannel().sendMessage(target.getAsMention() + "님의 뮤트가 해제되었습니다.").queue();
                            },
                            e -> messageReceivedEvent.getChannel().sendMessage("언뮤트에 실패했습니다: " + e.getMessage()).queue()
                    );
        }
    }

    //클리어 (>클린 [삭제할 메세지 숫자])
    private void CleanCommand(String[] args){
        ChackOp();
        int Cnt = 2;//지울 메세지 개수 + (>클린[내가 친 메세지])
        String String_Cnt = (args.length > 1 ? args[1] : "").toLowerCase();

        //만약 숫자 입력을 안하면 기본값 2로
        if (String_Cnt.isEmpty()){
            String_Cnt = "2";
        }

        try {
            Cnt = Integer.parseInt(String_Cnt);

            if (Cnt <= 0 || Cnt >100) {
                messageReceivedEvent.getMessage().reply("숫자는 1 ~ 100 사이로 입력해주세요.").queue();
            }
        }catch (NumberFormatException e){
            System.out.println(e.getMessage()+"\n 문자를 숫자로 변환하려 함");
            messageReceivedEvent.getMessage().reply("숫자를 입력하세요").
                    queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
        }

        messageReceivedEvent.getChannel().getIterableHistory()
                .takeAsync(Cnt+1) // 비동기적으로 메시지 가져오기
                .thenAccept(messageReceivedEvent.getChannel()::purgeMessages);

        embedUtil.Embed("메시지 삭제",Color.RED,"메세지 "+Cnt+"개 만큼 삭제 했습니다.",true,2);
    }

    //help.md를 읽어오기1
    private void readhelp(){
        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("HelpMd/help.md");

        if (is == null) {
            throw new RuntimeException("파일을 찾을 수 없습니다.");
        }
        try {
            String help = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            embedUtil.Embed("도움말",Color.cyan,help);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean ChackOp() {
        boolean hasAdmin = messageReceivedEvent.getMember() != null && !messageReceivedEvent.getMember().hasPermission(Permission.ADMINISTRATOR);
        if (hasAdmin) {
            String currentPerms = messageReceivedEvent.getMember()
                    .getPermissions().stream()
                    .map(Permission::getName).sorted()
                    .collect(Collectors.joining(", "));

            String Description = "현재 권한 : `" + currentPerms
                    + "`\n필요한 권한 : `" + "ADMINISTRATOR"
                    + "`\n사용하려는 명령어 : `" + command + "`";
            embedUtil.Embed("권한 부족",Color.RED,Description);
        }
        return hasAdmin;
    }
}