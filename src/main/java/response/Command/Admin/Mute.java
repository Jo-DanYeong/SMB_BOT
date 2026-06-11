package response.Command.Admin;

import WarnDB.WarnRepo;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import response.Util.EmbedUtil;
import response.Util.HighestPermUtil;

import java.awt.*;
import java.time.Duration;

public class Mute {
    // 뮤트 (>mute @유저 )
    public static void MuteCommand(MessageReceivedEvent messageReceivedEvent, String[] args, boolean mute, WarnRepo warnRepo) {
        int time = 5;
        EmbedUtil embedUtil = new EmbedUtil(messageReceivedEvent);

        //뮤트
        if (mute){
            String muteTime = (args.length > 2 ? args[2] : "").toLowerCase();//뮤트할 시간

            if(messageReceivedEvent.getMessage().getMentions().getMembers().isEmpty()) {
                messageReceivedEvent.getMessage().reply("뮤트할 대상을 멘션해주세요.").queue();
                return;
            }

            //뮤트 시간 처리
            try {
                if(muteTime.isEmpty()){
                    messageReceivedEvent.getMessage().reply("뮤트 시간을 입력해 주세요").queue();
                    return;
                }
                else{
                    time = Integer.parseInt(muteTime);
                }
            }catch (NumberFormatException e){
                embedUtil.Embed("뮤트 시간 에러","뮤트할 시간은 정수로 입력해주세요", Color.RED,true);
            }

            Member target = messageReceivedEvent.getMessage().getMentions().getMembers().get(0);
            String guildId = messageReceivedEvent.getGuild().getId();
            String userId = target.getId();
            try {
                target.getGuild().timeoutFor(target, Duration.ofMinutes(time))
                        .queue(
                                v -> {warnRepo.setMuted(guildId, userId, true);
                                    embedUtil.Embed("뮤트 성공",target.getAsMention() + "님을 "+muteTime+"분간 뮤트 했습니다.",Color.GREEN);
                                });
            }
            catch (Exception e) {
                embedUtil.Embed("뮤트중 에러발생","해당 유저가 현재 봇보다 권한이 높기에 뮤트할 수 없습니다.\n"+"대상 권한 : " + HighestPermUtil.GetHighestPerm(messageReceivedEvent),Color.orange);
            }
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
                            e -> embedUtil.Embed("뮤트중 에러발생","에러 코드 : "+e.getMessage(),Color.BLACK)
                    );
        }
    }
}
