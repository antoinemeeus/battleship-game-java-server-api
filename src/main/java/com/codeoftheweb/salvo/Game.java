package com.codeoftheweb.salvo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;


@Entity
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Instant creationDate;

    @OneToMany(mappedBy = "gameP", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Score> scores = new ArrayList<>();

    /* Constructor ------------------------------------------------------------------------------*/
    public Game() {
        this.creationDate = Instant.now();
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setGame(this);
        gamePlayers.add(gamePlayer);
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    @JsonIgnore
    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public long getId() {
        return id;
    }

    public String toString() {
        return "Game with id:" + this.id + " created at:" + this.creationDate;
    }
}


