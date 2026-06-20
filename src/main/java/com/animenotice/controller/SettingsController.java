package com.animenotice.controller;

import com.animenotice.model.StreamingSite;
import com.animenotice.model.User;
import com.animenotice.model.UserSettings;
import com.animenotice.repository.UserSettingsRepository;
import com.animenotice.service.NotificationService;
import com.animenotice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private User currentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping
    public String settings(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElse(new UserSettings());
        model.addAttribute("settings", settings);
        model.addAttribute("streamingSites", StreamingSite.values());
        model.addAttribute("userEmail", user.getEmail());
        return "settings";
    }

    @PostMapping
    public String saveSettings(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam(required = false) String notificationTime,
                                @RequestParam(required = false) List<String> selectedSites,
                                RedirectAttributes redirectAttributes) {
        User user = currentUser(userDetails);
        UserSettings settings = userSettingsRepository.findByUser(user)
                .orElse(new UserSettings());
        settings.setUser(user);
        settings.setNotificationTime(notificationTime != null ? notificationTime : "08:00");

        Set<String> sites = selectedSites != null ? new HashSet<>(selectedSites) : new HashSet<>();
        settings.setSelectedSites(sites);

        userSettingsRepository.save(settings);
        redirectAttributes.addFlashAttribute("successMessage", "設定を保存しました");
        return "redirect:/settings";
    }

    @PostMapping("/test-notification")
    public String testNotification(@AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        String result = notificationService.testNotification(currentUser(userDetails));
        redirectAttributes.addFlashAttribute("infoMessage", result);
        return "redirect:/settings";
    }
}
