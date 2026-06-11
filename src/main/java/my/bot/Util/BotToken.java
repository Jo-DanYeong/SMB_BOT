package my.bot.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BotToken{
    private final String discordBotToken;

    public BotToken(@Value("${BOT_TOKEN}") String discordBotToken) {
        this.discordBotToken = discordBotToken;
    }

    public String getBotToken() {
        return discordBotToken;
    }
}
