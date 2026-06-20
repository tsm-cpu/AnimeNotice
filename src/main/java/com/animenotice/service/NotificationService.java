package com.animenotice.service;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import com.animenotice.model.UserSettings;
import com.animenotice.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AnimeService animeService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    public void sendDailyNotification(User user) {
        if (mailSender == null) {
            System.out.println("[通知] メール設定が未完了のため通知をスキップしました");
            return;
        }

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        List<Anime> todayAnime = animeService.findTodayAnimeForNotification(user, today);

        if (todayAnime.isEmpty()) return;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("【AnimeNotice】今日のアニメ更新通知 " + today.getDisplayName(TextStyle.SHORT, Locale.JAPANESE));

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

            message.setText(body.toString());
            mailSender.send(message);
            System.out.println("[通知] メール送信完了: " + user.getEmail() + " (" + todayAnime.size() + "件)");
        } catch (Exception e) {
            System.err.println("[通知] メール送信失敗: " + e.getMessage());
        }
    }

    public String testNotification(User user) {
        if (mailSender == null) return "メール送信設定が未完了です。application.propertiesを確認してください。";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("【AnimeNotice】テスト通知");
            message.setText("AnimeNoticeのテスト通知です。\nメール設定が正常に動作しています。");
            mailSender.send(message);
            return "テストメールを送信しました: " + user.getEmail();
        } catch (Exception e) {
            return "メール送信エラー: " + e.getMessage();
        }
    }
}
