package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import java.time.Instant;
import javax.persistence.*;

@Entity
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    private Double score;
    private Instant gameFinishedDate;

    /* Constructor ------------------------------------------------------------------------------*/
    public Score() {
    }

    public Score(Game game, Player player, Double score) {
        this.game = game;
        this.player = player;
        this.score = score;
        this.gameFinishedDate = Instant.now();
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    public long getId() {
        return id;
    }

    @JsonIgnore
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @JsonIgnore
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Instant getGameFinishedDate() {
        return gameFinishedDate;
    }

    public void setGameFinishedDate(Instant gameFinishedDate) {
        this.gameFinishedDate = gameFinishedDate;
    }

    public String toString() {
        return "Score: " + this.score + " finished at:" + this.gameFinishedDate;
    }
}
