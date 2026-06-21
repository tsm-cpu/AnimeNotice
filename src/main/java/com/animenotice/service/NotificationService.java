package com.animenotice.service;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private final AnimeService animeService;

    public NotificationService(AnimeService animeService) {
        this.animeService = animeService;
    }

    public void sendDailyNotification(User user) {
        if (mailSender == null || fromEmail.isBlank()) {
            System.out.println("[通知] メール設定が未設定のため通知をスキップしました");
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
        } catch (MailException e) {
            System.err.println("[通知] メール送信失敗: " + e.getMessage());
        }
    }

    public String testNotification(User user) {
        if (mailSender == null || fromEmail.isBlank()) {
            return "メール設定が未設定です。application.properties の spring.mail.username と spring.mail.password を確認してください。";
        }
        try {
            send(user.getEmail(), "【AnimeNotice】テスト通知", "AnimeNoticeのテスト通知です。\nメール設定が正常に動作しています。");
            return "テストメールを送信しました: " + user.getEmail();
        } catch (MailException e) {
            return "メール送信エラー: " + e.getMessage();
        }
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
