package com.myproject.caro_game.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Move;
import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.res.UserRoomResponse;

@Service
public class GameService {
    private final Map<String, Game> games = new HashMap<>();

    public UserRoomResponse createRoom(String playerName) {
        try {
            String zoomId = UUID.randomUUID().toString();
            Game game = new Game();
            Player player1 = new Player(playerName, 'X');
            game.addPlayer(player1);
            games.put(zoomId, game);
            return new UserRoomResponse(zoomId, player1.getUserId(), "PLAYER_1");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public Game getGame(String zoomId) {
        return games.get(zoomId);
    }

    public UserRoomResponse joinToRoom(String zoomId, String playerName) {
        try {
            Game game = games.get(zoomId);
            if (game == null)
                throw new Exception("Room not found");
            Player player2 = new Player(playerName, 'O');
            game.addPlayer(player2);
            return new UserRoomResponse(zoomId, player2.getUserId(), "PLAYER_2");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean placeMove(String zoomId, Move move) {
        Game game = games.get(zoomId);
        if (game == null)
            return false;
        Player current = game.getCurrentPlayer();
        if (!current.getUserId().equals(move.userId()))
            return false;

        boolean success = game.getBoard().placeMove(move.point(), move.symbol());
        if (success)
            game.switchPlayer();
        return success;
    }

    public boolean startGame(String zoomId, String userHostId, boolean firstTurn) {
        try {
            Game game = games.get(zoomId);
            if (game == null)
                throw new Exception("Room not found");

            // check has 2 players
            if (game.getPlayers().size() < 2)
                throw new Exception("Not enough players");

            // Tìm hostPlayer
            Player hostPlayer = game.getPlayers().stream()
                    .filter(p -> p.getUserId().equals(userHostId))
                    .findFirst()
                    .orElse(null);

            if (hostPlayer == null)
                throw new Exception("Host player not found");

            Player otherPlayer = game.getPlayers().stream()
                    .filter(p -> !p.getUserId().equals(userHostId))
                    .findFirst()
                    .orElse(null);

            if (otherPlayer == null)
                throw new Exception("Second player not found");
           
            Player firstPlayer = firstTurn ? hostPlayer : otherPlayer;
            game.setCurrentPlayer(firstPlayer);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // /**
    // * Kiểm tra thắng cơ bản 5 ô liên tiếp
    // */
    // private boolean checkWin(int x, int y, char symbol) {
    // return countContinuous(x, y, symbol, 1, 0) + countContinuous(x, y, symbol,
    // -1, 0) - 1 >= 5 // ngang
    // || countContinuous(x, y, symbol, 0, 1) + countContinuous(x, y, symbol, 0, -1)
    // - 1 >= 5 // dọc
    // || countContinuous(x, y, symbol, 1, 1) + countContinuous(x, y, symbol, -1,
    // -1) - 1 >= 5 // chéo \
    // || countContinuous(x, y, symbol, 1, -1) + countContinuous(x, y, symbol, -1,
    // 1) - 1 >= 5; // chéo /
    // }

    // /**
    // * Đếm số ô liên tiếp theo hướng dx, dy
    // */
    // private int countContinuous(int x, int y, char symbol, int dx, int dy) {
    // int count = 0;
    // int nx = x, ny = y;
    // while (true) {
    // Character c = game.getBoard().getSymbolAt(nx, ny);
    // if (c != null && c == symbol) {
    // count++;
    // nx += dx;
    // ny += dy;
    // } else {
    // break;
    // }
    // }
    // return count;
    // }
}
