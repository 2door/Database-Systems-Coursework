/*
 * This is the schema for the database being used:

DROP TABLE PREREQUISITES CASCADE CONSTRAINTS;
DROP TABLE EXAM CASCADE CONSTRAINTS;
DROP TABLE HISTORY CASCADE CONSTRAINTS;
DROP TABLE MODULE CASCADE CONSTRAINTS;
DROP TABLE STUDENT CASCADE CONSTRAINTS;

CREATE TABLE STUDENT(
    Student_name varchar(30) NOT NULL,
    Student_id char(4) NOT NULL,
    Course_name varchar(30) NOT NULL,
    Year smallint NOT NULL,
    PRIMARY KEY(Student_id)
);

CREATE TABLE MODULE(
    Module_name varchar(30) NOT NULL,
    Module_code varchar(6) NOT NULL, 
    Department_name varchar(30) NOT NULL,
    PRIMARY KEY(Module_code)
);

CREATE TABLE HISTORY(
    Module_code varchar(6) NOT NULL,
    Delivery_year smallint NOT NULL,
    Organizer_name varchar(30) NOT NULL,
    FOREIGN KEY(Module_code) REFERENCES MODULE(Module_code),
    PRIMARY KEY(Module_code, Delivery_year)
);

CREATE TABLE EXAM(
    Student_id char(4) NOT NULL,
    Module_code varchar(6) NOT NULL,
    Exam_year smallint NOT NULL,
    Score smallint NOT NULL,
    FOREIGN KEY(Module_code, Exam_year) REFERENCES HISTORY(Module_code, Delivery_year),
    FOREIGN KEY(Student_id) REFERENCES STUDENT(Student_id),
    UNIQUE(Student_id, Module_code, Exam_year)
);

CREATE TABLE PREREQUISITES(
    Module_code varchar(6) NOT NULL,
    Prerequisite_code varchar(6) NOT NULL,
    FOREIGN KEY(Module_code) REFERENCES MODULE(Module_code),
    FOREIGN KEY(Prerequisite_code) REFERENCES MODULE(Module_code),
    UNIQUE(Module_code, Prerequisite_code)
);

*/
import java.sql.*;
import java.io.*;

