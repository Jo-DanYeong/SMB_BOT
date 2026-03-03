package my.bot;

import CurseWord.MessageFilter;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import response.ListenCommend;

import java.util.EnumSet;

@SpringBootApplication(scanBasePackages = {"my.bot", "response", "CurseWord", "Warn"})
@EnableMongoRepositories(basePackages = {"CurseWord.database", "Warn"})

public class BotMain {
    public static void main(String[] args) {
        final String ANSI_CYAN = "\u001B[36m";
        final String ANSI_RESET = "\u001B[0m";

        ApplicationContext context = SpringApplication.run(BotMain.class, args);

        BotToken botToken = new BotToken();
        GatwayIntents intents = new GatwayIntents();
        EnumSet<GatewayIntent> intent = intents.getIntents();

        String token = botToken.getBotToken();

        MessageFilter messageFilter = context.getBean(MessageFilter.class);
        ListenCommend  listenCommend = context.getBean(ListenCommend.class);

        final String token = botToken.getBotToken();
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_RESET = "\u001B[0m";

        EnumSet<GatewayIntent> intent = intents.getIntents();

        JDABuilder.createDefault(token).
                enableIntents(intent).
                setActivity(Activity.competing("도움말은 >help")).
                setStatus(OnlineStatus.ONLINE).
                addEventListeners(messageFilter,listenCommend).
                build();

        System.out.println(ANSI_CYAN+"bot booting successful"+ANSI_RESET);
    }
}