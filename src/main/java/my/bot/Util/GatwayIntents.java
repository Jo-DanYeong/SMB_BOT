package my.bot.Util;

import lombok.Getter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;

@Getter
public class GatwayIntents {
    private final EnumSet<GatewayIntent> intents = EnumSet.of(
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.DIRECT_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_PRESENCES,
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES);

}
