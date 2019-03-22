package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private Integer avatarID;
    private String userName;
    private String email;
    private String password;

    @OneToMany(mappedBy = "playerP", fetch = FetchType.EAGER)
    private Set<GamePlayer> gamePlayers = new HashSet<>();

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<Score> scores = new ArrayList<>();

    /* Constructor ------------------------------------------------------------------------------*/
    public Player() {
    }

    public Player(Integer avatarID, String userName, String email, String password) {
        this.avatarID=avatarID;
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    public void addGamePlayer(GamePlayer gamePlayer) {
        gamePlayer.setPlayer(this);
        gamePlayers.add(gamePlayer);
    }

    public void addScore(Score newScore) {
        newScore.setPlayer(this);
        scores.add(newScore);
    }
    @JsonIgnore
    public List<Score> getScores() {
        return scores;
    }

    public Score getScore(Game game) {
        return scores.stream().filter(sc -> sc.getGame().getId() == game.getId()).findFirst().orElse(null);
    }

    @JsonIgnore
    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }
    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(Integer avatarID) {
        this.avatarID = avatarID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ID: " + this.id + " userName: " + this.userName + " with email: " + this.email + " and pwd: " + this.password;
    }
}


