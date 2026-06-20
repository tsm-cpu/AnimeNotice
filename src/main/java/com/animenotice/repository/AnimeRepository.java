package com.animenotice.repository;

import com.animenotice.model.Anime;
import com.animenotice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long> {

    List<Anime> findByUserOrderByDayOfWeekAscStartTimeAsc(User user);

    List<Anime> findByUserAndDayOfWeekOrderByStartTimeAsc(User user, DayOfWeek dayOfWeek);

    List<Anime> findByUserAndDayOfWeekAndNotificationEnabled(User user, DayOfWeek dayOfWeek, boolean notificationEnabled);

    Optional<Anime> findByIdAndUser(Long id, User user);
}
