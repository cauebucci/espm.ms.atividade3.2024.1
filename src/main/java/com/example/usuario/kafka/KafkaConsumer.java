import com.example.usuario.model.LoginAttempt;
import com.example.usuario.repository.LoginAttemptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @KafkaListener(topics = "login_attempts", groupId = "group_id")
    public void consume(String username) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setUsuario(username);
        attempt.setTempo(LocalDateTime.now());
        loginAttemptRepository.save(attempt);
    }
}
