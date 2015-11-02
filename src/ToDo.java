import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by zach on 10/12/15.
 */
public class ToDo {
    static void printTodos(ArrayList<ToDoItem> todos) {
        int todoNum = 1;
        for (ToDoItem todo : todos) {
            String checkBox = "[ ]";
            if (todo.isDone) {
                checkBox = "[x]";
            }
            String line = String.format("%d. %s %s", todoNum, checkBox, todo.text);
            System.out.println(line);
            todoNum++;
        }
    }

    static void insertTodo(Connection conn, String text) throws SQLException { //two arguments
        //create a Prepared Statement
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (?, false)");
        stmt.setString(1, text); //1 is referring to the first question mark. If we had multiple question marks, it would know what to refer to
        stmt.execute();
    }

    static ArrayList<ToDoItem> selectTodos(Connection conn) throws SQLException { //we don't need a PrepStatment bc we arenot passing anything thru
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM todos");
        ArrayList<ToDoItem> todos = new ArrayList();
        while (results.next()) {
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            ToDoItem item = new ToDoItem(text, isDone);
            todos.add(item); //adds a line to the database
        }

        return todos;
    }

    static void toggleTodo (Connection conn, int selectNum) throws SQLException { //making it toggle btwn done and not done
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE ROWNUM = ?"); //built in way to get the rownumber
        stmt.setInt(1, selectNum);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (text VARCHAR, is_done BOOLEAN)"); //creates our table for us

//        no longer storing a global arraylist (as below) so it can be deleted
//        ArrayList<ToDoItem> todos = new ArrayList();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            ArrayList<ToDoItem> todos = selectTodos(conn); //query our todos each time the loop goes around
            printTodos(todos);

            System.out.println("Options:");
            System.out.println("[1] Create todo");
            System.out.println("[2] Mark todo as done or not done");

            String option = scanner.nextLine();
            int optionNum = Integer.valueOf(option);

            if (optionNum == 1) { //where we're creating the todo
                System.out.println("Type a todo and hit enter");
                String todo = scanner.nextLine();
                insertTodo(conn, todo); //passing in the thing that they typed
            }
            else if (optionNum == 2) {
                System.out.println("Type the number of the todo you want to toggle");
                String select = scanner.nextLine();
                try {
                    int selectNum = Integer.valueOf(select);
//                    ToDoItem item = todos.get(selectNum - 1);
//                    item.isDone = !item.isDone;
//                    replacing the above lines with the below
                    toggleTodo(conn, selectNum);
                } catch (Exception e) {
                    System.out.println("An error occurred.");
                }
            }
            else {
                System.out.println("Invalid number.");
            }
        }
    }
}
