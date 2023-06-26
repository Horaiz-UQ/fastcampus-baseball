package team.snowball.baseball.dao;

import team.snowball.baseball.handler.DatabaseException;
import team.snowball.baseball.model.player.Player;
import team.snowball.baseball.model.player.PlayerRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static team.snowball.baseball.code.ConsoleMessage.*;
import static team.snowball.baseball.code.ErrorMessage.*;

/**
 * author         : Jason Lee
 * date           : 2023-06-27
 * description    :
 */
public class PlayerDao implements PlayerRepository {
    private Connection connection;

    public PlayerDao() {
        connection = SnowballDBManager.getConnection();
    }

    @Override
    public int insert(Player player) {
        PreparedStatement pstmt = null;
        int result = 0;
        try {
            connection.setAutoCommit(false);

            String sql = "INSERT INTO player(team_id, name, position, created_at) VALUES (?, ?, ?, now())";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, player.getTeamId());
            pstmt.setString(2, player.getName());
            pstmt.setString(3, player.getPosition());

            result = pstmt.executeUpdate();

            if (result == 1) {
                connection.commit();
                System.out.println(MSG_SUCCESS_TO_REGISTER.getMessage());
                return result;
            }

            connection.rollback();
            System.out.println(ERR_MSG_FAILED_TO_REGISTER.getErrorMessage());
            return result;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println(e.getMessage());
                throw new DatabaseException();
            }
        } finally {
            SnowballDBManager.disconnect(connection, pstmt, null);
        }
        return result;
    }

    @Override
    public List<Player> findByTeamId(int id) {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        List<Player> playerList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM player WHERE team_id = ?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);
            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                Player player = Player.builder()
                        .id(resultSet.getLong("id"))
                        .teamId(resultSet.getInt("team_id"))
                        .name(resultSet.getString("name"))
                        .position(resultSet.getString("position"))
                        .createdAt(resultSet.getTimestamp("created_at"))
                        .build();
                playerList.add(player);
            }

            Stream.of(playerList).forEach(System.out::println);
            return playerList;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException();
        } finally {
            SnowballDBManager.disconnect(connection, pstmt, resultSet);
        }
    }

    @Override
    public int delete(int id) {
        PreparedStatement pstmt = null;
        int result = 0;
        try {
            connection.setAutoCommit(false);

            String sql = "delete from player where id=?";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, id);

            result = pstmt.executeUpdate();
            if (result == 1) {
                System.out.println(MSG_SUCCESS_TO_DELETE.getMessage());
                connection.commit();
                return result;
            }

            connection.rollback();
            System.out.println(ERR_MSG_FAILED_TO_DELETE.getErrorMessage());
            return result;

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                System.out.println(e.getMessage());
                throw new DatabaseException(ERR_MSG_FAILED_TO_DELETE.getErrorMessage());
            }
            System.out.println(e.getMessage());
            throw new DatabaseException();
        } finally {
            SnowballDBManager.disconnect(connection, pstmt, null);
        }
    }
}