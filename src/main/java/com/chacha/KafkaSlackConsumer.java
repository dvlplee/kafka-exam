package com.chacha;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaSlackConsumer {
    @Value("${slack-url}")
    private String webhookUrl;

    @KafkaListener(topics = "churn-alert", groupId = "slack-group")
    public void consume(String message) {
        System.out.println("받은 메시지: " + message);
        sendSlackMsg(message);
    }

    public void sendSlackMsg(String msg) {
        Slack slack = Slack.getInstance();

        JsonObject jsonObject = JsonParser.parseString(msg).getAsJsonObject();
        String userId = jsonObject.get("userId").getAsString();
        String lod = jsonObject.get("lastOrderDate").getAsString();

        String slackMsg = "[고객 이탈 알림]\n고객 ID: " + userId
                + "\n마지막 주문일: " + lod
                + "\n이 고객이 마지막으로 주문한 지 2주 이상 경과했습니다.";
        Payload payload = Payload.builder()
                .text(slackMsg) // Slack 메시지에 사용되는 텍스트
                .build();
        try {
            WebhookResponse response = slack.send(webhookUrl, payload);
            System.out.println("Slack 응답: " + response.getBody());
        } catch (IOException e) {
            System.out.println("Slack 메시지 발송 중 문제가 발생했습니다: " + e.getMessage());
        }
    }
}
