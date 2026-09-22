package com.example.homework.config;
import com.example.homework.domain.ApprovedIsFinalRule;
import com.example.homework.domain.Rule;
import com.example.homework.domain.RuleChain;
import com.example.homework.domain.TransitionRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class RuleConfig {
    @Bean
    public Rule assignmentRules() {
        return new RuleChain(List.of(new ApprovedIsFinalRule(), new TransitionRule()));
    }
}