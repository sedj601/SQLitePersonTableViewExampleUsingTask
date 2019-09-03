/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sed.sample.sqlitepersontableviewexampleusingtasks;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author blj0011
 */
public class DBHandler
{

    Connection connection;

    public DBHandler()
    {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:person.db");
            System.out.println("Connected to SQLite Db: person.db");
        }
        catch (SQLException ex) {
            System.out.println(ex.toString());
        }
    }

    public Service<List<Person>> getDBData()
    {
        Service<List<Person>> service = new Service()
        {
            @Override
            protected Task<List<Person>> createTask()
            {
                return new Task<List<Person>>()
                {
                    @Override
                    protected List<Person> call() throws Exception
                    {
                        //Since there is not a lot of data in this database we are going to make this Service take longer that it should.

                        for (int i = 0; i < 100000; i++) {
                            System.out.println("Faking getting lots of data from database!");
                        }

                        String query = "SELECT * FROM Person";
                        List<Person> data = new ArrayList();

                        try (Statement statement = connection.createStatement();
                                ResultSet resultSet = statement.executeQuery(query)) {
                            while (resultSet.next()) {
                                Person person = new Person(resultSet.getInt("id"), resultSet.getString("first_name"), resultSet.getString("last_name"), resultSet.getInt("age"));
                                data.add(person);
                            }
                        }
                        catch (SQLException ex) {
                            System.out.println(ex.toString());
                        }

                        return data;
                    }
                };
            }
        };

        return service;
    }

    public Service<Boolean> addNewPerson(Person person)
    {
        return new Service<Boolean>()
        {
            @Override
            protected Task<Boolean> createTask()
            {
                return new Task<Boolean>()
                {
                    @Override
                    protected Boolean call() throws Exception
                    {
                        String sqlQuery = "INSERT INTO Person(first_name, last_name, age) VALUES (?, ?, ?)";

                        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery)) {
                            pstmt.setString(1, person.getFirstName());
                            pstmt.setString(2, person.getLastName());
                            pstmt.setInt(3, person.getAge());
                            pstmt.executeUpdate();

                            return true;
                        }
                        catch (SQLException ex) {
                            System.out.println(ex.toString());
                            return false;
                        }
                    }
                };
            }
        };
    }

    public Service<Boolean> deletePerson(Person person)
    {
        return new Service<Boolean>()
        {
            @Override
            protected Task<Boolean> createTask()
            {
                return new Task<Boolean>()
                {
                    @Override
                    protected Boolean call() throws Exception
                    {
                        String sqlQuery = "DELETE FROM Person WHERE id = ?";

                        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery)) {
                            pstmt.setInt(1, person.getId());
                            pstmt.executeUpdate();

                            return true;
                        }
                        catch (SQLException ex) {
                            System.out.println(ex.toString());
                            return false;
                        }
                    }
                };
            }
        };
    }

    public Service<Integer> getLastIDFromPersonTable()
    {
        return new Service<Integer>()
        {
            @Override
            protected Task<Integer> createTask()
            {
                return new Task<Integer>()
                {
                    @Override
                    protected Integer call() throws Exception
                    {
                        String sqlQuery = "SELECT seq FROM sqlite_sequence WHERE name = 'Person'";
                        int returnValue = -1;

                        try (Statement statement = connection.createStatement();
                                ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                            while (resultSet.next()) {
                                returnValue = resultSet.getInt("seq");
                            }
                        }
                        catch (SQLException ex) {
                            System.out.println("getLastIDFromPersonTable error:");
                            System.out.println("\t" + ex.toString());
                        }

                        return returnValue;
                    }
                };
            }
        };
    }

    public void closeConnection()
    {
        try {
            connection.close();
        }
        catch (SQLException ex) {
            System.out.println(ex.toString());
        }
    }
}
