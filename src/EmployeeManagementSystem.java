import java.io.*;
import java.util.*;

class Employee implements Serializable {
    private int id;
    private String name;
    private String department;
    private float basicSalary;

    public Employee(int id, String name, String department, float basicSalary) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.basicSalary = basicSalary;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public float getBasicSalary() { return basicSalary; }
}

class Attendance implements Serializable {
    private int id;
    private int daysPresent;
    private int overtimeHours;
    private float deductions;

    public Attendance(int id, int daysPresent, int overtimeHours, float deductions) {
        this.id = id;
        this.daysPresent = daysPresent;
        this.overtimeHours = overtimeHours;
        this.deductions = deductions;
    }

    public int getId() { return id; }
    public int getDaysPresent() { return daysPresent; }
    public int getOvertimeHours() { return overtimeHours; }
    public float getDeductions() { return deductions; }
}

public class EmployeeManagementSystem {
    private static final String EMPLOYEE_FILE = "employees.ser";
    private static final String ATTENDANCE_FILE = "attendance.ser";
    private static final String EMPLOYEE_BACKUP = "backup_employees.ser";
    private static final String ATTENDANCE_BACKUP = "backup_attendance.ser";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "12345";

    private Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new EmployeeManagementSystem().login();
    }

    private void login() {
        int attempts = 3;
        while (attempts-- > 0) {
            System.out.print("Enter Username: ");
            String username = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                System.out.println("Login successful!\n");
                menu();
                return;
            } else {
                System.out.println("Invalid credentials. Attempts left: " + attempts);
            }
        }
        System.out.println("Too many failed attempts. Exiting...");
    }

    private void menu() {
        while (true) {
            System.out.println("\n1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Record Attendance");
            System.out.println("4. View Attendance");
            System.out.println("5. Process Payroll");
            System.out.println("6. Backup Data");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1 -> addEmployee();
                case 2 -> viewEmployees();
                case 3 -> recordAttendance();
                case 4 -> viewAttendance();
                case 5 -> processPayroll();
                case 6 -> backupData();
                case 7 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void addEmployee() {
        System.out.print("Enter ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Department: ");
        String dept = scanner.nextLine();
        System.out.print("Enter Basic Salary: ");
        float salary = Float.parseFloat(scanner.nextLine());

        Employee emp = new Employee(id, name, dept, salary);
        writeToFile(EMPLOYEE_FILE, emp);
        System.out.println("Employee added successfully.");
    }

    private void viewEmployees() {
        List<Employee> employees = readFromFile(EMPLOYEE_FILE);
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }

        System.out.printf("%-5s %-20s %-15s %-10s\n", "ID", "Name", "Department", "Basic Salary");
        for (Employee emp : employees) {
            System.out.printf("%-5d %-20s %-15s %-10.2f\n",
                    emp.getId(), emp.getName(), emp.getDepartment(), emp.getBasicSalary());
        }
    }

    private void recordAttendance() {
        System.out.print("Enter Employee ID: ");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Days Present: ");
        int days = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Overtime Hours: ");
        int hours = Integer.parseInt(scanner.nextLine());
        System.out.print("Enter Deductions: ");
        float deduct = Float.parseFloat(scanner.nextLine());

        Attendance att = new Attendance(id, days, hours, deduct);
        writeToFile(ATTENDANCE_FILE, att);
        System.out.println("Attendance recorded.");
    }

    private void viewAttendance() {
        List<Attendance> attendanceList = readFromFile(ATTENDANCE_FILE);
        if (attendanceList.isEmpty()) {
            System.out.println("No attendance records.");
            return;
        }

        System.out.printf("%-5s %-15s %-10s %-10s\n", "ID", "Days Present", "Overtime", "Deductions");
        for (Attendance att : attendanceList) {
            System.out.printf("%-5d %-15d %-10d %-10.2f\n",
                    att.getId(), att.getDaysPresent(), att.getOvertimeHours(), att.getDeductions());
        }
    }

    private void processPayroll() {
        List<Employee> employees = readFromFile(EMPLOYEE_FILE);
        List<Attendance> attendanceList = readFromFile(ATTENDANCE_FILE);

        if (employees.isEmpty() || attendanceList.isEmpty()) {
            System.out.println("Missing employee or attendance data.");
            return;
        }

        System.out.printf("%-5s %-20s %-10s %-10s %-10s %-10s\n",
                "ID", "Name", "Basic", "Overtime", "Deductions", "Net Salary");

        for (Employee emp : employees) {
            Optional<Attendance> record = attendanceList.stream()
                    .filter(att -> att.getId() == emp.getId())
                    .findFirst();

            if (record.isPresent()) {
                Attendance att = record.get();
                float overtimePay = att.getOvertimeHours() * 100;
                float netSalary = emp.getBasicSalary() + overtimePay - att.getDeductions();
                System.out.printf("%-5d %-20s %-10.2f %-10.2f %-10.2f %-10.2f\n",
                        emp.getId(), emp.getName(), emp.getBasicSalary(), overtimePay, att.getDeductions(), netSalary);
            } else {
                System.out.printf("%-5d %-20s %-10.2f %-10s %-10s %-10s\n",
                        emp.getId(), emp.getName(), emp.getBasicSalary(), "N/A", "N/A", "N/A");
            }
        }
    }

    private void backupData() {
        copyFile(EMPLOYEE_FILE, EMPLOYEE_BACKUP);
        copyFile(ATTENDANCE_FILE, ATTENDANCE_BACKUP);
    }

    private <T> void writeToFile(String filename, T obj) {
        List<T> list = readFromFile(filename);
        list.add(obj);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(list);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + filename);
        }
    }

    private <T> List<T> readFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    private void copyFile(String src, String dest) {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            in.transferTo(out);
            System.out.println("Backup of " + src + " completed.");
        } catch (IOException e) {
            System.out.println("Error backing up " + src);
        }
    }
}
