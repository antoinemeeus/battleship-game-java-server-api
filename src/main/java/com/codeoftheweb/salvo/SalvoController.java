package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GamePlayerRepository gamePlayerRepository;
    @Autowired
    private ShipRepository shipRepository;
    @Autowired
    private SalvoRepository salvoRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    //Check if Logging
    @PostMapping("/checkLog")
    ResponseEntity<Object> checkLog(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>("User is not logged in", HttpStatus.UNAUTHORIZED);
        } else {
            return new ResponseEntity<>("User is logged in", HttpStatus.OK);
        }

    }

    //Request player
    @GetMapping("/player")
    Map<String, Object> getPlayer(Authentication authentication) {
        Player player = getLoggedPlayer(authentication);
        Map<String, Object> dto = makePlayerDTO(player);
        Map<String, Object> dto2 = makeFinishedGameInfo(player);
        Map<String, Object> finalDTO = new LinkedHashMap<>(dto);
        finalDTO.putAll(dto2);
        return finalDTO;
    }

    //Requests games
    @GetMapping("/games")
    Map<String, Object> getGames(Authentication authentication) {
//        System.out.println("LoggedUser: " + getLoggedPlayer(authentication));
        Map<String, Object> loggedPlayerAndGamesDTO = new LinkedHashMap<>();
        Set<Game> games = new LinkedHashSet<>(gameRepository.findAll());
        loggedPlayerAndGamesDTO.put("games", games.stream().map(this::makeGameDTO).collect(toList()));
        loggedPlayerAndGamesDTO.put("player", makePlayerDTO(getLoggedPlayer(authentication)));
        return loggedPlayerAndGamesDTO;
    }

    //Create game
    @PostMapping("/games")
    ResponseEntity<Map<String, Object>> createGame(Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You need to be logged in to create a new Game! Please Log in or Sign up."), HttpStatus.UNAUTHORIZED);
        } else {
            Game newGame = new Game();
            Player newPlayer = getLoggedPlayer(authentication);
            GamePlayer newGamePlayer = new GamePlayer(newGame, newPlayer);
            gameRepository.save(newGame);
            gamePlayerRepository.save(newGamePlayer);
            return new ResponseEntity<>(createMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
        }
    }

    //Join game
    @PostMapping("/game/{game_id}/players")
    ResponseEntity<Map<String, Object>> createGame(@PathVariable Long game_id, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You need to be logged in to join a Game! Please Log in or Sign up."), HttpStatus.UNAUTHORIZED);
        }

        Game currentGame = gameRepository.findById(game_id).orElse(null);

        if (currentGame == null) {
            return new ResponseEntity<>(createMap("error", "No such game"), HttpStatus.FORBIDDEN);
        }
        if (currentGame.getGamePlayers().size() != 1) {
            return new ResponseEntity<>(createMap("error", "Game is already full"), HttpStatus.FORBIDDEN);
        }
        Player currentPlayer = getLoggedPlayer(authentication);
        GamePlayer newGamePlayer = new GamePlayer(currentGame, currentPlayer);
        gamePlayerRepository.save(newGamePlayer);
        return new ResponseEntity<>(createMap("gpid", newGamePlayer.getId()), HttpStatus.CREATED);
    }

    //Place Ships
    @PostMapping("/games/players/{gamePlayerId}/ships")
    public ResponseEntity<Map<String, Object>> setShips(@PathVariable Long gamePlayerId, @RequestBody List<Ship> ships, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You need to be logged in to place Ships! Please Log in or Sign up."), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(createMap("error", "This GamePlayer doesn't exist"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getPlayer().getId() != getLoggedPlayer(authentication).getId()) {
            return new ResponseEntity<>(createMap("error", "You are not authorized to place ships with this gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getShips().size() >= 5) {
            return new ResponseEntity<>(createMap("error", "Your ships are already placed!"), HttpStatus.FORBIDDEN);
        }
        if (ships.size() < 5) {
            return new ResponseEntity<>(createMap("error", "Some ships placements are missing!"), HttpStatus.FORBIDDEN);
        }
        if (!isShipLocationLegal(ships)) {
            return new ResponseEntity<>(createMap("error", "Illegal ship locations detected. Make sure no ship overlaps and are only in vertical or horizontal orientation"), HttpStatus.FORBIDDEN);
        }
        for (Ship ship : ships) {
            ship.setGamePlayer(gamePlayer);
            shipRepository.save(ship);
        }
        return new ResponseEntity<>(createMap("OK", "Ship positions saved successfully! "), HttpStatus.CREATED);
    }

    @PostMapping("/games/players/{gamePlayerId}/is_OFFLINE")
    public ResponseEntity<Map<String, Object>> setAFK(@PathVariable Long gamePlayerId, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You need to be logged in to fire salvoes! Please Log in or Sign up."), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        if (gamePlayer == null) {
            return new ResponseEntity<>(createMap("error", "This GamePlayer doesn't exist"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getPlayer().getId() != getLoggedPlayer(authentication).getId()) {
            return new ResponseEntity<>(createMap("error", "You are not authorized to send this request [set_OFFLINE] for this gamePlayer"), HttpStatus.UNAUTHORIZED);
        }
        gamePlayer.setAfk(true);
        gamePlayerRepository.save(gamePlayer);
        return new ResponseEntity<>(createMap("OK", "GamePlayer [" + gamePlayerId + "] set to OFFLINE"), HttpStatus.CREATED);


    }

    //Place Salvoes
    @PostMapping("/games/players/{gamePlayerId}/salvos")
    public ResponseEntity<Map<String, Object>> setSalvoes(@PathVariable Long gamePlayerId, @RequestBody Salvo salvo, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You need to be logged in to fire salvoes! Please Log in or Sign up."), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayerId).orElse(null);

        if (gamePlayer == null) {
            return new ResponseEntity<>(createMap("error", "This GamePlayer doesn't exist"), HttpStatus.UNAUTHORIZED);
        }

        if (gamePlayer.getPlayer().getId() != getLoggedPlayer(authentication).getId()) {
            return new ResponseEntity<>(createMap("error", "You are not authorized to fire salvoes with this gamePlayer"), HttpStatus.UNAUTHORIZED);
        }

        Game currentGame = gamePlayer.getGame();
        Map<String, String> gameStateObj = makeGameState(gamePlayer);
        String gameStatus = gameStateObj.get("Status");
        String gameInfo = gameStateObj.get("Info");

        //Pre check if game is finished
        if (!gameStatus.equals("GO")) {
            return new ResponseEntity<>(createMap("error", "Not your turn or the Game is finished with: " + gameInfo), HttpStatus.FORBIDDEN);
        }

        GamePlayer opponent = getOpponent(gamePlayer);
        Instant updateDate = Instant.now();

        int gpTurn = gamePlayer.getSalvoes().size() + 1;
        int opTurn = opponent.getSalvoes().size() + 1;

        if (gpTurn > opTurn + 1) {
            System.out.println("SALVO SIZE BETWEEN PLAYER  GP:" + gamePlayer.getSalvoes() + " opponent: " + opponent.getSalvoes());
            return new ResponseEntity<>(createMap("error", "Salvo number discrepancies. You can't sent more than one salvo per turn. Wait for opponent to play."), HttpStatus.FORBIDDEN);
        }

        salvo.setTurnNumber(gpTurn);

        //Sanity check that the new salvo turn number is not already present in the gamePlayer salvoes.
        // Can happen when player update quickly salvoes before Spring can save database.
//        List<Salvo> previousSalvoes = new ArrayList<>(gamePlayer.getSalvoes());
//        Salvo SalvoOfTurnAlreadySet = previousSalvoes.stream().filter(s -> s.getTurnNumber().equals(gpTurn)).findFirst().orElse(null);
//        if(SalvoOfTurnAlreadySet!=null){
//            System.out.println("SALVO PRESENT IN PREVIOUS TURNS?  INCONSISTENT:"+SalvoOfTurnAlreadySet);
//            return new ResponseEntity<>(createMap("error", "This turn already have a salvo. Can't save " + salvo+" for this turn "+gpTurn), HttpStatus.FORBIDDEN);
//        }
        salvo.setGamePlayer(gamePlayer);
        gamePlayer.addSalvo(salvo);
        gamePlayer.setLastPlayedDate(updateDate);

        salvoRepository.save(salvo);


        //Re-check Game status after new salvo saved
        applyScore(makeGameState(gamePlayer), currentGame, gamePlayer, opponent);
//        switch (gameStatus) {
//            case "WON":
//                Score userIsWinner = new Score(currentGame, gamePlayer.getPlayer(), 1.0);
//                Score opponentIsLooser = new Score(currentGame, opponent.getPlayer(), 0.0);
//                scoreRepository.save(userIsWinner);
//                scoreRepository.save(opponentIsLooser);
//                break;
//            case "LOST":
//                Score opponentIsWinner = new Score(currentGame, opponent.getPlayer(), 1.0);
//                Score userIsLooser = new Score(currentGame, gamePlayer.getPlayer(), 0.0);
//                scoreRepository.save(opponentIsWinner);
//                scoreRepository.save(userIsLooser);
//                break;
//            case "TIED":
//                Score userTied = new Score(currentGame, gamePlayer.getPlayer(), 0.5);
//                Score opponentTied = new Score(currentGame, opponent.getPlayer(), 0.5);
//                scoreRepository.save(userTied);
//                scoreRepository.save(opponentTied);
//                break;
//            case "GO":
//                break;
//            case "WAIT":
//                break;
//        }

        return new ResponseEntity<>(createMap("OK", "Salvo positions saved successfully! "), HttpStatus.CREATED);
    }


    //GamePlayer information
    @GetMapping("/game_view/{gamePlayer_id}")
    ResponseEntity<Map<String, Object>> getGameView(@PathVariable Long gamePlayer_id, Authentication authentication) {
        if (authentication == null) {
            return new ResponseEntity<>(createMap("error", "You're Not Logged In!"), HttpStatus.UNAUTHORIZED);
        }
        GamePlayer gamePlayer = gamePlayerRepository.findById(gamePlayer_id).orElse(null);
        if (gamePlayer == null) {
            return new ResponseEntity<>(createMap("error", "This GamePlayer doesn't exist"), HttpStatus.UNAUTHORIZED);
        }
        if (gamePlayer.getPlayer().getId() == getLoggedPlayer(authentication).getId()) {

            //Check if opponent is timeout
            GamePlayer opponent = getOpponent(gamePlayer);
            if (opponent != null) {

                Duration timeDisconnected = Duration.between(Instant.now(), opponent.getLastConnected());
//                System.out.println("TIME DISCONNECTED: " + timeDisconnected.abs().toMinutes());
                if (timeDisconnected.abs().toMinutes() > 3) {
                    //Opponent didn't connect since 3 minutes, games finishes with opponent losing.
                    opponent.setAfk(true);
                    gamePlayerRepository.save(opponent);
                }
                applyScore(makeGameState(gamePlayer), gamePlayer.getGame(), gamePlayer, opponent);
            }
            //set time of connection relative to request to this gp id.
            gamePlayer.setLastConnected(Instant.now());
            gamePlayerRepository.save(gamePlayer);
            //Apply score if any

            return new ResponseEntity<>(makeGameView(gamePlayer), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(createMap("error", "Game Data access restricted. Nice try but no cheating..."), HttpStatus.UNAUTHORIZED);
        }

    }

    //Players
    @GetMapping("/players")
    List<Object> getPlayersInfo() {
        Set<Player> players = new LinkedHashSet<>(playerRepository.findAll());
        return players.stream().map(this::makePlayerDTO).collect(toList());
    }

    //Player SignIn
    @PostMapping("/players")
    ResponseEntity<Object> registerPlayer(@RequestParam Integer avatarID, @RequestParam String userName, @RequestParam String email, @RequestParam String password) {
//        System.out.println("userName: " + userName + "email: " + email + "pwd: " + password);
        if (userName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Missing data", HttpStatus.FORBIDDEN);
        }
        //Check if userName or email is already taken in database
        if (playerRepository.findByEmail(email) != null) {
            return new ResponseEntity<>(createMap("error", "Email already taken"), HttpStatus.FORBIDDEN);
        }

        playerRepository.save(new Player(avatarID, userName, email, passwordEncoder.encode(password)));
        return new ResponseEntity<>(HttpStatus.CREATED);

    }


    //Method return logged user
    private Player getLoggedPlayer(Authentication authentication) {
        if (authentication != null) {
            return playerRepository.findByEmail(authentication.getName());
        }
        return null;
    }

    //Object for gameView/?gp={id}
    private Map<String, Object> makeGameView(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        Game game = gamePlayer.getGame();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("lastPlayed", gamePlayer.getLastPlayedDate());
        dto.put("lastConnected", gamePlayer.getLastConnected());
        dto.put("gameState", makeGameState(gamePlayer));
        dto.put("gamePlayers", game.getGamePlayers().stream().map(this::makeGamePlayersDTO).collect(toList()));
        dto.put("scores", game.getScores().stream().map(this::makeScoresDTO).collect(toList()));
        dto.put("hits", makeHitsDto(gamePlayer));
        dto.put("ships", gamePlayer.getShips().stream().map(this::makeShipDTO).collect(toList()));
        dto.put("salvoes", game.getGamePlayers()
                .stream()
                .collect(Collectors.toMap(gp -> gp.getPlayer().getId(), gp -> gp.getSalvoes().
                        stream().
                        collect(Collectors.toMap(Salvo::getTurnNumber, Salvo::getSalvoLocations, (address1, address2) -> {
                            System.out.println("duplicate key found! newval:" + address1 + "oldval: " + address2);
                            if (address1.size() >= address2.size()) {
                                return address1;
                            } else {
                                return address2;
                            }

                        })))));
        return dto;
    }

    //Object to make GameState
    private Map<String, String> makeGameState(GamePlayer gamePlayer) {
        Map<String, String> dto = new HashMap<>();

        if (getOpponent(gamePlayer) == null || gamePlayer.getGame().getGamePlayers().size() == 1) {
            //No opponent detected
            dto.put("Status", "WAIT");
            dto.put("code", "0");
            dto.put("Info", "Waiting for opponent to join game...");
            return dto;
        }
        if (gamePlayer.getShips().size() == 0 || gamePlayer.getShips().size() < 5) {
            //Ships are not place yet, placingShip mode
            dto.put("Status", "WAIT");
            dto.put("code", "1");
            dto.put("Info", "Waiting for user to place ships...");
            return dto;
        }

        if (gamePlayer.getGame().getGamePlayers().size() == 2) {

            GamePlayer opponent = getOpponent(gamePlayer);

            //Check Opponent Ship placement
            if (opponent.getShips().size() == 0 || opponent.getShips().size() < 5) {
                dto.put("Status", "WAIT");
                dto.put("code", "2");
                dto.put("Info", "Waiting for opponent to place ships");
                return dto;
            }

            //Check AFK Situation
            /*---------------------------------*/
            //Check if tied in AFK situation /*---------------------------------*/

            if (gamePlayer.getAfk() && opponent.getAfk()) {
                dto.put("Status", "TIED");
                dto.put("code", "5");
                dto.put("Info", "You both went AFK, you tied...");
                return dto;
            }
            //Check if current gamePlayer is AFK
            if (gamePlayer.getAfk() && !opponent.getAfk()) {
                dto.put("Status", "LOST");
                dto.put("code", "5");
                dto.put("Info", "You skipped too many rounds, you lost the game!");
                return dto;
            }
            //Check if opponent gamePlayer is AFK
            if (opponent.getAfk() && !gamePlayer.getAfk()) {
                dto.put("Status", "WON");
                dto.put("code", "5");
                dto.put("Info", "Opponent took too much time to play, you won the game!");
                return dto;
            }
            /*---------------------------------*/

            //Players have finished firing salvoes
            if (gamePlayer.getSalvoes().size() == opponent.getSalvoes().size()) {
                Boolean playerAllShipSunk = areAllShipsSunk(gamePlayer, opponent);
                Boolean opponentAllShipSunk = areAllShipsSunk(opponent, gamePlayer);



                //Check if current player WON against opponent
                if (!playerAllShipSunk && opponentAllShipSunk) {
                    dto.put("Status", "WON");
                    dto.put("code", "5");
                    dto.put("Info", "You won the game");
                    return dto;
                }
                //Check if current player LOST against opponent
                if (playerAllShipSunk && !opponentAllShipSunk) {
                    dto.put("Status", "LOST");
                    dto.put("code", "5");
                    dto.put("Info", "You lost the game");
                    return dto;
                }
                //Check if current player TIED against opponent
                if (playerAllShipSunk && opponentAllShipSunk) {
                    dto.put("Status", "TIED");
                    dto.put("code", "5");
                    dto.put("Info", "You tied with the opponent");
                    return dto;
                }
                //No winner, loser or ties? Game continues for both players
                if (!playerAllShipSunk && !opponentAllShipSunk) {
                    dto.put("Status", "GO");
                    dto.put("code", "4");
                    dto.put("Info", "Place and fire your salvo!");
                    return dto;
                }

            }
            //Opponent has finished firing Salvoes, you continue to play
            if (gamePlayer.getSalvoes().size() < opponent.getSalvoes().size()) {
                dto.put("Status", "GO");
                dto.put("code", "4");
                dto.put("Info", "Opponent is waiting for you! Place and fire your salvo!");
                return dto;
            }
            //Player has finishes firing Salvoes, waiting for opponent
            if (gamePlayer.getSalvoes().size() > opponent.getSalvoes().size()) {
                dto.put("Status", "WAIT");
                dto.put("code", "3");
                dto.put("Info", "Opponent is choosing it's targets and preparing to fire it's salvo!");
                return dto;
            }

        } else {
            dto.put("Status", "ERROR");
            dto.put("code", "-1");
            dto.put("Info", "The numbers of players is inconsistent, only 2 players can play this game");
            return dto;
        }

        dto.put("Status", "UNDEFINED");
        dto.put("code", "-1");
        dto.put("Info", "Undefined error, game status is undefined");
        return dto;
    }

    //Method to apply scores
    private void applyScore(Map<String, String> gameState, Game currentGame, GamePlayer gamePlayer, GamePlayer opponent) {
        String gameStatus = gameState.get("Status");
        System.out.println("APPLYING SCORE: ");
        if (gamePlayer.getPlayer().getScore(gamePlayer.getGame()) != null) {
            //Score is not null, so already exist, don't apply new score.
            return;
        }
        switch (gameStatus) {
            case "WON":
                Score userIsWinner = new Score(currentGame, gamePlayer.getPlayer(), 1.0);
                Score opponentIsLooser = new Score(currentGame, opponent.getPlayer(), 0.0);
                scoreRepository.save(userIsWinner);
                scoreRepository.save(opponentIsLooser);
                break;
            case "LOST":
                Score opponentIsWinner = new Score(currentGame, opponent.getPlayer(), 1.0);
                Score userIsLooser = new Score(currentGame, gamePlayer.getPlayer(), 0.0);
                scoreRepository.save(opponentIsWinner);
                scoreRepository.save(userIsLooser);
                break;
            case "TIED":
                Score userTied = new Score(currentGame, gamePlayer.getPlayer(), 0.5);
                Score opponentTied = new Score(currentGame, opponent.getPlayer(), 0.5);
                scoreRepository.save(userTied);
                scoreRepository.save(opponentTied);
                break;
            case "GO":
                break;
            case "WAIT":
                break;
        }
    }

    //Object format for finishedGames
    private Map<String, Object> makeFinishedGameInfo(Player player) {
        Set<GamePlayer> gamePlayers = new LinkedHashSet<>(player.getGamePlayers());
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("finishedGames", gamePlayers.stream().map(gp -> makeScoreByGame(gp, player)).collect(toList()));
        return dto;
    }

    private Map<String, Object> makeScoreByGame(GamePlayer gamePlayer, Player player) {
        Score scoreByGame = player.getScore(gamePlayer.getGame());
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("gameScore", scoreByGame);
        map1.put("opponentPlayer", makeOpponentPlayerDTO(gamePlayer));
        return map1;
    }

    private Map<String, Object> makeOpponentPlayerDTO(GamePlayer currentGamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        GamePlayer opponentGP = getOpponent(currentGamePlayer);
        if (opponentGP != null) {
            Player opponentPlayer = opponentGP.getPlayer();
            dto.put("avatarID", opponentPlayer.getAvatarID());
            dto.put("id", opponentPlayer.getId());
            dto.put("userName", opponentPlayer.getUserName());
            dto.put("email", opponentPlayer.getEmail());
        }
        return dto;
    }

    //Object format for ships
    private Map<String, Object> makeShipDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //Object format for games
    private Map<String, Object> makeGameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game.getId());
        dto.put("created", game.getCreationDate());
        dto.put("gamePlayers", game.getGamePlayers().stream().sorted(Comparator.comparingLong(GamePlayer::getId)).map(this::makeGamePlayersDTO).collect(toList()));
        dto.put("scores", game.getScores().stream().map(this::makeScoresDTO).collect(toList()));

        return dto;
    }

    //Object format for gamePlayers
    private Map<String, Object> makeGamePlayersDTO(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", gamePlayer.getId());
        dto.put("player", makePlayerDTO(gamePlayer.getPlayer()));
        dto.put("lastPlayedDate", gamePlayer.getLastPlayedDate());
        dto.put("lastConnectedDate", gamePlayer.getLastConnected());
        return dto;
    }

    //Object format for player
    private Map<String, Object> makePlayerDTO(Player player) {
        if (player != null) {
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("avatarID", player.getAvatarID());
            dto.put("id", player.getId());
            dto.put("userName", player.getUserName());
            dto.put("email", player.getEmail());
            dto.put("score", makePlayerScoreDTO(player));
            return dto;
        }

        return null;
    }


    //Object format for scores
    private Map<String, Object> makeScoresDTO(Score score) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("playerID", score.getPlayer().getId());
        dto.put("score", score.getScore());
        dto.put("finishDate", score.getGameFinishedDate());
        return dto;
    }

    private Map<String, Object> makePlayerScoreDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        List<Double> scoreList = player.getScores().stream().map(Score::getScore).collect(toList());
        Double total = scoreList.stream().reduce(0.0, Double::sum);
        dto.put("total", total);
        dto.put("won", Collections.frequency(scoreList, 1.0));
        dto.put("lost", Collections.frequency(scoreList, 0.0));
        dto.put("tied", Collections.frequency(scoreList, 0.5));
        return dto;
    }

    //Object for hits

    private Map<String, Object> makeHitsDto(GamePlayer gamePlayer) {
        Map<String, Object> dto = new LinkedHashMap<>();
        GamePlayer opponentPlayer = this.getOpponent(gamePlayer);
        if (opponentPlayer != null) {
            dto.put("user", makeHitsAndMiss(gamePlayer.getShips(), opponentPlayer.getSalvoes(), true));
            dto.put("opponent", makeHitsAndMiss(opponentPlayer.getShips(), gamePlayer.getSalvoes(), false));
        }
        return dto;
    }

    private Map<Integer, Map<String, Object>> makeHitsAndMiss(Set<Ship> ships, Set<Salvo> oppositeSalvoSet, Boolean isUser) {
        Map<Integer, Map<String, Object>> userHitsAndMiss = new LinkedHashMap<>();
        //Transform set to list and sort by turn number
        List<Salvo> oppositeSalvoListSorted = new ArrayList<>(oppositeSalvoSet);
        oppositeSalvoListSorted.sort(Comparator.comparingInt(Salvo::getTurnNumber));

        Set<String> accumulatedSalvoesSet = new HashSet<>();
        Set<String> accumulatedHitSalvoesSet = new HashSet<>();
        Set<String> accumulatedMissedSalvoesSet = new HashSet<>();

        oppositeSalvoListSorted.forEach(salvo -> {
            Map<String, Object> hitHistoryDto = new LinkedHashMap<>();
            Map<String, Object> fleetDto = new LinkedHashMap<>();
            Set<String> hitsOnPlayerPerTurn = new LinkedHashSet<>();
            Set<String> shipMissed = new LinkedHashSet<>(salvo.getSalvoLocations());
            accumulatedSalvoesSet.addAll(salvo.getSalvoLocations());

            ships.forEach(ship -> {
                Set<String> accumulatedShipHits = new LinkedHashSet<>(accumulatedSalvoesSet);
                List<String> shipHits = new ArrayList<>(salvo.getSalvoLocations());
                shipHits.retainAll(ship.getLocations());
                hitsOnPlayerPerTurn.addAll(shipHits);
                accumulatedShipHits.retainAll(ship.getLocations());

                fleetDto.put(ship.getType(), makeFleet(ship, accumulatedShipHits, isUser));
            });
            shipMissed.removeAll(hitsOnPlayerPerTurn);
            accumulatedMissedSalvoesSet.addAll(shipMissed);
            accumulatedHitSalvoesSet.addAll(hitsOnPlayerPerTurn);

            hitHistoryDto.put("fleetState", fleetDto);
            hitHistoryDto.put("hitsLoc", hitsOnPlayerPerTurn);
            hitHistoryDto.put("missLoc", shipMissed);
            hitHistoryDto.put("AllHits", accumulatedHitSalvoesSet);
            hitHistoryDto.put("AllMissed", accumulatedMissedSalvoesSet);

            userHitsAndMiss.put(salvo.getTurnNumber(), hitHistoryDto);


        });
        return userHitsAndMiss;
    }

    private Map<String, Object> makeFleet(Ship ship, Set<String> hits, Boolean showAll) {
        Map<String, Object> dto = new LinkedHashMap<>();
        Boolean sunk = hits.size() >= ship.getLocations().size();
        dto.put("sunk", sunk);
        if (showAll) {
            dto.put("damage", hits.size());
            dto.put("hitsLocation", hits);
        }
        return dto;
    }

    //----------UTILITIES -  METHODS-----------
    //Method to find opponent gamePlayer
    private GamePlayer getOpponent(GamePlayer currentGamePlayer) {
        Game game = currentGamePlayer.getGame();
        return game.getGamePlayers()
                .stream()
                .filter(gp -> !gp.getId().equals(currentGamePlayer.getId()))
                .findFirst()
                .orElse(null);

    }

    //Method to quickly make Map<String,Object>
    private Map<String, Object> createMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    //Method to check if ships are in a legal position:
    // -No overlapping
    // -No diagonals positions
    private Boolean isShipLocationLegal(List<Ship> ships) {
        List<String> positionsFlatList = new ArrayList<>();

        for (Ship ship : ships) {
            //For each ship, check diagonal position
            List<String> shipLocations = ship.getLocations();
            List<String> rowLoc = new ArrayList<>();
            List<String> colLoc = new ArrayList<>();
            for (String eachLocation : shipLocations) {
                positionsFlatList.add(eachLocation);
                rowLoc.add(eachLocation.replaceAll("[^0-9 ]", ""));
                colLoc.add(eachLocation.replaceAll("[^A-Z ]", ""));
            }
            //If rowLoc has different value inside list, check colLoc
            //If colLoc has different value inside list, then the position is diagonal.
            Boolean isRowUnique = sizeOfDuplicatesSet(rowLoc) == 1;
            Boolean isColUnique = sizeOfDuplicatesSet(colLoc) == 1;
            if (!isRowUnique && !isColUnique) {
                //rowLoc has different value and colLoc has different value, ILLEGAL
                return false;
            }
            //If rowLoc has same value inside list, check if colLoc
            if (isRowUnique && isColUnique) {
                //rowLoc has only one value and colLoc has only one value, ILLEGAL
                return false;
            }
            //If rowLoc has same value inside list, check if colLoc is a set of following alphabet
            if (!isColUnique) {
                if (!isIncreasingByOne(colLoc, true)) return false;
            }
            //If colLoc  has same value inside list, check if rowLoc is a set of following number
            if (!isRowUnique) {
                if (!isIncreasingByOne(rowLoc, false)) return false;
            }
        }
        //If flatList contains duplicates, ships are overlapping
        return (sizeOfDuplicatesSet(positionsFlatList) == positionsFlatList.size());

    }

    //Method that check if an array of string's number is increasing in order and by 1
    private boolean isIncreasingByOne(List<String> stringList, Boolean isListOfNames) {
        List<Integer> intList = new ArrayList<>();
        if (isListOfNames) {
            intList.addAll(stringList.stream().map(this::rowNameToNumber).sorted().collect(Collectors.toList()));
        } else {
            intList.addAll(stringList.stream().map(Integer::parseInt).sorted().collect(Collectors.toList()));
        }
        //sort array (sanity check if array)
        for (int i = 1; i < intList.size(); i++) {
            Integer x = intList.get(i - 1);
            Integer y = intList.get(i) - 1;
            if (intList.get(i - 1) != intList.get(i) - 1)
                return false;
        }
        return true;
    }

    //Method to return true/false if all ships are sunk
    private Boolean areAllShipsSunk(GamePlayer gamePlayer, GamePlayer opponentPlayer) {
        Set<Salvo> opponentPlayerSalvoes = new LinkedHashSet<>(opponentPlayer.getSalvoes());
        List<String> allOpponentSalvoesLocation = opponentPlayerSalvoes.stream().map(Salvo::getSalvoLocations).flatMap(List::stream).collect(toList());
        for (Ship ship : gamePlayer.getShips()) {
            List<String> shipHits = new ArrayList<>(allOpponentSalvoesLocation);
            shipHits.retainAll(ship.getLocations());
            boolean isSunk = shipHits.size() >= ship.getLocations().size();
            //If one ship of the fleet is not sunk, the game is not finished -> return false
            if (!isSunk) {
                return false;
            }
        }
        return true;
    }

    //Private method to check if array contains only duplicates
    private Integer sizeOfDuplicatesSet(List<String> array) {
        Set<String> set = new HashSet<>(array);
        return set.size();
    }


    //Method to get coordinate in number from Rowname (Excel format)
    private int rowNameToNumber(String name) {
        int number = 0;
        for (int i = 0; i < name.length(); i++) {
            number = number * 26 + (name.charAt(i) - ('A' - 1));
        }
        return number;
    }
}


