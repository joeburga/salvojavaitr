package com.codeoftheweb.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.*;

import static java.util.stream.Collectors.toList;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private Date newDate = new Date();

    @OneToMany(mappedBy="game", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    public Game() {
    }

    public Game(Date date) {
        this.newDate = date;
    }

    public void addGame(GamePlayer player) {
        player.setGame(this);
        getGamePlayers().add(player);
    }

/*  @JsonIgnore
    public List<Player> getGame() {
        return getGamePlayers().stream().map(sub -> sub.getPlayer()).collect(toList());
    }*/

    public Date getDate() {
        return newDate;
    }

    public void setDate(Date date) {
        this.newDate = date;
    }
    
    public String toString() {
        return newDate + " ";
    }

    public long getId(){
        return id;
    }

    public Set<GamePlayer> getGamePlayers() {
        return gamePlayers;
    }

    public void setGamePlayers(Set<GamePlayer> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }
}