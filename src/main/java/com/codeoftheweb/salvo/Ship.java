package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String type;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="gamePlayer_id")
    private GamePlayer gamePlayer;

 /*   @OneToMany(mappedBy="shipLocations", fetch=FetchType.EAGER)
    private Set<ShipLocation> shipLocations;*/

    public Ship(){};

    public Ship(String tipo, GamePlayer gamePlayer) {
        this.type = tipo;
        this.gamePlayer = gamePlayer;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }


/*    public Set<ShipLocation> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(Set<ShipLocation> shipLocations) {
        this.shipLocations = shipLocations;
    }*/
}
