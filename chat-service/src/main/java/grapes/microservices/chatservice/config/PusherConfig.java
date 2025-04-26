package grapes.microservices.chatservice.config;

import com.pusher.rest.Pusher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PusherConfig {

    @Value("${pusher.app_id}")
    private String appId;

    @Value("${pusher.key}")
    private String key;

    @Value("${pusher.secret}")
    private String secret;

    @Value("${pusher.cluster}")
    private String cluster;

    @Value("${pusher.encrypted}")
    private boolean encrypted;

    @Bean
    public Pusher pusher() {
        Pusher pusher = new Pusher(appId, key, secret);
        pusher.setCluster(cluster);
        pusher.setEncrypted(encrypted); // true est recommandé (HTTPS)
        // pusher.setHost("..."); // Optionnel si vous utilisez un hôte spécifique
        // pusher.setPort(443); // Optionnel si vous utilisez un port spécifique (443 pour https)
        // pusher.setConnectionTimeout(5000); // Optionnel
        // pusher.setSocketTimeout(5000); // Optionnel
        return pusher;
    }
}