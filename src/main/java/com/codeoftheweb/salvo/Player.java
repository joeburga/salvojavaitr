package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Arrays;
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
    private String mail;
    private String password;

    //Mapea la columna player
    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private Set<GamePlayer> gamePlayers;

    @OneToMany(mappedBy="player", fetch=FetchType.EAGER)
    private List<Score> scores;

    public Player() { }

    public Player(String userName, String mail, String password) {
        this.userName = userName;
        this.mail = mail;
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

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getEmail() {
        return mail;
    }

    public void setEmail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Score> getScores() {
        return scores;
    }

    public void setScores(List<Score> scores) {
        this.scores = scores;
    }

    public float getScore(Player player){

        return getWins(player.getScores()) + getDraws(player.getScores())*(float)0.5 + getLosses(player.getScores())*0 ;
    }

    public float getWins(List<Score> scores){

        return scores
                .stream()
                .filter(score -> score.getScore() == 1)
                .count();
    }

    public float getDraws(List<Score> scores){

        return scores
                .stream()
                .filter(score -> score.getScore() == 0.5)
                .count();
    }
    public float getLosses(List<Score> scores){

        return scores
                .stream()
                .filter(score -> score.getScore() == 0)
                .count();
    }

}