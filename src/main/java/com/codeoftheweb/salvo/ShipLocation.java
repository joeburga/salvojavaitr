package com.codeoftheweb.salvo;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
public class ShipLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

/*    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="shipLocations_id")
    private List<String> shipLocations;*/

    public ShipLocation(){};

/*
    public ShipLocation(List<String> shipLocations){
        this.shipLocations = shipLocations;
    };
*/


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

/*    public List<String> getShipLocations() {
        return shipLocations;
    }

    public void setShipLocations(List<String> shipLocations) {
        this.shipLocations = shipLocations;
    }*/
}
