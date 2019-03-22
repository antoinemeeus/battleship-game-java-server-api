package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Instant creationDate;
    private Instant lastPlayedDate;

    private Boolean afk;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game gameP;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player playerP;


    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Ship> ships = new LinkedHashSet<>();

    @OneToMany(mappedBy = "gamePlayer", fetch = FetchType.EAGER)
    private Set<Salvo> salvoes = new LinkedHashSet<>();

    /* Constructor ------------------------------------------------------------------------------*/
    public GamePlayer() {}



    public GamePlayer(Game game, Player player) {
        this.gameP = game;
        this.playerP = player;
        this.creationDate = Instant.now();
        this.lastPlayedDate=Instant.now();
        this.afk=false;
    }

    /* Getters & Setters ------------------------------------------------------------------------*/
    public Game getGame() {
        return gameP;
    }

    public void setGame(Game game) {
        this.gameP = game;
    }

    public Player getPlayer() {
        return playerP;
    }
    public Instant getLastPlayedDate() {
        return lastPlayedDate;
    }
    public void setLastPlayedDate(Instant lastPlayedDate) {
        this.lastPlayedDate = lastPlayedDate;
    }
    public Boolean getAfk() {
        return afk;
    }
    public void setAfk(Boolean afk) {
        this.afk = afk;
    }
    public void setPlayer(Player player) {
        this.playerP = player;
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public void addShip(Ship newShip) {
        newShip.setGamePlayer(this);
        this.ships.add(newShip);
    }
    public Set<Salvo> getSalvoes(){
        return salvoes;
    }

    public void setSalvoes(Set<Salvo> salvoes) {
        this.salvoes = salvoes;
    }
    public void addSalvo(Salvo newSalvo){
        newSalvo.setGamePlayer(this);
        this.salvoes.add(newSalvo);
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Long getId() {
        return id;
    }

    public String toString(){
        return "GamePlayer id:"+this.id+" Player: "+this.playerP+" Salvoes: "+this.salvoes;
    }
}