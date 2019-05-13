package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.List;
import java.util.Set;
import static java.util.stream.Collectors.toList;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String userName;
    private String name;
    private String password;

    //Mapea la columna player
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<Score> scores;

    public Player() { }

    public Player(String user, String name, String password) {
        this.userName = user;
        this.name = name;
        this.password = password;
    }

    /*public void addGame(GamePlayer game) {
        game.setPlayer(this);
        getGamePlayers().add(game);
    }*/

    public List<Game> getGame() {
        return getGamePlayers().stream().map(sub -> sub.getGame()).collect(toList());
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        this.userName = user;
    }

    public String toString() {
        return userName + " ";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Score> getScores() {
        return scores;
    }

    public void setScores(Set<Score> scores) {
        this.scores = scores;
    }
}