public class Assignment {
    public static void main(String args[]) throws Exception, IOException, SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load the driver");
        }

        String user = readEntry("Enter userid: ");
        String pass = readEntry("Enter password: ");
        Connection conn = DriverManager.getConnection(
            "jdbc:oracle:thin:@daisy.warwick.ac.uk:1521:daisy", user, pass
            );

        boolean done = false;
        do {
            printMenu();
            System.out.print("Enter your choice: ");
            System.out.flush();
            String ch = readLine();
            //this accounts for an empty option to avoid exceptions
            //when trying to chack an empty 'ch' String
            if(ch.isEmpty()) {
                System.out.println("Not a valid option");
            } else {
                switch (ch.charAt(0)) {
                case '1':
                    modulesByStudent(conn);
                    break;
                case '2':
                    ghostModules(conn);
                    break;
                case '3':
                    popularityRatings(conn);
                    break;
                case '4':
                    topStudent(conn);
                    break;
                case '5':
                    harshness(conn);
                    break;
                case '6':
                    leafModules(conn);
                    break;
                case '7':
                    riskyExams(conn);
                    break;
                case '8':
                    twistedPres(conn);
                    break;
                case '0':
                    done = true;
                    break;
                default:
                    System.out.println(" Not a valid option ");
                }
            }
        } while (!done);
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("MENU");
        System.out.println("(1) Modules by student");
        System.out.println("(2) Ghost modules");
        System.out.println("(3) Popularity ratings");
        System.out.println("(4) Top student(s)");
        System.out.println("(5) Harshness ranking");
        System.out.println("(6) Leaf modules");
        System.out.println("(7) Risky exams");
        System.out.println("(8) Twisted prerequiesites");
        System.out.println("(0) Quit");
    }

    public static void modulesByStudent(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        //first, all student ids (Student_id) must be retrieved in order to
        //separately display modules (if any) for each
        sqlString = "select Student_id from STUDENT";
        ResultSet rset1;
        ResultSet rset2;
        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query for student IDs" + e);
            stmt1.close();
            stmt2.close();
            return;
        }

        if(rset1.next()) {
            //if there are students in the STUDENT table
            do {
                //saving this particular student id to display and 
                //insert into query for this student's modules
                String sid = rset1.getString(1);

                //retrieving module codes (Module_code) for each specific student id (Student_id)
                sqlString = "select Module_code from EXAM where Student_id = '" + sid + "'";
                try {
                    rset2 = stmt2.executeQuery(sqlString);
                } catch (SQLException e) {
                    System.out.println(e + ": Could not execute query for modules by ID: " + sid);
                    stmt1.close();
                    stmt2.close();
                    return;
                }

                if (rset2.next()) {
                    //this particular student takes modules,
                    //so they must be listed in the format described in the specification:
                    //STUDENT_ID: MODULE1 MODULE2 ...
                    System.out.print(sid + ": ");
                    do {
                        //each module taken by the student with this student id is displayed
                        System.out.print(rset2.getString(1) + ' ');
                    } while (rset2.next());
                    System.out.println();
                } else {
                    //this particular student takes no modules,
                    //so the info displayed is the id and that there are no modules to display:
                    //STUDENT_ID: None
                    System.out.println(sid + ": None");
                }
            } while(rset1.next());
        } else {
            //there are no students in the STUDENT table,
            //so there are no modules to display for any student
            System.out.println("No students to display");
        }
        stmt1.close();
        stmt2.close();
    }

    public static void ghostModules(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt = conn.createStatement();

        //retrieving any module codes that can not be found in the EXAM table
        sqlString = "select Module_code from MODULE where Module_code NOT IN " +
            "(select distinct Module_code from EXAM)";
        ResultSet rset1;
        try {
            rset1 = stmt.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retrieve examined modules" + e);
            stmt.close();
            return;
        }

        if(rset1.next()) {
            //if there are any ghost modules, display them following the specified format:
            //MODULE1 MODULE2 ...
            do {
                System.out.print(rset1.getString(1) + ' ');  
            } while(rset1.next());
        } else {
            //if there are no modules in the MODULE table, there are none to display
            //in order to provide some form of feedback to the user,
            //the fact that there are no ghost modules is printed
            System.out.println("No modules to display");
        }
        System.out.println();
        stmt.close();
    }

    public static void popularityRatings(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt1 = conn.createStatement();

        //a larger number of exams taken in a module means that it is more popular,
        //so a larger count of entries in EXAM representing exams taken in a module means
        //a higher popularity rating for that module
        /*
        * Retrieve all module names (Module_name) from MODULE, and the count of entries in 
        * EXAM for each, in descending order by their popularity (count of entries),
        * such that the most popular module is at the top.
        * Any module which is not found in EXAM will have 'null' as its count value,
        * so it will be last in the ordering (at the bottom).
        * The modules are checked against EXAM table entries by their module code (Module_code).
        * any 2 modules with identical popularity (count of entries),
        * including any 2 modules with 'null' popularity, are display in arbitrary order.
        */
        sqlString = "select M.Module_name from MODULE M left join " + 
            "(select Module_name as Name, count(*) as Num from MODULE M, " + 
            "EXAM E where(M.Module_code=E.Module_code) group by Module_name order by Num desc) " + 
            "A on (M.Module_name=A.Name)";
        ResultSet rset1;
        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retrieve top modules: " + e);
            stmt1.close();
            return;
        }
        if(rset1.next()) {
            //if there are any modules at all in the MODULE table,
            //they will be displayed in order of their popularity, 
            //following the specified format:
            //MODULE_NAME1
            //MODULE_NAME2
            //...
            do {
                System.out.println(rset1.getString(1));
            } while(rset1.next());
        } else {
            //if there are no modules in the MODULE table at all, then there are no modules to display
            //in order to provide some form of feedback to the user, 
            //the fact that there are no modules to include in the popularity rating or display is printed
            System.out.println("No modules to display");
        }
        stmt1.close();
    }

    public static void topStudent(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt = conn.createStatement();
        /*
        * Retrieve the student names (Student_name) and their overal exam score average,
        * in descending order by this average, such that the students with a higher 
        * average score are at the top.
        * Any 2 students with identical averages are taken in arbitrary order.
        * Students that have not taken any exams are not considered as they have no 
        * scores and therefore no average score either.
        */
        sqlString = "select S.Student_name, avg(Score) Av from STUDENT S, "+ 
            "EXAM E where (S.Student_id=E.Student_id) group by " + 
            "S.Student_name order by Av desc";
        ResultSet rset;
        try {
            rset = stmt.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retrieve top students: " + e);
            stmt.close();
            return;
        }

        if(rset.next()) {
            //if there are any students that have taken at least one exam

            //the student at the top of the list will have the highest 
            //average score so this score is saved as a maximum
            String max = rset.getString(2);
            do {
                //display any students who have an average score equal to 
                //the saved maximum, following the specified format:
                //STUDENT1
                //STUDENT2
                //...
                if (rset.getString(2).equals(max)) {
                    System.out.println(rset.getString(1));
                } else {
                    /*
                    * if this student has a score which is not equal to 
                    * the saved max they should not be displayed and,
                    * since the scores are considered in descending 
                    * order and the first one was the maximum,
                    * there are no more socres equal to the maximum score from 
                    * this point so there is no point in looping over the remaining students
                    */
                    break;
                }
            } while (rset.next());
        } else {
            //if there are no students who have taken any exams,
            //then all students are assumed to be top students,
            //listed in arbitrary order

            //retrieve all student names (Student_name) from STUDENT table
            sqlString = "select Student_name from STUDENT";
            try {
                rset = stmt.executeQuery(sqlString);
            } catch (SQLException e) {
                System.out.println("Could not execute query to retrieve all stuent names: " + e);
                stmt.close();
                return;
            }
            if(rset.next()) {
                //if there are any students in the STUDENT table,
                //each student name is displayed, following the specified format:
                //STUDENT1
                //STUDENT2
                //...
                do {
                    System.out.println(rset.getString(1));
                } while(rset.next());
            } else {
                //if there are no students in the STUDENT table,
                //there are no top students to display

                //in order to provide some form of feedback to the user,
                //the fact that there are no students to list is printed
                System.out.println("No students to display");
            }
        }
        stmt.close();
    }

    public static void harshness(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt1 = conn.createStatement();
        /*
        * Retrieving all organizer names (Organizer_name) from the HISTORY table,
        * in ascending by the average of scores ( avg(Score) ) obtained by
        * students in exams for modules organized by the organizer, such that the
        * organizer with the lowest average (harshest) is at the top.
        * For an exam to be considered when calculating this average for a specific
        * organizer, the exam module code (Module_code) and year (Exam_year) are 
        * checked against module codes and delivery years (Delivery_year) in records from 
        * HISTORY related to the organizer.
        * Any 2 organizers with identical average score obtained by students in
        * their respective modules are listed in arbitrary order.
        */
        sqlString = "select H.Organizer_name, avg(Score) from HISTORY H, " + 
            "EXAM E where(H.Module_code=E.Module_code AND H.Delivery_year=E.Exam_year) " + 
            "group by H.Organizer_name order by avg(Score) asc";
        ResultSet rset1;
        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retireve harshest exams: " + e);
            stmt1.close();
            return;
        }

        Statement stmt2 = conn.createStatement();
        /*
        * Some organizer names might not be found by the query because some might have 
        * organized modules in which nobody has taken any exam.
        * In order to include these names, a temporary table is created and all of the
        * retrieved names and averages are inserted.
        * Then, all additional organizer names are added with an associated
        * average equal to a value grater than the maximum average retrieved by
        * the first query.
        * In this way, when listing all organizer names in ascending order of their 
        * averages, the organizers who have not marked any exams are
        * listed last (as being the least harsh).
        * This is an assumption that organizers who have not had any exams being 
        * taken in their modules are the least harsh.
        * These organizer names (which have identical values) will be listed in arbitrary order.
        */
        try {
            stmt2.executeQuery("create table temp(Name char(30) PRIMARY KEY NOT NULL, " + 
                "Score smallint NOT NULL)");
        } catch (SQLException e) {
            System.out.println("Could not create temp: " + e);
            stmt1.close();
            stmt2.close();
            return;
        }
        int max = 0;
        while (rset1.next()) {
            //inserting retrieved values into temporary table
            String sqlString2 = "insert into temp values('" + 
                rset1.getString(1) + "', " + rset1.getInt(2) + ")";
            try {
                stmt2.executeQuery(sqlString2);
                //if the average of this organizer is greater than the maximum of
                //all previous organizers, a value greater than this average is stored:
                if(rset1.getInt(2) >= max) max = rset1.getInt(2) + 1;
            } catch (SQLException e) {
                System.out.println("Could not insert value " + rset1.getString(1) +
                    " into temp: " + e);
            }
        }

        //selecting all organizer names from HISTORY table to insert into the temporary table
        sqlString = "select distinct Organizer_name from HISTORY";
        try {
            rset1 = stmt2.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not select extra organizer names: " + e);
            try {
                stmt2.executeQuery("drop table temp");
            } catch (SQLException e1) {
                System.out.println("Could not drop temp: " + e1);
            }
            stmt2.close();
            stmt1.close();
            return;
        }

        while(rset1.next()) {
            //inserting each organizer name into the temporary table along with a
            //value greater than the averages of any organizer:
            String sqlString2 = "insert into temp values('" + rset1.getString(1) + "', " + 
                max + ")";
            //any organizer names which were already in the table will not ba added again,
            //as the organizer name is a primary key in the temporary table
            try {
                stmt1.executeQuery(sqlString2);
            } catch (SQLException e) {
            }
        }
        /*
        * The temporary table now has values for every organizer name.
        * The harshest organizer will be at the top of the listing (harshest).
        * Any organizers who have had no exams taken in their modules will be at
        * the bottom of the listing (least harsh) in arbitrary order.
        */
        sqlString = "select Name, Score from temp order by Score asc";
        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not extract from temp: " + e); 
            try {
                stmt2.executeQuery("drop table temp");
            } catch (SQLException e1) {
                System.out.println("Could not drop temp: " + e1);
            }
            stmt2.close();
            stmt1.close();
            return;
        }

        if(rset1.next()) {
            //if there are organizers to list, each organizer is listed in order of
            //their harshness, following the specified format:
            //ORGANIZER1
            //ORGANIZER2
            //...
            do {
                System.out.println(rset1.getString(1));
            } while(rset1.next());
        } else {
            //if there were no organizers at all (meaning there are no 
            //entries in the HISTORY table), the temporary table will be empty

            //in order to provide some form of feedback to the user,
            //the fact that there are no organizer names to display is printed
            System.out.println("No names to siplay");
        }

        try {
            stmt2.executeQuery("drop table temp");
        } catch (SQLException e) {
            System.out.println("Could not drop temp: " + e);
        }
        stmt2.close();
        stmt1.close();
    }

    public static void leafModules(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt = conn.createStatement();

        //retrieving any modules from MODULE which can not be found to
        //have prerequisites in the PREREQUISITES table
        sqlString = "select Module_code from MODULE where " + 
            "(Module_code not in (select Module_code from PREREQUISITES))";
        ResultSet rset;
        try {
            rset = stmt.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not retrieve modules not in Prerequisites: " + e);
            stmt.close();
            return;
        }

        if(rset.next()) {
            //if there are any modules with no prerequisites,
            //each module is displayed following the specified format:
            //MODULE1 MODULE2 ...
            do {
                System.out.print(rset.getString(1) + ' ');
            } while(rset.next());
        } else {
            //if there are no modules which have no prerequisites,
            //in order to provide some form of feedback to the user,
            //the fact that there are no such modules is displayed
            System.out.print("No modules to display");
        }
        System.out.println();
        stmt.close();
    }

    public static void riskyExams(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt1 = conn.createStatement();
        /*
        * Retrieving the student ids (Student_id) of students who are not in 
        * the selection of students who have taken exams in all of a module's
        * prerequiesites before taking that module.
        * It is assumed that if a student takes an exam for a module A which is
        * a prerequisite for B in the same year as the one in which he is taking an
        * exam for B, the exam for B is risky and the student is listed.
        */
        sqlString = "select distinct Student_id from EXAM E, PREREQUISITES P  where " + 
            "(Student_id NOT IN (select E.Student_id from EXAM E inner join (select " + 
            "E.Student_id as ID, E.Exam_year as YEAR, E.Module_code as MOD, " + 
            "P.Prerequisite_code as PRE from EXAM E, PREREQUISITES P where(" + 
            "E.Module_code=P.Module_code)) on (E.Student_id=ID AND E.Module_code=PRE AND " + 
            "E.Exam_year<YEAR)) AND E.Module_code=P.Module_code)";
        ResultSet rset1;
        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retrieve student ids from " + 
                "Exam table: " + e);
            stmt1.close();
            return;
        }

        if(rset1.next()) {
            //if there are any students who take risky exams,
            //they are listed following the specified format:
            //STUDENT1 STUDENT2 ...
            do {
                System.out.print(rset1.getString(1) + ' ');
            } while(rset1.next());
        } else {
            //if there are no students who take risky exams,
            //in order to provide some form of feedback to the user,
            //the fact that there are no such users to siplay is printed
            System.out.print("No students to display");
        }
        System.out.println();
        stmt1.close();
    }

    public static void twistedPres(Connection conn) throws SQLException, IOException {
        String sqlString = null;
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();

        //select the root (CONNECT_BY_ROOT Module_code) and a flag if a cycle was
        //encountered (CONNECT_BY_ISCYCLE) for any paths starting at any module and
        //following prerequisites (prior Prerequisite_code=Module_code) of
        //each module in the path;

        //the path following stops if a cycle is encounteres ('nocycle')
        sqlString = "select CONNECT_BY_ROOT Module_code, CONNECT_BY_ISCYCLE from " + 
            "prerequisites connect by nocycle prior Prerequisite_code=Module_code";
        ResultSet rset1;

        try {
            rset1 = stmt1.executeQuery(sqlString);
        } catch (SQLException e) {
            System.out.println("Could not execute query to retrieve modules and " + 
                "hierarchical prerequisites: " + e);
            stmt1.close();
            return;
        }

        if(rset1.next()) {
            //if there are any modules in the MODULE table, create a temporary table to
            //hold unsatisfiable module codes

            //a module is unsatisfiable if it is part of a cycle or is at some point its
            //prerequisites are unsatisfiable (eg. if the module has a prerequisite which is
            //part of a cycle)
            sqlString = "create table temp(Code varchar(30) PRIMARY KEY NOT NULL)";
            try {
                stmt2.executeQuery(sqlString);
            } catch (SQLException e) {
                System.out.println("Could not create temp: " + e);
                stmt1.close();
                stmt2.close();
                return;
            }

            do {
                //add module codes of unsatisfiable modules; a module will only be
                //added once because the code is a primary key in the temporary table
                if(rset1.getInt(2) == 1) {
                    sqlString = "insert into temp values('" + rset1.getString(1) + "')";
                    try {
                        stmt2.executeQuery(sqlString);
                    } catch (SQLException e) {
                    }
                }
            } while(rset1.next());

            //retrieve the modules from the temporary table (these will be all of 
            //the unsatisfiable modules)
            sqlString = "select Code from temp";
            try {
                rset1 = stmt1.executeQuery(sqlString);
            } catch (SQLException e) {
                System.out.println("Could not extract from temp: " + e); 
                try {
                    stmt2.executeQuery("drop table temp");
                } catch (SQLException e1) {
                    System.out.println("Could not drop temp: " + e1);
                }
                stmt2.close();
                stmt1.close();
                return;
            }

            if(rset1.next()) {
                //if there were any modules with unsatisfiable prerequisites,
                //they will have been put in the temporary table

                //display all of these modules following the specified format:
                //MODULE1 MODULE2 ...
                do {
                    System.out.print(rset1.getString(1) + ' ');
                } while(rset1.next());
            } else {
                //if there were no modules with unsatisfiable prerequisites,
                //then there are no modules to display

                //in order to provide some from of feedback to the user,
                //the fact that there are no modules with twisted prerequisites to
                //display is printed to the screen
                System.out.print("No modules to display");
            }
        } else {
            //if there are no modules in the MODULE table, in order to provide some
            //form of feedback to the user, this fact is printed to the screen
            System.out.print("No modules to display");
        }
        System.out.println();

        try {
            stmt2.executeQuery("drop table temp");
        } catch (SQLException e1) {
            System.out.println("Could not drop temp: " + e1);
        }
        stmt1.close();
        stmt2.close();
    }

    //method used to read user id and password from the user
    private static String readEntry(String prompt) {
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while (c != '\n' && c != -1) {
                buffer.append((char) c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }
    //method used to read input from the user
    private static String readLine() {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr, 1);
        String line = "";

        try {
            line = br.readLine();
        } catch (IOException e) {
            System.out.println("Error in SimpleIO.readLine: " + "IOException was thrown");
            System.exit(1);
        }
        return line;
    }
}