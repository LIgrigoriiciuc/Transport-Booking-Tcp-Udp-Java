package Repository;


import Domain.Reservation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ReservationRepository extends GenericRepository<Long, Reservation> {

    @Override
    public String getTableName() { return "reservations"; }

    @Override
    protected String buildInsertSql() {
        return "INSERT INTO reservations (clientName, reservationTime) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, Reservation reservation) throws SQLException {
        ps.setString(1, reservation.getClientName());
        ps.setString(2, reservation.getReservationTime().toString());
        }

    @Override
    protected String buildUpdateSql() {
        return "UPDATE reservations SET clientName = ?, reservationTime = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, Reservation reservation) throws SQLException {
        ps.setString(1, reservation.getClientName());
        ps.setString(2, reservation.getReservationTime().toString());
        ps.setLong(3, reservation.getId());
        }

    @Override
    protected Reservation mapResultSetToEntity(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String clientName = rs.getString("clientName");
        LocalDateTime reservationTime = LocalDateTime.parse(rs.getString("reservationTime"));
        Reservation reservation = new Reservation(id, clientName, reservationTime);
        return reservation;
    }
    @Override
    protected Long extractGeneratedId(ResultSet keys) throws SQLException {
        return keys.getLong(1);
    }
}