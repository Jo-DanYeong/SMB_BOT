package response.Command;

import WarnDB.WarnCount;
import WarnDB.WarnRepo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import response.EmbedUtil;

import java.awt.*;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Collectors;

public class Warn {
    private static WarnRepo warnRepo;
    // 경고 커맨드 처리: >경고 @유저 [사유...]
    public static void WarnCommand(MessageReceivedEvent messageReceivedEvent, String[] args, String Warning) {
        if (messageReceivedEvent.getMessage().getMentions().getMembers().isEmpty()) {
            messageReceivedEvent.getMessage().reply("경고를 줄 대상을 멘션해주세요.").queue();
            return;
        }
        EmbedUtil embedUtil = new EmbedUtil(messageReceivedEvent);
        Member target = messageReceivedEvent.getMessage().getMentions().getMembers().get(0);
        String guildId = messageReceivedEvent.getGuild().getId();
        String userId = target.getId();
        String reason = (args.length > 2 ? args[2] : "").toLowerCase();
        String comment;
        Color color;

        WarnCount warnCount;
        int count;

        switch (reason){
            case "조회":
            case "list":
                String title = target.getUser().getName() + "님의 누적 경고 횟수";
                count = warnRepo.getWarn(guildId,userId);
                comment = "경고 횟수 : "+ count + "\n뮤트 : " + warnRepo.getMute(guildId,userId);
                embedUtil.Embed(title, Color.cyan,comment);
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
                    color = new Color(251,101,68);
                    comment = target.getAsMention() + "님께 경고 " + count + "회가 누적되었습니다.";
                }
                else{
                    color = new Color(251,101,68);
                    comment = target.getAsMention() + "님께 경고 " + count + "회가 누적되었습니다. (사유: " + reason + ")";
                }
                embedUtil.Embed("경고 횟수",color,comment);

                // 타임아웃(뮤트)
                try {
                    if (count >= 5 && !warnCount.isMute()) {
                        int timeout = count * 2;
                        color = new Color(251,101,68);
                        comment = target.getAsMention() + "님이 10분 동안 뮤트되었습니다.";
                        String currentPerms = Objects.requireNonNull(messageReceivedEvent.getMember())
                                .getPermissions().stream()
                                .map(Permission::getName).sorted()
                                .collect(Collectors.joining(", "));
                        //유사 final
                        String finalComment = comment;
                        Color finalColor = color;
                        target.getGuild().timeoutFor(target, Duration.ofMinutes(timeout))
                                .reason("경고 " + count + "회 누적")
                                .queue(
                                        v -> {
                                            warnRepo.setMuted(guildId, userId, true);
                                            embedUtil.Embed("뮤트", finalColor, finalComment);
                                        },
                                        e -> embedUtil.Embed("뮤트 실패",Color.RED,target.getAsMention()+"님의 권한이 높습니다."+"\n 현재 권한 : "+ currentPerms)
                                );
                    }
                }catch (HierarchyException e){
                    embedUtil.Embed("뮤트 실패",Color.cyan,target.getAsMention()+"(은)는 뮤트할 수 없습니다");
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
}
