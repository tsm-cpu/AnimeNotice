package com.animenotice.controller;

import com.animenotice.model.Anime;
import com.animenotice.model.StreamingSite;
import com.animenotice.model.User;
import com.animenotice.service.AnimeService;
import com.animenotice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AnimeController {

    @Autowired
    private AnimeService animeService;

    @Autowired
    private UserService userService;

    private static final Map<DayOfWeek, String> DAY_NAMES = new LinkedHashMap<>();
    static {
        DAY_NAMES.put(DayOfWeek.MONDAY, "月曜日");
        DAY_NAMES.put(DayOfWeek.TUESDAY, "火曜日");
        DAY_NAMES.put(DayOfWeek.WEDNESDAY, "水曜日");
        DAY_NAMES.put(DayOfWeek.THURSDAY, "木曜日");
        DAY_NAMES.put(DayOfWeek.FRIDAY, "金曜日");
        DAY_NAMES.put(DayOfWeek.SATURDAY, "土曜日");
        DAY_NAMES.put(DayOfWeek.SUNDAY, "日曜日");
    }

    private User currentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = currentUser(userDetails);
        List<Anime> allAnime = animeService.findAll(user);
        Map<DayOfWeek, List<Anime>> groupedAnime = allAnime.stream()
                .collect(Collectors.groupingBy(Anime::getDayOfWeek));

        model.addAttribute("groupedAnime", groupedAnime);
        model.addAttribute("dayNames", DAY_NAMES);
        model.addAttribute("today", LocalDate.now().getDayOfWeek());
        model.addAttribute("totalCount", allAnime.size());
        model.addAttribute("userEmail", user.getEmail());
        return "index";
    }

    @GetMapping("/anime/new")
    public String newAnime(Model model) {
        model.addAttribute("anime", new Anime());
        model.addAttribute("streamingSites", StreamingSite.values());
        model.addAttribute("dayNames", DAY_NAMES);
        model.addAttribute("isEdit", false);
        return "anime/form";
    }

    @PostMapping("/anime")
    public String createAnime(@AuthenticationPrincipal UserDetails userDetails,
                               @Valid @ModelAttribute Anime anime,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("streamingSites", StreamingSite.values());
            model.addAttribute("dayNames", DAY_NAMES);
            model.addAttribute("isEdit", false);
            return "anime/form";
        }
        anime.setUser(currentUser(userDetails));
        animeService.save(anime);
        redirectAttributes.addFlashAttribute("successMessage", "「" + anime.getTitle() + "」を登録しました");
        return "redirect:/";
    }

    @GetMapping("/anime/{id}/edit")
    public String editAnime(@AuthenticationPrincipal UserDetails userDetails,
                             @PathVariable Long id, Model model) {
        return animeService.findById(currentUser(userDetails), id).map(anime -> {
            model.addAttribute("anime", anime);
            model.addAttribute("streamingSites", StreamingSite.values());
            model.addAttribute("dayNames", DAY_NAMES);
            model.addAttribute("isEdit", true);
            return "anime/form";
        }).orElse("redirect:/");
    }

    @PostMapping("/anime/{id}/update")
    public String updateAnime(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               @Valid @ModelAttribute Anime anime,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("streamingSites", StreamingSite.values());
            model.addAttribute("dayNames", DAY_NAMES);
            model.addAttribute("isEdit", true);
            return "anime/form";
        }
        User user = currentUser(userDetails);
        animeService.findById(user, id).ifPresent(existing -> {
            existing.setTitle(anime.getTitle());
            existing.setDayOfWeek(anime.getDayOfWeek());
            existing.setStartTime(anime.getStartTime());
            existing.setStreamingSite(anime.getStreamingSite());
            existing.setNotificationEnabled(anime.isNotificationEnabled());
            existing.setMemo(anime.getMemo());
            animeService.save(existing);
        });
        redirectAttributes.addFlashAttribute("successMessage", "「" + anime.getTitle() + "」を更新しました");
        return "redirect:/";
    }

    @PostMapping("/anime/{id}/delete")
    public String deleteAnime(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id, RedirectAttributes redirectAttributes) {
        animeService.findById(currentUser(userDetails), id).ifPresent(anime -> {
            animeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "「" + anime.getTitle() + "」を削除しました");
        });
        return "redirect:/";
    }

    @PostMapping("/anime/{id}/toggle")
    public String toggleNotification(@AuthenticationPrincipal UserDetails userDetails,
                                      @PathVariable Long id) {
        animeService.findById(currentUser(userDetails), id).ifPresent(anime -> {
            anime.setNotificationEnabled(!anime.isNotificationEnabled());
            animeService.save(anime);
        });
        return "redirect:/";
    }
}
