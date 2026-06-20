package com.animenotice.service;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import com.animenotice.repository.AnimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Service
public class AnimeService {

    @Autowired
    private AnimeRepository animeRepository;

    public List<Anime> findAll(User user) {
        return animeRepository.findByUserOrderByDayOfWeekAscStartTimeAsc(user);
    }

    public List<Anime> findByDayOfWeek(User user, DayOfWeek dayOfWeek) {
        return animeRepository.findByUserAndDayOfWeekOrderByStartTimeAsc(user, dayOfWeek);
    }

    public List<Anime> findTodayAnimeForNotification(User user, DayOfWeek dayOfWeek) {
        return animeRepository.findByUserAndDayOfWeekAndNotificationEnabled(user, dayOfWeek, true);
    }

    public Optional<Anime> findById(User user, Long id) {
        return animeRepository.findByIdAndUser(id, user);
    }

    public Anime save(Anime anime) {
        return animeRepository.save(anime);
    }

    public void delete(Long id) {
        animeRepository.deleteById(id);
    }
}
