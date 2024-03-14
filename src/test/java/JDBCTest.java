import com.example.electricitybilling.JDBCCommands;
import com.example.electricitybilling.Khw;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JDBCTest {

    private final JDBCCommands jdbcCommands = new JDBCCommands();

    public JDBCTest() throws SQLException, IOException {
    }

    @Test
    public void оновленняПоказниківВжеІснуючогоЛічильника() throws SQLException {

        Date latestDate = Date.valueOf("2024-01-01");
        Date nDate = Date.valueOf("2024-05-01");
        jdbcCommands.addNewKwh("test", 100, 50, latestDate );

        Khw checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 100);
        assertEquals(checkKhw.getNightKhw(), 50);

        jdbcCommands.addNewKwh("test", 500, 500, nDate );

        checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 500);
        assertEquals(checkKhw.getNightKhw(), 500);

        jdbcCommands.deleteViaID("test");
    }

    @Test
    public void оновленняПоказниківНовогоЛічильника() throws SQLException {

        Date latestDate = Date.valueOf("2024-01-01");
        jdbcCommands.addNewKwh("test", 100, 50, latestDate );

        Khw checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 100);
        assertEquals(checkKhw.getNightKhw(), 50);

        jdbcCommands.deleteViaID("test");
    }

    @Test
    public void оновленняПоказниківЗаниженіНічні() throws SQLException {

        Date latestDate = Date.valueOf("2024-01-01");
        Date nDate = Date.valueOf("2024-05-01");
        jdbcCommands.addNewKwh("test", 100, 50, latestDate );
        jdbcCommands.addNewKwh("test", 150, 30, nDate );

        Khw checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 150);
        assertEquals(checkKhw.getNightKhw(), 130);

        jdbcCommands.deleteViaID("test");
    }

    @Test
    public void оновленняПоказниківЗаниженіДенні() throws SQLException {

        Date latestDate = Date.valueOf("2024-01-01");
        Date nDate = Date.valueOf("2024-05-01");
        jdbcCommands.addNewKwh("test", 100, 50, latestDate );
        jdbcCommands.addNewKwh("test", 10, 100, nDate );

        Khw checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 200);
        assertEquals(checkKhw.getNightKhw(), 100);

        jdbcCommands.deleteViaID("test");
    }

    @Test
    public void оновленняПоказниківЗанижені() throws SQLException {

        Date latestDate = Date.valueOf("2024-01-01");
        Date nDate = Date.valueOf("2024-05-01");
        jdbcCommands.addNewKwh("test", 200, 250, latestDate );
        jdbcCommands.addNewKwh("test", 10, 100, nDate );

        Khw checkKhw = jdbcCommands.getTheNewestKhw("test");

        assertEquals(checkKhw.getDayKhw(), 300);
        assertEquals(checkKhw.getNightKhw(), 330);

        jdbcCommands.deleteViaID("test");
    }

}
