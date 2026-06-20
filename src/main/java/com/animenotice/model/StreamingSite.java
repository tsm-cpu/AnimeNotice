package com.animenotice.model;

public enum StreamingSite {
    NETFLIX("Netflix"),
    AMAZON_PRIME("Amazon Prime Video"),
    DISNEY_PLUS("Disney+"),
    HULU("Hulu"),
    D_ANIME("dアニメストア"),
    ABEMA("ABEMAビデオ"),
    NICONICO("ニコニコ動画"),
    BANDAI_CHANNEL("バンダイチャンネル"),
    FOD("FOD"),
    U_NEXT("U-NEXT"),
    ANIME_HODAI("アニメ放題"),
    DMM_TV("DMM TV");

    private final String displayName;

    StreamingSite(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
