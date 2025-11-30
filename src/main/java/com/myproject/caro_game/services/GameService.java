package com.myproject.caro_game.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.myproject.caro_game.models.Game;
import com.myproject.caro_game.models.Move;
import com.myproject.caro_game.models.Player;
import com.myproject.caro_game.models.Point;
import com.myproject.caro_game.models.res.MoveResult;
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
                throw new Exception("Room's not exist!");
            Player player2 = new Player(playerName, 'O');
            game.addPlayer(player2);
            return new UserRoomResponse(zoomId, player2.getUserId(), "PLAYER_2");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public MoveResult placeMove(String zoomId, Move move) {
        Game game = games.get(zoomId);
        if (game == null) return new MoveResult(false, null);

        Player current = game.getCurrentPlayer();
        if (!current.getUserId().equals(move.userId()))
            return new MoveResult(false, null);

        Point point = new Point(move.x(), move.y());
        char symbol = current.getSymbol();
        boolean success = game.getBoard().placeMove(point, symbol);

        if (!success) {
            return new MoveResult(false, null);
        }

        // Check win at placed point
        boolean win = checkWin(point.x(), point.y(), symbol, game);
        if (win) {
            return new MoveResult(true, current.getUserId());
        }

        // No win: switch player and continue
        game.switchPlayer();
        return new MoveResult(true, null);
    }

    public boolean startGame(String zoomId, String userHostId, boolean firstTurn) {
        try {
            Game game = games.get(zoomId);
            if (game == null)
                throw new Exception("Room's not exist!");

            // check has 2 players
            if (game.getPlayers().size() < 2)
                throw new Exception("Players are not enough to start the game");

            // Find hostPlayer
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

    /**
     * Basic win test: 5 consecutive boxes (collected in 4 directions)
     */
    private boolean checkWin(int x, int y, char symbol, Game game) {
        return countContinuous(x, y, symbol, 1, 0, game) + countContinuous(x, y, symbol, -1, 0, game) - 1 >= 5 // horizontal
                || countContinuous(x, y, symbol, 0, 1, game) + countContinuous(x, y, symbol, 0, -1, game) - 1 >= 5 // vertical
                || countContinuous(x, y, symbol, 1, 1, game) + countContinuous(x, y, symbol, -1, -1, game) - 1 >= 5 // diag \
                || countContinuous(x, y, symbol, 1, -1, game) + countContinuous(x, y, symbol, -1, 1, game) - 1 >= 5; // diag /
    }

    /**
     * Count the number of consecutive cells in dx, dy direction (including the initial cell)
     */
    private int countContinuous(int x, int y, char symbol, int dx, int dy, Game game) {
        int count = 0;
        int nx = x, ny = y;
        while (true) {
            Character c = game.getBoard().getSymbolAt(nx, ny);
            if (c != null && c == symbol) {
                count++;
                nx += dx;
                ny += dy;
            } else {
                break;
            }
        }
        return count;
    }
}
