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
import java.util.stream.Collector;
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

    /**
     * Verifica que el Player sea el mismo del Game que el authenticatedPlayer.
     *
     * @param gamePlayer
     * @param authenticatedPlayer
     * @return boolean
     */
    private boolean correctGamePlayer(GamePlayer gamePlayer, Player authenticatedPlayer) {
        return gamePlayer.getPlayer().getId() == authenticatedPlayer.getId();
    }

    /**
     * Para las ResponseEntity
     *
     * @param key
     * @param value
     * @return Map<String, Object>
     */
    private Map<String, Object> responseInfo(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Obtengo el Player que esta logueado en el Juego.
     *
     * @param authentication
     * @return Player
     */
    private Player getAuthenticationPlayer(Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        } else {
            return playerRepository.findByEmail(authentication.getName());
        }
    }

    /**
     * Obtengo el Oponente de mi Juego.
     *
     * @param gamePlayer
     * @return GamePlayer
     */
    private GamePlayer getOpponent(GamePlayer gamePlayer) {
        return gamePlayer.getGame().getGamePlayers().stream()
                .filter(gp -> gp != gamePlayer)
                .findFirst()
                .orElse(null);
    }

    /* ######################## @RequestMapping - GETs - FOR DTOs ######################## */

    @RequestMapping("/games")
    public Map<String, Object> makeauthenticatedPlayer(Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);

        if (authenticatedPlayer == null)
            dto.put("player", "Guest");
        else
            dto.put("player", makePlayerDTO(authenticatedPlayer));


        dto.put("games", getAllGamePlayers());

        return dto;
    }

    @RequestMapping("/game_view/{id}")
    public ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long id,
                                                           Authentication authentication) {
        Map<String, Object> dto = new LinkedHashMap<>();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(id).get();

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.FORBIDDEN);
        }
        if (!correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getGame().getGamePlayers().size() == 1) {
            return new ResponseEntity<>(responseInfo("error", "Waiting for next player, go back homePage!!"), HttpStatus.UNAUTHORIZED);
        }

        dto = gameViewDTO(gamePlayer);
        return new ResponseEntity<>(dto, HttpStatus.OK);
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
        dto.put("gamePlayers", makeGamePlayersList(gamePlayer.getGame().getGamePlayers()));
        dto.put("ships", makeShipsList(gamePlayer.getShips()));
        dto.put("salvoes", makeGamesSalvoesList(gamePlayer.getGame()));
        dto.put("scores", makeGamesScoresList(gamePlayer.getGame().getScores()));
        dto.put("gameState", "PLAY");


        // Task 5 - 1
        dto.put("hits", hitsDTO(gamePlayer, getOpponent(gamePlayer)));
        dto.put("hits1", gamePlayerHits(gamePlayer, getOpponent(gamePlayer)));

        return dto;
    }

    ///////////////////////////// Task 5 - 1

    private Map<String, Object> gamePlayerHits(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("self1", getHits1(self, opponent));
        dto.put("opponent1", getHits1(opponent, self));

        return dto;
    }

    private Map<String, Object> hitsDTO(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("self", getHits(self, opponent));
        dto.put("opponent", getHits(opponent, self));
        return dto;
    }

    private List<Map> getHits(GamePlayer self, GamePlayer opponent) {
        List<Map> dto = new ArrayList<>();
        int carrierDamage = 0;
        int destroyerDamage = 0;
        int patrolboatDamage = 0;
        int submarineDamage = 0;
        int battleshipDamage = 0;
        List<String> carrierLocations = new ArrayList<>();
        List<String> destroyerLocations = new ArrayList<>();
        List<String> submarineLocations = new ArrayList<>();
        List<String> patrolboatLocations = new ArrayList<>();
        List<String> battleshipLocations = new ArrayList<>();

        for (Ship ship : self.getShips()) {
            switch (ship.getType()) {
                case "Carrier":
                    carrierLocations = ship.getLocations();
                    break;
                case "Battleship":
                    battleshipLocations = ship.getLocations();
                    break;
                case "Destroyer":
                    destroyerLocations = ship.getLocations();
                    break;
                case "Submarine":
                    submarineLocations = ship.getLocations();
                    break;
                case "PatrolBoat":
                    patrolboatLocations = ship.getLocations();
                    break;
            }
        }

        //Ordeno por Turn
        List<Salvo> salvoesOpponent = opponent.getSalvoes()
                .stream()
                .sorted(Comparator.comparingInt(Salvo::getTurn))
                .collect(Collectors.toList());

        for (Salvo salvo : salvoesOpponent) {
            Integer carrierHitsInTurn = 0;
            Integer battleshipHitsInTurn = 0;
            Integer submarineHitsInTurn = 0;
            Integer destroyerHitsInTurn = 0;
            Integer patrolboatHitsInTurn = 0;
            Integer missedShots = salvo.getLocations().size();
            Map<String, Object> hitsMapPerTurn = new LinkedHashMap<>();
            Map<String, Object> damagesPerTurn = new LinkedHashMap<>();
            List<String> salvoLocationsList = new ArrayList<>();
            List<String> hitCellsList = new ArrayList<>();
            salvoLocationsList.addAll(salvo.getLocations());


            for (String salvoShot : salvoLocationsList) {
                if (carrierLocations.contains(salvoShot)) {
                    carrierDamage++;
                    carrierHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (battleshipLocations.contains(salvoShot)) {
                    battleshipDamage++;
                    battleshipHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (submarineLocations.contains(salvoShot)) {
                    submarineDamage++;
                    submarineHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (destroyerLocations.contains(salvoShot)) {
                    destroyerDamage++;
                    destroyerHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
                if (patrolboatLocations.contains(salvoShot)) {
                    patrolboatDamage++;
                    patrolboatHitsInTurn++;
                    hitCellsList.add(salvoShot);
                    missedShots--;
                }
            }

            hitsMapPerTurn.put("turn", salvo.getTurn());
            hitsMapPerTurn.put("hitLocations", hitCellsList);
            hitsMapPerTurn.put("damages", damagesPerTurn);
            hitsMapPerTurn.put("missed", missedShots);

            damagesPerTurn.put("carrierHits", carrierHitsInTurn);
            damagesPerTurn.put("battleshipHits", battleshipHitsInTurn);
            damagesPerTurn.put("submarineHits", submarineHitsInTurn);
            damagesPerTurn.put("destroyerHits", destroyerHitsInTurn);
            damagesPerTurn.put("patrolboatHits", patrolboatHitsInTurn);
            damagesPerTurn.put("carrier", carrierDamage);
            damagesPerTurn.put("battleship", battleshipDamage);
            damagesPerTurn.put("submarine", submarineDamage);
            damagesPerTurn.put("destroyer", destroyerDamage);
            damagesPerTurn.put("patrolboat", patrolboatDamage);
            dto.add(hitsMapPerTurn);
        }
        return dto;
    }


    private Map<String, Object> getHits1(GamePlayer self, GamePlayer opponent) {
        Map<String, Object> hits = new LinkedHashMap<>();
        Map<String, Integer> contadorHits = new LinkedHashMap<>();
        Set<Ship> opponentShips = opponent.getShips();
        int cont = 0;

        for (Ship opponentShip : opponentShips) {
            List<String> opponentShipLocations = opponentShip.getLocations();
            Set<Salvo> selfSalvoes = self.getSalvoes();
            for (Salvo selfSalvo : selfSalvoes) {
                List<String> selfSalvoLocations = selfSalvo.getLocations();
                for (String salvoLocation : selfSalvoLocations) {
                    if (opponentShipLocations.stream().anyMatch(shipLocations -> shipLocations.equals(salvoLocation))) {
                        if (!contadorHits.containsKey(opponentShip.getType())) {
                            contadorHits.put(opponentShip.getType(), opponentShip.getLocations().size() - 1);
                            cont++;
                        } else {
                            contadorHits.put(opponentShip.getType(), contadorHits.get(opponentShip.getType()) - 1);
                            cont++;
                        }

                        hits.put("turn", self.getSalvoes().stream().findFirst().get().getTurn());
                        //hitsLocations mis salvos acertados en el ship del oponente
                        hits.put("hitLocations", salvoesAcertadosPlayer(self.getSalvoes(), opponent.getShips()));
                        hits.put("misSalvos", self.getSalvoes().stream()
                                .map(salvo -> salvo.getLocations())
                                .flatMap(strings -> strings.stream())
                                .collect(Collectors.toList()));
                        hits.put("misShips", self.getShips().stream().map(ship -> ship.getType() + " " + ship.getLocations()));
                        //dto.put("damages", damageHits(gamePlayer));
                        hits.put("missed", missedSalvoes(self.getSalvoes(), opponent.getShips()));
                        hits.put(opponentShip.getType().replace(" ", "") + "Hits", cont);
                        hits.put(opponentShip.getType() + " - " + "Hits para hundir al oponente", contadorHits.get(opponentShip.getType()));
                    }
                }
            }
        }

        return hits;
    }


    public int missedSalvoes(Set<Salvo> salvoes, Set<Ship> ships) {

        List<String> salvoLocations = salvoes.stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        List<String> shipLocations = ships.stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());

        List<String> salvoesAcertados = salvoLocations.stream()
                .filter(shipLocations::contains)
                .collect(Collectors.toList());

        return shipLocations.size() - salvoesAcertados.size();
    }

    public List<String> salvoesAcertadosPlayer(Set<Salvo> salvoes, Set<Ship> ships) {

        List<String> salvoLocations = salvoes.stream().map(salvo -> salvo.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());
        List<String> shipLocations = ships.stream().map(ship -> ship.getLocations()).flatMap(locations -> locations.stream()).collect(Collectors.toList());

        List<String> salvoesAcertados = salvoLocations.stream()
                .filter(shipLocations::contains)
                .collect(Collectors.toList());

        return salvoesAcertados;
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
        dto.put("gamePlayers", makeGamePlayersList(game.getGamePlayers()));
        //Task 5
        dto.put("scores", makeGamesScoresList(game.getScores()));

        return dto;
    }

    /* Itero sobre la lista anterior */
    public List<Object> makeGamePlayersList(Set<GamePlayer> gamePlayers) {
        return gamePlayers
                .stream()
                //Ordeno por GamePlayerID
                .sorted(Comparator.comparingLong(GamePlayer::getId))
                .map(gamePlayer -> makeGamePlayersDTO(gamePlayer))
                .collect(Collectors.toList());
    }

    private Map<String, Object> makeGamePlayersDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("gpid", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));

        return dto;
    }

    private Map<String, Object> makePlayerDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("id", player.getId());
        //Agregue un campo userName
        dto.put("username", player.getUserName());
        dto.put("email", player.getEmail());
        //Task 5
        dto.put("score", makeScoresListDTO(player));
        return dto;
    }


    /* ######################## DTO SHIPS ######################## */

    public List<Object> makeShipsList(Set<Ship> ships) {
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

    private List<Map<String, Object>> makeGamesSalvoesList(Game game) {
        List<Map<String, Object>> dto = new ArrayList<>();

        game.getGamePlayers().forEach(gamePlayer -> dto.addAll(makeSalvoesList(gamePlayer.getSalvoes())));

        return dto;
    }

    /* Desde game necesito todos los gameplayers, y desde gameplayer necesito todos los salvoes.
       Lista de mapas. */
    public List<Map<String, Object>> makeSalvoesList(Set<Salvo> salvoes) {
        return salvoes
                .stream()
                //Ordeno por Turno
                .sorted(Comparator.comparingInt(Salvo::getTurn))
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

    private Map<String, Object> makeScoresListDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<String, Object>();

        dto.put("name", player.getUserName());
        dto.put("total", player.getScore(player));
        dto.put("won", player.getWins(player.getScores()));
        dto.put("lost", player.getLosses(player.getScores()));
        dto.put("tied", player.getDraws(player.getScores()));

        return dto;
    }

    public List<Object> makeGamesScoresList(Set<Score> scores) {
        return scores
                .stream()
                //Ordeno por ScoreID
                .sorted(Comparator.comparingLong(Score::getId))
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

        // 1.- Si los campos del sign up estan vacios, envio respuesta FORBIDDEN.
        // 2.- Si ya hay un Player con el mismo email, envio respuesta FORBIDDEN.
        // 3.- Creo un newPlayer con el userName, email y password.
        //     Guardo newPlayer en su respectivo repositorio.
        //     Envio respuesta CREATED.

        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>(responseInfo("error", "Missing data"), HttpStatus.FORBIDDEN);
        }
        if (playerRepository.findByEmail(email) != null) {
            return new ResponseEntity<>(responseInfo("error", "Name already in use"), HttpStatus.FORBIDDEN);
        }

        Player newPlayer = new Player(userName, email, passwordEncoder.encode(password));
        playerRepository.save(newPlayer);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping(path = "/games", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);

        // 1.- Si no hay Player logueado envio respuesta FORBIDDEN.
        // 2.- Si hay Player logueado.
        //      Creo un Game y lo guardo en su respectivo repositorio.
        //      Creo un GamePlayer, lo vinculo con Game y Player, y lo guardo en su respectivo repositorio.
        //      Envio respuesta CREATED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.FORBIDDEN);
        } else {

            Date date = Date.from(java.time.ZonedDateTime.now().toInstant());
            Game newGame = new Game(date);
            gameRepository.save(newGame);

            GamePlayer newGamePlayer = new GamePlayer(newGame, authenticatedPlayer);
            gamePlayerRepository.save(newGamePlayer);

            return new ResponseEntity<>(responseInfo("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    @RequestMapping(path = "/game/{nn}/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> joinGame(@PathVariable Long nn,
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        Game game = gameRepository.findById(nn).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay Game con el Id(nn) envio respuesta FORBIDDEN.
        // 3.- Si en Game hay más de 2 jugadores envio respuesta FORBIDDEN.
        // 4.- Creo un newGamePlayer con el Game y el Player.
        //     Vinculo el Game con el newGamePlayer.
        //     Guardo el newGamePlayer en su respectivo respositorio.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (game == null) {
            return new ResponseEntity<>(responseInfo("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (game.getGamePlayers().size() >= 2) {
            return new ResponseEntity<>(responseInfo("error", "Game is full"), HttpStatus.FORBIDDEN);
        }

        GamePlayer newGamePlayer = new GamePlayer(game, authenticatedPlayer);
        game.addGame(newGamePlayer);
        gamePlayerRepository.save(newGamePlayer);

        return new ResponseEntity<>(responseInfo("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/ships", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addShips(@PathVariable Long gamePlayerId,
                                                        @RequestBody Set<Ship> ships, //Cuerpo de la Request
                                                        Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay GamePlayer con el Id(gamePlayerId) envio respuesta UNAUTHORIZED.
        // 3.- Verifico que GamePlayer sea el mismo del authenticatedPlayer.
        //  3.1.- Verifico si el GamePlayer ya tiene vinculados sus Ships.
        //        Si no los tiene, vinculo Ships con GamePlayer.
        //        Guardo los Ships en su respectivo respositorio.
        //        Envio respuesta CREATED.
        //  3.2.- Si los tiene, envio respuesta FORBIDDEN.
        // 4.- Si no lo es, envio respuesta UNAUTHORIZED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "No such gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        if (correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            if (gamePlayer.getShips().isEmpty()) {
                ships.forEach(ship -> ship.setGamePlayer(gamePlayer));
                gamePlayer.setShips(ships);
                shipRepository.saveAll(ships);

                return new ResponseEntity<>(responseInfo("ok", "Ships saved"), HttpStatus.CREATED);

            } else {
                return new ResponseEntity<>(responseInfo("error", "Player already has ships"), HttpStatus.FORBIDDEN);
            }

        } else {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        }
    }

    @RequestMapping(path = "/games/players/{gamePlayerId}/salvoes", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addSalvoes(@PathVariable Long gamePlayerId,
                                                          @RequestBody Salvo salvo,
                                                          Authentication authentication) {
        authentication = SecurityContextHolder.getContext().getAuthentication();
        Player authenticatedPlayer = getAuthenticationPlayer(authentication);
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        // 1.- Si no hay Player logueado envio respuesta UNAUTHORIZED.
        // 2.- Si no hay GamePlayer con el Id(gamePlayerId) envio respuesta UNAUTHORIZED.
        // 3.- Verifico que GamePlayer sea el mismo del authenticatedPlayer.
        //  3.1.- Verifico si el GamePlayer ya ha disparado su Salvo.
        //        Si ha disparado, envio respuesta FORBIDDEN.
        //  3.2.- Si no ha disparado, vinculo Salvo con GamePlayer.
        //        Guardo el Salvo en su respectivo respositorio.
        //        Envio respuesta CREATED.
        // 4.- Si no lo es, envio respuesta UNAUTHORIZED.

        if (authenticatedPlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "You need to be logged first"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer == null) {
            return new ResponseEntity<>(responseInfo("error", "No gamePlayerID given"), HttpStatus.UNAUTHORIZED);
        }
        if (correctGamePlayer(gamePlayer, authenticatedPlayer)) {
            // Hago un filter y count para saber si el Turn no esta repetido.
            if (gamePlayer.getSalvoes().stream().filter(s -> s.getTurn() == salvo.getTurn()).count() == 1) {
                return new ResponseEntity<>(responseInfo("error", "Player already has the salvo"), HttpStatus.FORBIDDEN);

            } else {
                gamePlayer.getSalvoes().add(salvo);
                salvoRepository.save(salvo);

                return new ResponseEntity<>(responseInfo("ok", "Salvoes saved"), HttpStatus.CREATED);
            }

        } else {
            return new ResponseEntity<>(responseInfo("error", "Wrong gamePlayerID"), HttpStatus.UNAUTHORIZED);

        }
    }
}
