package response.Util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class EmbedUtil {
    private final MessageReceivedEvent event;

    public EmbedUtil(MessageReceivedEvent event) {
        this.event = event;
    }

    //임베드(기본)
    public void Embed(String title, String message,Color color){
        Embed(title,message,color,false,0);
    }

    //임베드(일정 시간만 보여주고 메세지 삭제)
    public void Embed(String title, String message, Color color, boolean Delete){
        Embed(title,message, color, Delete,5);
    }

    //임베드(원하는 시간만큼 보여주고 메세지 삭제)
    public void Embed(String title, String message, Color color, boolean Delete, int Time){
        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(title);
        eb.setColor(color);
        eb.setDescription(message);

        MessageEmbed embed = eb.build();

        if (Delete){
            event.getMessage().delete().queueAfter(Time,TimeUnit.SECONDS);
            event.getMessage().replyEmbeds(embed).queue(embeMessage -> embeMessage.delete().queueAfter(Time, TimeUnit.SECONDS));
        }
        else{
            event.getMessage().replyEmbeds(embed).queue();
        }
        eb.clear();
    }
}
