package com.animenotice.scheduler;

import com.animenotice.model.UserSettings;
import com.animenotice.repository.UserSettingsRepository;
import com.animenotice.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Component
public class NotificationScheduler {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndNotify() {
        String now = String.format("%02d:%02d", LocalTime.now().getHour(), LocalTime.now().getMinute());

        List<UserSettings> allSettings = userSettingsRepository.findAll();
        for (UserSettings settings : allSettings) {
            if (settings.getNotificationTime() != null && settings.getNotificationTime().equals(now)) {
                notificationService.sendDailyNotification(settings.getUser());
            }
        }
    }
}
