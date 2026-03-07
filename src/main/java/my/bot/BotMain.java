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

@SpringBootApplication(scanBasePackages = {"my.bot", "response", "CurseWordDB", "WarnDB"})
@EnableMongoRepositories(basePackages = {"CurseWordDB.database", "WarnDB"})

public class BotMain {
    @Getter
    private static final String PREFIX = ">";
    private static final BotToken botToken = new BotToken();
    private static final String token = botToken.getBotToken();

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BotMain.class, args);
        MessageFilter messageFilter = context.getBean(MessageFilter.class);
        ListenCommend  listenCommend = context.getBean(ListenCommend.class);

        final GatwayIntents intents = new GatwayIntents();
        EnumSet<GatewayIntent> intent = intents.getIntents();

        String ANSI_CYAN = "\u001B[36m";
        String ANSI_RESET = "\u001B[0m";

        JDABuilder.createDefault(token).
                enableIntents(intent).
                setActivity(Activity.listening("도움말은 >help")).
                setStatus(OnlineStatus.ONLINE).
                addEventListeners(messageFilter,listenCommend).
                build();

        System.out.println(ANSI_CYAN+"bot booting successful"+ANSI_RESET);
    }

}