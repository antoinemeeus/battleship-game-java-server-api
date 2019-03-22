package com.codeoftheweb.salvo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    private Integer turnNumber;

    @ElementCollection
    @Column(name = "salvoLocations")
    private List<String> salvoLocations;

    /* Constructor ------------------------------------------------------------------------------*/
    public Salvo() {
    }

    public Salvo(GamePlayer gamePlayer, Integer turnNumber, List<String> salvoLocations) {
        this.gamePlayer = gamePlayer;
        this.turnNumber = turnNumber;
        this.salvoLocations = salvoLocations;
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    public long getId() {
        return id;
    }

    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public Integer getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(Integer turnNumber) {
        this.turnNumber = turnNumber;
    }

    public List<String> getSalvoLocations() {
        return salvoLocations;
    }

    public void setSalvoLocations(List<String> salvoLocations) {
        this.salvoLocations = salvoLocations;
    }

    public String toString() {
        return "Salvo TurnNumber: " + this.turnNumber + " Position list: " + this.salvoLocations;
    }
}
