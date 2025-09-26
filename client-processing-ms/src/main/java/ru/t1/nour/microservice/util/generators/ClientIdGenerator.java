package ru.t1.nour.microservice.util.generators;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientIdGenerator implements IdentifierGenerator {
    private static final String REGION_NUMBER = "77"; // XX
    private static final String DIVISION_NUMBER = "01"; // FF

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object o) {
        try{
            Connection connection = session.getJdbcConnectionAccess().obtainConnection();;
            try(Statement statement = connection.createStatement()){
                ResultSet rs = statement.executeQuery("SELECT nextval('clients_add_client_id_seq')");
                if (rs.next()) {
                    // Получаем порядковый номер
                    long sequenceValue = rs.getLong(1);

                    // Форматируем его в строку с ведущими нулями, чтобы она всегда была длиной 8 символов
                    String ordinalNumber = String.format("%08d", sequenceValue);

                    // Собираем и возвращаем финальный clientId
                    return REGION_NUMBER + DIVISION_NUMBER + ordinalNumber;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not generate Client ID", e);
        }

        throw new RuntimeException("Could not generate Client ID");
    }
}
