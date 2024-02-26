import java.sql.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

interface DatabaseObserver {
    void update();
}

class Main implements DatabaseObserver {
    private static List<DatabaseObserver> observers;

    public Main() {
        observers = new ArrayList<>();
    }

    public void registerObserver(DatabaseObserver observer) {
        observers.add(observer);
    }

    public void unregisterObserver(DatabaseObserver observer) {
        observers.remove(observer);
    }

    private static void notifyObservers() {
        for (DatabaseObserver observer : observers) {
            observer.update();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.registerObserver(main); 
        main.start();
    }

    private void start() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://localhost:5432/postgres";

                Properties authorization = new Properties();
                authorization.put("user", "postgres");
                authorization.put("password", "1234");

                Connection connection = DriverManager.getConnection(url, authorization);

                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);

                System.out.print("Menu:" + "\n" +
                        "1. View books in database" + "\n" +
                        "2. Add new book into database" + "\n" +
                        "3. Update database" + "\n" +
                        "4. Delete book in database" + "\n" +
                        "5. Exit" + "\n" +
                        "Enter option: ");
                int option = scanner.nextInt();
                switch (option) {
                    case 1:
                        viewBooks(connection, statement);
                        break;
                    case 2:
                        addBook(connection, scanner, statement);
                        break;
                    case 3:
                        updateDatabase(connection, scanner);
                        break;
                    case 4:
                        deleteBook(connection, scanner);
                        break;
                    case 5:
                        System.out.println("Exited");
                        return;
                    default:
                        System.err.println("Error");
                }

                connection.close();
                statement.close();
            }

        } catch (Exception e) {
            System.err.println("Error accessing database!");
            e.printStackTrace();
        }
    }

    public static void viewBooks(Connection connection, Statement statement) throws SQLException {
        ResultSet table = statement.executeQuery("SELECT * FROM books ORDER BY id ASC");
        table.first();
        for (int j = 1; j <= table.getMetaData().getColumnCount(); j++) {
            System.out.print(table.getMetaData().getColumnName(j) + "\t\t\t");
        }
        System.out.println();

        table.beforeFirst();
        while (table.next()) {
            for (int j = 1; j <= table.getMetaData().getColumnCount(); j++) {
                System.out.print(table.getString(j) + "\t\t");
            }
            System.out.println();
        }
    }
    public static void addBook(Connection connection, Scanner scanner, Statement statement) throws SQLException {
        System.out.println("Enter id");
        int newId= scanner.nextInt();
        scanner.nextLine();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM books WHERE id = " +newId);
        if (resultSet.next()){
            System.err.println("This id already exist");
            return;
        }
        System.out.println("Enter title");
        String newTitle=scanner.nextLine();
        System.out.println("Enter author");
        String newAuthor=scanner.nextLine();
        System.out.println("Enter year of publishing");
        int newYear=scanner.nextInt();
        statement.executeUpdate("INSERT INTO books(id,title,author,year) VALUES(" + newId + ", '" + newTitle + "', '" + newAuthor + "', " + newYear + ")");
        notifyObservers();
    }
    public static void updateDatabase(Connection connection, Scanner scanner) throws SQLException {
        System.out.println("Enter book's id to update: ");
        int updateId = scanner.nextInt();
        System.out.println("Enter title");
        scanner.nextLine();
        String newTitle = scanner.nextLine();
        System.out.println("Enter author");
        String newAuthor = scanner.nextLine();
        System.out.println("Enter year of publishing");
        int newYear = scanner.nextInt();
        String sql = "UPDATE books SET title = ?, author = ?, year = ? WHERE id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, newTitle);
        preparedStatement.setString(2, newAuthor);
        preparedStatement.setInt(3, newYear);
        preparedStatement.setInt(4, updateId);
        preparedStatement.executeUpdate();
        notifyObservers();
    }
    public static void deleteBook(Connection connection, Scanner scanner) throws SQLException{
        System.out.println("Enter book's id to delete");
        int deleteId = scanner.nextInt();
        String sql = "DELETE FROM books WHERE id=?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1,deleteId);
        preparedStatement.executeUpdate();
        notifyObservers();
    }

    @Override
    public void update() {
        System.out.println("Database state changed");
    }
}
