package my.bot;

import CurseWordDB.MessageFilter;
import lombok.Getter;
import my.bot.Util.BotToken;
import my.bot.Util.GatwayIntents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import response.ListenCommend;

import java.util.EnumSet;

@SpringBootApplication(scanBasePackages = {"my.bot", "response", "CurseWordDB", "WarnDB"})
@EnableMongoRepositories(basePackages = {"CurseWordDB.database", "WarnDB"})
public class BotMain {

    @Getter
    private static final String PREFIX = ">";

    @Getter
    private static JDA jda;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(BotMain.class, args);
        printAdminUrl(context);

        MessageFilter messageFilter = context.getBean(MessageFilter.class);
        ListenCommend listenCommend = context.getBean(ListenCommend.class);
        BotToken botToken = context.getBean(BotToken.class);

        GatwayIntents intents = new GatwayIntents();
        EnumSet<GatewayIntent> intent = intents.getIntents();

        String ANSI_CYAN = "\u001B[36m";
        String ANSI_RESET = "\u001B[0m";

        String token = botToken.getBotToken();

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(intent)
                    .setActivity(Activity.listening("commands >help"))
                    .setStatus(OnlineStatus.ONLINE)
                    .addEventListeners(messageFilter, listenCommend)
                    .build()
                    .awaitReady();

            System.out.println(ANSI_CYAN + "bot booting successful" + ANSI_RESET);
        } catch (InvalidTokenException e) {
            System.err.println("BOT_TOKEN is invalid. Web admin started, but Discord bot is offline.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Discord bot startup was interrupted. Web admin is still running.");
        }
        catch (IllegalArgumentException ignored){
            System.err.println("토큰값이 올바르지 않습니다.");
        }
    }

    private static void printAdminUrl(ApplicationContext context) {
        Environment environment = context.getEnvironment();

        String port = environment.getProperty(
                "local.server.port",
                environment.getProperty("server.port", "8080")
        );

        String contextPath = environment.getProperty("server.servlet.context-path", "");

        if (contextPath.equals("/")) {
            contextPath = "";
        }

        System.out.println("web admin page: http://localhost:" + port + contextPath);
    }
}
