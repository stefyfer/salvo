package com.codeoftheweb.salvo.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
public class Ship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    private String  type;

    @ElementCollection
    @Column(name = "shipLocation")
    private List<String>    locations;

    @ManyToOne(fetch  = FetchType.EAGER)
    @JoinColumn(name="gamePlayerID")
    private GamePlayer  gamePlayer;

    public Ship(){
    }

    public Ship(String  type, List<String>  locations,  GamePlayer  gamePlayer){
        this.type   =   type;
        this.locations  =   locations;
        this.gamePlayer =   gamePlayer;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public void setGamePlayer(GamePlayer gamePlayer) {
        this.gamePlayer = gamePlayer;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public List<String> getLocations() {
        return locations;
    }

    public GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public Map<String, Object> makeShipDTO(){
        Map<String, Object> dto =  new LinkedHashMap<>();
        dto.put("type",   this.getType());
        dto.put("locations",   this.getLocations());
        return dto;
    }
}
