package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    /* ######################## @Autowireds ######################## */

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;


    /* ######################## MÉTODOS AUXILIARES ######################## */

    /* Verifica que no cualquier jugador pueda entrar a mi juego */
    private boolean wrongGamePlayer(GamePlayer gamePlayer, Player player) {
        boolean corretGP = gamePlayer.getPlayer().getId() != player.getId();
        return corretGP;
    }

    /* Para las ResponseEntity */
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /* Para autenticar a un jugador */
    private Player getAuthentication(Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return playerRepository.findByEmail(authentication.getName());
        }
    }

    /* ######################## @RequestMapping - GETs - FOR DTOs ######################## */

    @RequestMapping("/games")
    public Map<String, Object> makeLoggedPlayer(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));


        dto.put("games", getAllGamePlayers());

        return dto;
    }

    /* Task 3 - Tarea 2 */
    @RequestMapping("/game_view/{id}")
    public Map<String, Object> getGameView(@PathVariable Long id) {
        return gameViewDTO(gamePlayerRepository.findById(id).get());
    }

    @RequestMapping("/leaderBoard")
    public List<Object> makeLeaderBoard() {
        return playerRepository
                .findAll()
                .stream()
                .map(player -> makePlayerDTO(player))
                .collect(Collectors.toList());
    }


    /* ######################## DTO GAMES && GAMEPLAYERS ######################## */

    private Map<String, Object> gameViewDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();

        dto.put("id", gamePlayer.getGame().getId());
        dto.put("created", gamePlayer.getGame().getDate());
        /*Estoy en un gameplayer luiego getgame para ir al juego donde estoy,
          luego desde el game traig los gamePlayers.*/
        dto.put("gamePlayers", makeGamePlayersListaDTO(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", makeShipsListaDTO(gamePlayer.getShips())); // gamePlayer.getShips()
        dto.put("salvoes", makeListGamesSalvoes(gamePlayer.getGame()));
        /*gamePlayer.getGame().getGamePlayers().stream().map(
                gamePlayer1 -> makeSalvoesListaDTO(gamePlayer1.getSalvoes()))*/
        dto.put("scores", makeListGamesScores(gamePlayer.getGame().getScores()));

        // Task 5 - 1
        //dto.put("hits", makeHitsListaDTO(gamePlayer.getGame().getGamePlayers()));
        dto.put("hits", gamePlayerHits(gamePlayer));

        return dto;
    }

    ///////////////////////////// Task 5 - 1

    private Map<String, Object> gamePlayerHits(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("self", getHits(gamePlayer));
        dto.put("opponent", getHits(getOpponent(gamePlayer)));

        //dto.put("opponent", getOpponent(gamePlayer).getShips().stream().map(ship -> ship.getLocations() + ship.getType()));

        return dto;
    }

    private Map<String, Object> getHits(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();


        dto.put("turn", gamePlayer.getSalvoes().stream().findFirst().get().getTurn());
        //hitsLocations mis salvos acertados en el ship del oponente
        dto.put("hitLocations", salvoesAcertadosPlayer(gamePlayer.getSalvoes(),getOpponent(gamePlayer).getShips()));
        dto.put("misSalvos", gamePlayer.getSalvoes().stream().findFirst().get().getLocations());
        dto.put("misShips", gamePlayer.getShips().stream().map(ship -> ship.getType() + " " + ship.getLocations()));
        //dto.put("damages", damageHits(gamePlayer));
        dto.put("missed", missedSalvoes(gamePlayer.getSalvoes(),getOpponent(gamePlayer).getShips()));


        return dto;
    }

 /*   private Map<String, Object> damageHits(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        //dto.put("carrierHits", getCarrier);
        dto.put("patrolboatHits",getPatrolBoatHits(gamePlayer));

        return dto;
    }

    public int getPatrolBoatHits(GamePlayer gamePlayer){

        getOpponent(gamePlayer).getShips().stream().map(ship -> ship.getType()).collect(Collectors.toList());


    }*/

    public int missedSalvoes(Set<Salvo> salvoes,Set<Ship> ships) {

        List<String> salvoLocations =  salvoes.stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        List<String> shipLocations =  ships.stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());

/*        for (String t : salvoLocations) {
            if(shipLocations.contains(t)) {
                acertados.add(t);
            }
        }*/

        List<String> salvoesAcertados = salvoLocations.stream()
                .filter(shipLocations::contains)
                .collect(Collectors.toList());

        return shipLocations.size() - salvoesAcertados.size();
    }

    public List<String> salvoesAcertadosPlayer(Set<Salvo> salvoes,Set<Ship> ships) {

        List<String> salvoLocations =  salvoes.stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        List<String> shipLocations =  ships.stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());

/*        for (String t : salvoLocations) {
            if(shipLocations.contains(t)) {
                acertados.add(t);
            }
        }*/

        List<String> salvoesAcertados = salvoLocations.stream()
                .filter(shipLocations::contains)
                .collect(Collectors.toList());

        return salvoesAcertados;
    }

    private GamePlayer getOpponent(GamePlayer gamePlayer){
        return gamePlayer.getGame().getGamePlayers().stream()
                .filter(gp -> gp != gamePlayer)
                .findFirst()
                .orElse(null);
    }

