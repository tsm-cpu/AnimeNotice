package com.animenotice.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String notificationTime = "08:00";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_selected_sites", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "site")
    private Set<String> selectedSites = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNotificationTime() { return notificationTime; }
    public void setNotificationTime(String notificationTime) { this.notificationTime = notificationTime; }

    public Set<String> getSelectedSites() { return selectedSites; }
    public void setSelectedSites(Set<String> selectedSites) { this.selectedSites = selectedSites; }
}
