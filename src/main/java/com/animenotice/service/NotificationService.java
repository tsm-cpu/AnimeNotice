package com.animenotice.service;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class NotificationService {

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.from-email:}")
    private String fromEmail;

    private final AnimeService animeService;
    private final RestTemplate restTemplate = new RestTemplate();

    public NotificationService(AnimeService animeService) {
        this.animeService = animeService;
    }

    public void sendDailyNotification(User user) {
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[通知] Brevo APIキーが未設定のため通知をスキップしました");
            return;
        }

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Anime> todayAnime = animeService.findTodayAnimeForNotification(user, today);

        if (todayAnime.isEmpty()) return;

        StringBuilder body = new StringBuilder("今日放送されるアニメの通知です。\n\n");
        for (Anime anime : todayAnime) {
            body.append("▶ ").append(anime.getTitle());
            if (anime.getStartTime() != null && !anime.getStartTime().isBlank()) {
                body.append("  ").append(anime.getStartTime()).append("〜");
            }
            body.append("  [").append(anime.getStreamingSite().getDisplayName()).append("]\n");
            if (anime.getMemo() != null && !anime.getMemo().isBlank()) {
                body.append("  メモ: ").append(anime.getMemo()).append("\n");
            }
            body.append("\n");
        }
        body.append("\n---\nAnimeNotice");

        try {
            send(user.getEmail(),
                    "【AnimeNotice】今日のアニメ更新通知 " + today.getDisplayName(TextStyle.SHORT, Locale.JAPANESE),
                    body.toString());
            System.out.println("[通知] メール送信完了: " + user.getEmail() + " (" + todayAnime.size() + "件)");
        } catch (Exception e) {
            System.err.println("[通知] メール送信失敗: " + e.getMessage());
        }
    }

    public String testNotification(User user) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Brevo APIキーが未設定です。環境変数 BREVO_API_KEY を確認してください。";
        }
        try {
            send(user.getEmail(), "【AnimeNotice】テスト通知", "AnimeNoticeのテスト通知です。\nメール設定が正常に動作しています。");
            return "テストメールを送信しました: " + user.getEmail();
        } catch (Exception e) {
            return "メール送信エラー: " + e.getMessage();
        }
    }

    private void send(String to, String subject, String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "AnimeNotice", "email", fromEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
    }
}