/////////////////////////////


    public List<Object> getAllGamePlayers() {
        return gameRepository
                .findAll()
                .stream()
                .map(game -> makeGameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", game.getId());
        dto.put("creationDate", game.getDate());
        dto.put("gamePlayers", makeGamePlayersListaDTO(game.getGamePlayers()));
        //Task 5
        dto.put("scores", makeListGamesScores(game.getScores()));

        return dto;
    }

    /* Itero sobre la lista anterior */
    public List<Object> makeGamePlayersListaDTO(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

        return dto;
    }

    public Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", player.getId());
        dto.put("email", player.getEmail());
        //Task 5
        dto.put("score", makeScoresList(player));
        return dto;
    }


    /* ######################## DTO SHIPS ######################## */

    public List<Object> makeShipsListaDTO(Set<Ship> ships) {
        return ships
                .stream()
                .map(ship -> makeShipDTO(ship))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());

        return dto;
    }


    /* ######################## DTO SALVOES ######################## */

    private List<Map<String, Object>> makeListGamesSalvoes(Game game) {
        List<Map<String, Object>> dto = new ArrayList<>();

        game.getGamePlayers().forEach(gamePlayer -> dto.addAll(makeSalvoesListaDTO(gamePlayer.getSalvoes())));

        return dto;
    }

    /* Desde game necesito todos los gameplayers, y desde gameplayer necesito todos los salvoes
       Lista de mapas. */
    public List<Map<String, Object>> makeSalvoesListaDTO(Set<Salvo> salvoes) {
        return salvoes
                .stream()
                .map(salvo -> makeSalvoDTO(salvo))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeSalvoDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("turn", salvo.getTurn());
        dto.put("player", salvo.getGamePlayer().getId());
        dto.put("locations", salvo.getLocations());

        return dto;
    }

    // Task 5 
    /* ######################## DTO SCORES ######################## */

    public Map<String, Object> makeScoresList(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("name", player.getUserName());
        dto.put("total", player.getScore(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));

        return dto;
    }

    public List<Object> makeListGamesScores(Set<Score> scores) {
        return scores
                .stream()
                .map(score -> makeScoreDTO(score))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeScoreDTO(Score score) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getFinishDate());

        return dto;
    }


    /* ######################## @RequestMapping - POSTs ######################## */

    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createUser(@RequestParam String userName,
                                                          @RequestParam String email,
                                                          @RequestParam String password) {

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Missing data"), HttpStatus.FORBIDDEN);
        }

        if (playerRepository.findByEmail(email) != null) {
            return new ResponseEntity<>(makeMap("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(userName, email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No name given"), HttpStatus.FORBIDDEN);
        } else {
            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game newGame = new Game(date);
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn,
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        Game game = gameRepository.findById(nn).orElse(null);
        //GamePlayer gamePlayer = gamePlayerRepository.findById(nn).orElse(null);

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No name given"), HttpStatus.UNAUTHORIZED);
        }
/*        if (gameRepository.findById(gamePlayer.getGame().getId()).orElse(null) == null){
            return new ResponseEntity<>(makeMap("error","No such game"), HttpStatus.FORBIDDEN);
        }*/
        if (game == null) {
            return new ResponseEntity<>(makeMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }
/*        if (gamePlayer.getGame().getGamePlayers().size() >= 2){
            return new ResponseEntity<>(makeMap("error","Game is full"), HttpStatus.FORBIDDEN);
        }*/
        if (game.getGamePlayers().size() >= 2) {
            return new ResponseEntity<>(makeMap("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        GamePlayer newGamePlayer = new GamePlayer(game, authenticatedPlayer);
        game.addGame(newGamePlayer);
        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(makeMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable Long gamePlayerId,
                                                        @RequestBody Set<Ship> ships, //Cuerpo de la Request
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No name given"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No gamePlayerID given"), HttpStatus.UNAUTHORIZED);
        }
        if (wrongGamePlayer(gamePlayer, authenticatedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        } else {
            if (gamePlayer.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);
                
                return new ResponseEntity<>(makeMap("ok", "Ships saved"), HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>(makeMap("error", "Player already has ships"), HttpStatus.FORBIDDEN);
            }
        }
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvoes(@PathVariable Long gamePlayerId,
                                                          @RequestBody Salvo salvo,
                                                          Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthentication(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No name given"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(makeMap("error", "No gamePlayerID given"), HttpStatus.UNAUTHORIZED);
        }
        if (wrongGamePlayer(gamePlayer, authenticatedPlayer)) {
            return new ResponseEntity<>(makeMap("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        } else {
            if (gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == salvo.getTurn()).count() == 1) {
                return new ResponseEntity<>(makeMap("error", "Player already has the salvo"), HttpStatus.FORBIDDEN);

            } else {
                gamePlayer.getSalvoes().add(salvo);
                salvoRepository.save(salvo);

                return new ResponseEntity<>(makeMap("ok", "Salvoes saved"), HttpStatus.CREATED);
            }
        }
    }
}
