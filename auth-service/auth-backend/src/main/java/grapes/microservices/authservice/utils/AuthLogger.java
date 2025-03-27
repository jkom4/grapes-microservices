package grapes.microservices.authservice.utils;

import lombok.Data;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Data
@Component
public class AuthLogger {

    @Getter
    private static final Logger logger = LoggerFactory.getLogger(AuthLogger.class);
}
