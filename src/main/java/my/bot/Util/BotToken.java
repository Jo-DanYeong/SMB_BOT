package my.bot.Util;

import org.springframework.beans.factory.annotation.Value;

public class BotToken  {
    @Value("${BotToken}")
    private String DiscordBotToken;

    public BotToken() {
        System.out.println("토큰 로딩 성공");
    }

    public String getBotToken() {
        System.out.println(DiscordBotToken);
        return DiscordBotToken;
    }
}
