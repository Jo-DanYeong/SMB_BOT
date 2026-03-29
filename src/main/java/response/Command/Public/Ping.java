package response.Command.Public;


import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Ping {
    public static void Print_Ping(MessageReceivedEvent event) {
        event.getChannel().sendMessage("ping : "+getping(event)).queue();
    }
    public static long getping(MessageReceivedEvent event){
        return event.getJDA().getGatewayPing();
    }
}
