package com.animenotice.service;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    @Value("${resend.api-key:}")
    private String apiKey;

    private final AnimeService animeService;

    public NotificationService(AnimeService animeService) {
        this.animeService = animeService;
    }

    public void sendDailyNotification(User user) {
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[通知] Resend APIキーが未設定のため通知をスキップしました");
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
        } catch (ResendException e) {
            System.err.println("[通知] メール送信失敗: " + e.getMessage());
        }
    }

    public String testNotification(User user) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Resend APIキーが未設定です。環境変数 RESEND_API_KEY を確認してください。";
        }
        try {
            send(user.getEmail(), "【AnimeNotice】テスト通知", "AnimeNoticeのテスト通知です。\nメール設定が正常に動作しています。");
            return "テストメールを送信しました: " + user.getEmail();
        } catch (ResendException e) {
            return "メール送信エラー: " + e.getMessage();
        }
    }

    private void send(String to, String subject, String text) throws ResendException {
        Resend resend = new Resend(apiKey);
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(to)
                .subject(subject)
                .text(text)
                .build();
        resend.emails().send(params);
    }
}
