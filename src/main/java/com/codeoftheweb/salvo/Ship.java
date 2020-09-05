package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "gamePlayer_id")
    private GamePlayer gamePlayer;

    @ElementCollection
    @Column(name = "shipLocations")
    private List<String> shipLocations;

    /* Constructor ------------------------------------------------------------------------------*/
    public Ship() {
    }

    public Ship(String type, List<String> shipLocations) {
        this.type = type;
        this.shipLocations = shipLocations;
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    @JsonIgnore
    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer newGamePlayer) {
        this.gamePlayer = newGamePlayer;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getLocations() {
        return shipLocations;
    }

    public void setLocations(List<String> locations) {
        this.shipLocations = locations;
    }
}
