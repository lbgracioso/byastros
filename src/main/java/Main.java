import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Main extends ListenerAdapter {

    private static final String token = "PUT HERE YOUR BOT TOKEN";
    private static final String activity = "the sky...";
    private static final String nasaAPI = "PUT HERE YOUR NASA APOD LINK WITH YOUR TOKEN";
    private static HttpURLConnection connection;

    public static void main(String[] args) throws LoginException {
        JDA jda = JDABuilder.createDefault(token)
                .addEventListeners(new Main())
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.watching(activity))
                .build();

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        String handledMessage = message.getContentRaw();

        if (handledMessage.equals("+apod"))
        {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Preparing...").queue();
            channel.sendMessage(prepareNasaApi()).queue();
        }

    }

    public static String prepareNasaApi() {

        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();

        try {
            URL url = new URL(nasaAPI);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }

            String parseApod = parseApod(responseContent.toString());
            String parsedApodData = parseApod;
            return parsedApodData;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return null;
    }

    public static String parseApod(String dataApod) {

        JSONObject apod = new JSONObject(dataApod);
        String title = apod.getString("title");
        String url = apod.getString("url");

        String data = "Title: " + title + "\nUrl: " + url;

        return data;

    }

}