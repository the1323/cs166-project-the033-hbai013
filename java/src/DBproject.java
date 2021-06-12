/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 */

public class DBproject {
    //reference to physical database connection
    private Connection _connection = null;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
        System.out.print("Connecting to database...");
        try {

            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        } catch (Exception e) {
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }
    }

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate(String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the update instruction
        stmt.executeUpdate(sql);

        // close the instruction
        stmt.close();
    }//end executeUpdate

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult(String query) throws SQLException {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        /*
         *  obtains the metadata object for the returned result set.  The metadata
         *  contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        //iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()) {
            if (outputHeader) {
                for (int i = 1; i <= numCol; i++) {
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i = 1; i <= numCol; ++i)
                System.out.print(rs.getString(i) + "\t");
            System.out.println();
            ++rowCount;
        }//end while
        stmt.close();
        return rowCount;
    }

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     *
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        /*
         * obtains the metadata object for the returned result set.  The metadata
         * contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        //iterates through the result set and saves the data returned by the query.
        boolean outputHeader = false;
        List<List<String>> result = new ArrayList<List<String>>();
        while (rs.next()) {
            List<String> record = new ArrayList<String>();
            for (int i = 1; i <= numCol; ++i)
                record.add(rs.getString(i));
            result.add(record);
        }//end while
        stmt.close();
        return result;
    }//end executeQueryAndReturnResult

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the number of results
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery(String query) throws SQLException {
        //creates a statement object
        Statement stmt = this._connection.createStatement();

        //issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        int rowCount = 0;

        //iterates through the result set and count nuber of results.
        if (rs.next()) {
            rowCount++;
        }//end while
        stmt.close();
        return rowCount;
    }

    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current
     * value of sequence used for autogenerated keys
     *
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */

    public int getCurrSeqVal(String sequence) throws SQLException {
        Statement stmt = this._connection.createStatement();

        ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
        if (rs.next()) return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup() {
        try {
            if (this._connection != null) {
                this._connection.close();
            }//end if
        } catch (SQLException e) {
            // ignored.
        }//end try
    }//end cleanup

    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                    "Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName() +
                            " <dbname> <port> <user>");
            return;
        }//end if

        DBproject esql = null;

        try {
            System.out.println("(1)");

            try {
                Class.forName("org.postgresql.Driver");
            } catch (Exception e) {

                System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
                e.printStackTrace();
                return;
            }

            System.out.println("(2)");
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];

            esql = new DBproject(dbname, dbport, user, "");

            boolean keepon = true;
            while (keepon) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Add Doctor");
                System.out.println("2. Add Patient");
                System.out.println("3. Add Appointment");
                System.out.println("4. Make an Appointment");
                System.out.println("5. List appointments of a given doctor");
                System.out.println("6. List all available appointments of a given department");
                System.out.println("7. List total number of different types of appointments per doctor in descending order");
                System.out.println("8. Find total number of patients per doctor with a given status");
                System.out.println("9. < EXIT");

                switch (readChoice()) {
                    case 1:
                        AddDoctor(esql);
                        break;
                    case 2:
                        AddPatient(esql);
                        break;
                    case 3:
                        AddAppointment(esql);
                        break;
                    case 4:
                        MakeAppointment(esql);
                        break;
                    case 5:
                        ListAppointmentsOfDoctor(esql);
                        break;
                    case 6:
                        ListAvailableAppointmentsOfDepartment(esql);
                        break;
                    case 7:
                        ListStatusNumberOfAppointmentsPerDoctor(esql);
                        break;
                    case 8:
                        FindPatientsCountWithStatus(esql);
                        break;
                    case 9:
                        keepon = false;
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup();
                    System.out.println("Done\n\nBye !");
                }//end if
            } catch (Exception e) {
                // ignored.
            }
        }
    }

    public static int readChoice() {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            } catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        } while (true);
        return input;
    }//end readChoice
    public static boolean checkint( String s ) {
        int ascii = (int)s.charAt(0);
        //System.out.println("castAscii " + ascii);
        if(ascii<48 || ascii>57) {
            return false;
        }
        Long i = Long.parseLong(s);
        if(i>2147483647 || i<0) {
            return false;
        }
        return true;
    }
    public static boolean checkage( String s ) {
        int ascii = (int)s.charAt(0);
        //System.out.println("castAscii " + ascii);
        if(ascii<48 || ascii>57) {
            return false;
        }
        Long i = Long.parseLong(s);
        if(i>150 || i<0) {
            return false;
        }
        return true;
    }
    public static boolean checkstatus( String s ) {
        if(s.equals("AV") || s.equals("AC") || s.equals("WL") || s.equals("PA") )return true;
        return false;
    }
    public static boolean checkdate( String s ) {
        //yyyy/mm/dd
        if( s.length() != 10) return false;
        if (s.charAt(4) != '/' || s.charAt(7) != '/' ) {
            return false;
        }
        for ( int i =0;i<4;i++){ //yyyy
            int ascii = (int)s.charAt(i);
            if(ascii<48 || ascii>57)return false;
        }
        for ( int i =5;i<7;i++){ //mm
            int ascii = (int)s.charAt(i);
            if(ascii<48 || ascii>57)return false;
        }
        for ( int i =8;i<10;i++){ //dd
            int ascii = (int)s.charAt(i);
            if(ascii<48 || ascii>57)return false;
        }
        String year = s.substring(0,4);
        if(Integer.parseInt(s.substring(0,4)) > 3000 ||Integer.parseInt(s.substring(0,4)) < 1911) return false;
        //System.out.println("year " + year );
        String mon = s.substring(5,7);
        // System.out.println("mmm " + mon );
        if(Integer.parseInt(s.substring(5,7)) > 12 ||Integer.parseInt(s.substring(5,7)) < 1) return false;
        String day = s.substring(8,10);
        //System.out.println("dddd " + day );
        if(Integer.parseInt(s.substring(8,10)) > 31 ||Integer.parseInt(s.substring(8,10)) < 1) return false;

        return true;
    }
    public static boolean checktime( String s ) {
        //08:00-10:00
        if ( s.length() !=11) return false;
        if( s.charAt(2)!= ':' || s.charAt(5)!= '-' || s.charAt(8)!= ':' ) return false;
        for ( int i =0;i<11;i++){

            if( i ==2 ||  i ==5 ||  i ==8  ) i++ ; //skip char
            int ascii = (int)s.charAt(i);
            if(ascii<48 || ascii>57)return false;
        }
        if(Integer.parseInt(s.substring(0,2)) > 23 ||Integer.parseInt(s.substring(0,2)) <0) return false;
        if(Integer.parseInt(s.substring(6,8)) > 23 ||Integer.parseInt(s.substring(6,8)) <0) return false;
        if(Integer.parseInt(s.substring(9,11)) > 59 ||Integer.parseInt(s.substring(9,11)) <0) return false;
        if(Integer.parseInt(s.substring(3,5)) > 59 ||Integer.parseInt(s.substring(3,5)) <0) return false;
        return true;
    }

    public static boolean checkname( String s ) {
        for ( int i =0;i<s.length();i++){
            if(Character.toUpperCase(s.charAt(i))>90 || Character.toUpperCase(s.charAt(i))<65 ) return false;
        }

        return true;
    }
    public static void AddDoctor(DBproject esql) {//1
        /*
         * doctor_ID INTEGER NOT NULL,
         *      	name VARCHAR(128),
         *              specialty VARCHAR(24),
         *              did INTEGER NOT NULL,
         *              PRIMARY KEY (doctor_ID),
         *              FOREIGN KEY (did) REFERENCES Department(dept_ID)
         *              */
        try {

            Integer id = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(doctor_id) from doctor;").get(0).get(0));
            String name, sp, did;

            System.out.println("Enter Doctor Name");
            name = in.readLine();
            while(!checkname(name)){
                System.out.println("Invalid Input, Try Again.");
                name = in.readLine();
            }
            System.out.println("Enter Specialty");
            sp = in.readLine();
            while(!checkname(sp)){
                System.out.println("Invalid Input, Try Again.");
                sp = in.readLine();
            }
            System.out.println("Enter Department ID");
            did = in.readLine();
            while(!checkint(did)){
                System.out.println("Invalid Input, Try Again.");
                did = in.readLine();

            }


            System.out.println("New Doctor: ID: " + id + " Name: " + name + " specialty: " + sp + " did: " + did);
            String query = "INSERT INTO doctor (doctor_id , name , specialty , did) VALUES ( " + id + " , '" + name + "' , '" + sp + "' , " + did + " );";
            esql.executeUpdate(query);
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }

    }

    public static void AddPatient(DBproject esql) {//2


		/*patient_ID INTEGER NOT NULL,
		name VARCHAR(128) NOT NULL,
		gtype _GENDER NOT NULL,
		age INTEGER NOT NULL,
		address VARCHAR(256),
		number_of_appts INTEGER,
		PRIMARY KEY (patient_ID)*/
        try {


            String name, gtype, age, address, napp;


            Integer pid = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(patient_id) from patient;").get(0).get(0));
            //System.out.println("nint: " + pid);
            //String valeur = esql.executeQueryAndReturnResult("select max(patient_id) from patient;").get(0).get(1);
            // System.out.println(valeur);
            //System.out.println("===========");


            System.out.println("Enter Patient Name");
            name = in.readLine();
            while(!checkname(name)){
                System.out.println("Invalid Input, Try Again.");
                name = in.readLine();
            }
            System.out.println("Enter gender M/F");
            gtype = in.readLine().toUpperCase();
            while(!gtype.toUpperCase().equals("M") && !gtype.toUpperCase().equals("F")){
                System.out.println("Invalid Input, Try Again.");
                gtype = in.readLine().toUpperCase();
            }
            System.out.println("Enter  age");
            age = in.readLine();
            while(!checkage(age)){
                System.out.println("Invalid Input, Try Again.");
                age= in.readLine();
            }
            System.out.println("Enter address");
            address = in.readLine();
            System.out.println("Enter number_of_appts");
            napp = in.readLine();
            while(!checkint(napp)){
                System.out.println("Invalid Input, Try Again.");
                napp = in.readLine();
            }
            String query = "INSERT INTO Patient (patient_ID , name , gtype , age , address , number_of_appts) VALUES ( " + pid + " , '" + name + "' , '" + gtype + "' , " + age + " , '" + address + "' , " + napp + " );";
            System.out.println("QUERY: " + query);
            esql.executeUpdate(query);
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }
    }


    public static void AddAppointment(DBproject esql) {//3


		/*
		appnt_ID INTEGER NOT NULL,
		adate DATE NOT NULL, eg '2018-10-20'
		time_slot VARCHAR(11),
		status _STATUS,
		PRIMARY KEY (appnt_ID)
		*/
        try {
            Integer appnt_ID = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(appnt_id) from appointment;").get(0).get(0));
            String adate, time_slot;
            System.out.println("Enter Date (YYYY/MM/DD):");
            adate = in.readLine();
            while(!checkdate(adate)){
                System.out.println("Invalid Input, Try Again.");
                adate = in.readLine();
            }
            System.out.println("Enter time_slot (HH:MM-HH:MM):");
            time_slot = in.readLine();
            while(!checktime(time_slot)){
                System.out.println("Invalid Input, Try Again.");
                time_slot = in.readLine();
            }
            System.out.println("New Appointment: ID: " + appnt_ID + " Date: " + adate + " time slot: " + time_slot + " Status: AV");
            String query = "INSERT INTO appointment (appnt_ID , adate , time_slot , status) VALUES ( " + appnt_ID + " , '" + adate + "' , '" + time_slot + "' , '" + "AV" + "' );";
            esql.executeUpdate(query);
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }


    }


    public static void MakeAppointment(DBproject esql) {//4
        //
        // Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
        try {
            String doctor_id, appt_id;
            String patient_id, name, gtype, age, address;
            System.out.println("Enter Doctor ID:");
            doctor_id = in.readLine();
            while(!checkint(doctor_id)){
                System.out.println("Invalid Input, Try Again.");
                doctor_id = in.readLine();
            }
            System.out.println("Enter Appointment ID:");
            appt_id = in.readLine();
            while(!checkint(appt_id)){
                System.out.println("Invalid Input, Try Again.");
                appt_id = in.readLine();
            }

            //System.out.println("ddd : " + doctor_id + " aaa: " + appt_id);
            String query = "select count(*) from has_appointment where doctor_id = " + doctor_id + " and appt_id = " + appt_id + " ;";
            Integer foundID = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0)); //check if count row = 0
            //System.out.println("FOUND, doctor has appointment.");
            //System.out.println(esql.executeQueryAndReturnResult(query));


            if (foundID == 1) { // found docid appt id, continue patient details
                String foundstatus = (esql.executeQueryAndReturnResult("select status from appointment where appnt_id = " + appt_id + " ;").get(0).get(0)); //check av ac ok, wl pa not ok
                if (foundstatus.equals("AV") || foundstatus.equals("AC")) { // can make appt
                    //System.out.println("status: " + foundstatus);
                    System.out.println("Enter Patient Details \n Enter Patient ID (if you are new patient, enter 'x'): ");
                    patient_id = in.readLine();
                    //System.out.println("xxxxxx.: "  + patient_id);
                    if (!patient_id.equals("x") ) {
                        while(!checkint(patient_id) ){

                            System.out.println("Invalid Input, Try Again.");
                            patient_id = in.readLine();
                        }
                    }
                    System.out.println("Enter name:");
                    name = in.readLine();
                    while(!checkname(name)){
                        System.out.println("Invalid Input, Try Again.");
                        name = in.readLine();
                    }
                    System.out.println("Enter Gender as 'M/F':");
                    gtype = in.readLine().toUpperCase();
                    while(!gtype.toUpperCase().equals("M") && !gtype.toUpperCase().equals("F")){
                        System.out.println("Invalid Input, Try Again.");
                        gtype = in.readLine().toUpperCase();
                    }
                    System.out.println("Enter age: ");
                    age = in.readLine();
                    while(!checkage(age)){
                        System.out.println("Invalid Input, Try Again.");
                        age = in.readLine();
                    }
                    System.out.println("Enter address: ");
                    address = in.readLine();
                    Integer newpid;
                    if (patient_id.equals("x")) { //new patient

                        System.out.println("Welcome new patient!");
                        query = "select max(patient_id) from patient;";
                        newpid = 1 + Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0));
                        //System.out.println("npid " + newpid);
                        query = "INSERT INTO patient (patient_id,  name, gtype, age, address ,number_of_appts) VALUES ( " + newpid + ", '" + name + "' , '" + gtype + "' , " + age + " , '" + address + "' , 1 );";
                        //System.out.println("insert new pat: " + query);
                        esql.executeUpdate(query); //insert new patient
                    } else { //old patient
                        String temp = "select * from patient where patient_id = " + patient_id;
                        System.out.println(esql.executeQueryAndPrintResult(temp));
                        System.out.println("is the info above yours? Y/N");
                        if(in.readLine().toUpperCase().equals("Y")){
                            newpid = Integer.parseInt(patient_id);
                        }
                        else{
                            System.out.println("please enter as new patient, patient id not found");
                            return;
                        }

                    }
                    if (foundstatus.equals("AV")) {//update appt
                        System.out.println("status is Available, update status to Active");
                        //Integer npid = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(patient_id) from patient;").get(0).get(0));
                        query = "update appointment set status = 'AC' where appnt_id = " + appt_id + " ;";
                        esql.executeUpdate(query);
                        System.out.println("Your Appointment is: ");
                        query = "select * from appointment where appnt_id = " + appt_id + " ;";
                        System.out.println(esql.executeQueryAndPrintResult(query));


                    }
                    if (foundstatus.equals("AC")) {// insert new appt, hasappt

                        Integer appnt_ID = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(appnt_id) from appointment;").get(0).get(0));
                        //System.out.println("appnid: " + appnt_ID);
                        System.out.println("The appointment is Active, new appointment added with appointment ID: " + appnt_ID + " Status: WL" );

                        //System.out.println( esql.executeQueryAndReturnResult("select adate from appointment where appnt_id = " + appnt_ID + " ;").get(0));
                        ///////////////////////////////////////////////////////////
                        //query = "select adate from appointment where appnt_id = " + appt_id + " ;";
                        //System.out.println("get date from old: " + query);
                        String getadate = esql.executeQueryAndReturnResult(query).get(0).get(0);
                        //System.out.println("adate " + getadate);
                        //query ="select time_slot from appointment where appnt_id = " + appt_id + " ;";
                        //System.out.println("get time from old: " + query);
                        String gettime = (esql.executeQueryAndReturnResult(query).get(0).get(0));

                        //System.out.println("atime " + gettime);
                        query = "INSERT INTO appointment (appnt_ID , adate , time_slot , status) VALUES ( " + appnt_ID + " , '" + getadate + "' , '" + gettime + "' , '" + "WL" + "' );";
                        esql.executeUpdate(query);
                        //Integer hasid = 1 + Integer.parseInt(esql.executeQueryAndReturnResult("select max(appt_id) from has_appointment;").get(0).get(0));
                        query = "INSERT INTO has_appointment (appt_id, doctor_id) VALUES ( " + appnt_ID + " , " + doctor_id + " ); ";
                        esql.executeUpdate(query);
                        System.out.println("Your Appointment is: ");
                        query = "select * from appointment where appnt_id = " + appnt_ID + " ;";
                        System.out.println(esql.executeQueryAndPrintResult(query));




                    }

                } else {// wl or pa

                    System.out.println("Appointment is Not available ");
                    return;
                }
            } else { // if appt or doc count =0
                System.out.println("Appointment ID or Doctor ID not found ");
                return;
            }

        }//try
        catch (
                Exception e) {

            System.err.println(e.getMessage());


        }

    }

    public static void ListAppointmentsOfDoctor(DBproject esql) {//5
        // For a doctor ID and a date range, find the list of active and available appointments of the doctor
        //select * from appointment  INNER JOIN has_appointment on appointment.appnt_id =  has_appointment.appt_id where  adate between '2011/1/1' and '2022/1/1'  and doctor_id = 25 and (status = 'AC' or status = 'AV');

        try {
            String doctor_id;
            String date1, date2;
            date1 = date2 = "'";
            System.out.println("Enter Doctor ID:");
            doctor_id = in.readLine();
            while(!checkint(doctor_id)){
                System.out.println("Invalid Input, Try Again.");
                doctor_id = in.readLine();
            }
            System.out.println("Enter Start Date (YYYY/MM/DD):");
            String temp1 = in.readLine();
            while(!checkdate(temp1)){
                System.out.println("Invalid Input, Try Again.");
                temp1 = in.readLine();
            }
            System.out.println("Enter End Date (YYYY/MM/DD):");
            String temp2 =  in.readLine();
            while(!checkdate(temp2)){
                System.out.println("Invalid Input, Try Again.");
                temp2= in.readLine();
            }
            date1 += temp1;
            date2 += temp2;
            date1 += "'";
            date2 += "'";
            System.out.println("Looking for appointment for DocID : " + doctor_id + " Date Range: " + date1 + " - " + date2);
            String query = "select appnt_id, adate, time_slot, status from appointment  INNER JOIN has_appointment on appointment.appnt_id =  has_appointment.appt_id where adate between " + date1 + " and " + date2 + " and doctor_id = " + doctor_id + " and (status = 'AC' or status = 'AV');";

            System.out.println(esql.executeQueryAndPrintResult(query));
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }

    }

    public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
        // For a department name and a specific date, find the list of available appointments of the department

        try {
            String date, name, name1;
            date = name = "'";
            System.out.println("Enter Date (YYYY/MM/DD) :");
            String temp2 = in.readLine();
            while(!checkdate(temp2)){
                System.out.println("Invalid Input, Try Again.");
                temp2= in.readLine();
            }
            date += temp2;
            System.out.println("Enter Department Name:");
            name1 = in.readLine();
            while(!checkname(name1)){
                System.out.println("Invalid Input, Try Again.");
                name1= in.readLine();
            }
            name+=name1;
            date += "'";
            name += "'";
            System.out.println("Available Appointments Of Department : " + name + " Date: " + date);
            String query = "select appnt_id, adate, time_slot, status from appointment INNER JOIN has_appointment on appointment.appnt_id =  has_appointment.appt_id INNER JOIN doctor on doctor.doctor_id =has_appointment.doctor_id INNER JOIN department on department.dept_id = doctor.did where status = 'AV' AND adate = " + date + " and department.name = " + name + ";";

            System.out.println(esql.executeQueryAndPrintResult(query));
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }

    }

    public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
        // Count number of different types of appointments per doctors and list them in descending order

        try {


            String query = "select doctor.doctor_id, name , status ,count(*)  from appointment , has_appointment, doctor where appointment.appnt_id = has_appointment.appt_id  and doctor.doctor_id = has_appointment.doctor_id group by doctor.doctor_id, status order by doctor_id, count desc ;";

            System.out.println(esql.executeQueryAndPrintResult(query));
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }


    }


    public static void FindPatientsCountWithStatus(DBproject esql) {//8
        // Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.

        try {
            String status = "'", status1;
            System.out.println("Enter Status:");
            status1 = in.readLine();
            while(!checkstatus(status1)){
                System.out.println("Invalid Input, Try Again.");
                status1= in.readLine();
            }

            status += status1;
            status += "'";
            System.out.println("Looking for Patients Count per doctor With Status : " + status);
            String query = "select has_appointment.doctor_id, doctor.name, count(has_appointment.doctor_id) as Num_of_Patient from appointment INNER JOIN has_appointment on appointment.appnt_id =  has_appointment.appt_id INNER JOIN doctor on doctor.doctor_id =  has_appointment.doctor_id INNER JOIN department on department.dept_id = doctor.did INNER JOIN searches on searches.aid = appointment.appnt_id where status = " + status + " and searches.hid = '0' group by has_appointment.doctor_id, doctor.name order by Num_of_Patient  desc;";

            System.out.println(esql.executeQueryAndPrintResult(query));
        } catch (Exception e) {

            System.err.println(e.getMessage());


        }
    }
}
