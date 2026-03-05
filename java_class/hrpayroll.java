public class PayrollException extends Exception {
public PayrollException(String message) {
super(message);
}
}
public class InvalidEmployeeException extends PayrollException {
public InvalidEmployeeException(String message) {
super(message);
}
}
public class InvalidSalaryException extends PayrollException {
public InvalidSalaryException(String message) {
super(message);
}
}
---------------- Employee.java ----------------
public abstract class Employee implements Comparable<Employee> {
protected int id;
protected String name;
protected String department;
protected double baseSalary;
public Employee(int id, String name,
String department,
double baseSalary)
throws InvalidEmployeeException {
if (id <= 0)
throw new InvalidEmployeeException("Invalid ID");
// TODO:
// Validate name length >= 3
// Validate department not null or empty
// Validate baseSalary >= 0

if(name==null || name.trim().length() < 3) throw new InvalidEmployeeException("Invalid employee");
if(department == null || department.trim().isEmpty()) throw new InavlidEmployeeException("Invalid department");
if(baseSalary <0) throw new InvalidSalaryException("Salary should be greater than zero");

this.id = id;
this.name = name;
this.department = department;
this.baseSalary = baseSalary;
}
public abstract double calculateSalary()
throws InvalidSalaryException;
@Override
public int compareTo(Employee other) {
// TODO:
// Sort in descending order of salary
try{
    return  Double.compare(other.calculateSalary(), this.calculateSalary());
}
catch(throw new InvalidSalaryException e);
return 0;
}
}
public String getDepartment() {
return department;
}
}
---------------- FullTimeEmployee.java ----------------
public class FullTimeEmployee extends Employee {
private double hra;
private double bonus;
public FullTimeEmployee(int id, String name,
String dept,
double baseSalary,
double hra,
double bonus)

throws InvalidEmployeeException {
super(id, name, dept, baseSalary);
// TODO:
// Validate hra >= 0
// Validate bonus >= 0
if(hra < =0)  throw new InvalidEmployeeException("Not be negative");
if(bonus <=0) throw new InvalidEmployeeException("Not be negative");
this.hra = hra;
this.bonus = bonus;
}
@Override
public double calculateSalary()
throws InvalidSalaryException {
// TODO:
// gross = baseSalary + hra + bonus
// tax = applyTax(gross)
// return gross - tax
    double gross = baseSalary + hra + bonus;

    if(gross<0) throw new InvalidSalaryException("Not negative");
    double tax = applyTax(gross);
return  gross - tax;
}
protected double applyTax(double gross) {
// TODO:
// Apply slab logic
if(gross < 5000)  
return 0;
}
}
---------------- ContractEmployee.java ----------------
public class ContractEmployee extends Employee {
private double hourlyRate;
private int hoursWorked;
public ContractEmployee(int id, String name,
String dept,
double hourlyRate,
int hoursWorked)
throws InvalidEmployeeException {
super(id, name, dept, 0);
// TODO:
// Validate hourlyRate > 0
// Validate hoursWorked >= 0
this.hourlyRate = hourlyRate;
this.hoursWorked = hoursWorked;
}
@Override
public double calculateSalary()
throws InvalidSalaryException {
// TODO:
// return hourlyRate * hoursWorked
return 0;
}
}
---------------- Manager.java ----------------
public class Manager extends FullTimeEmployee {
private double leadershipBonus;
public Manager(int id, String name,
String dept,
double baseSalary,
double hra,
double bonus,

double leadershipBonus)
throws InvalidEmployeeException {
super(id, name, dept, baseSalary, hra, bonus);
this.leadershipBonus = leadershipBonus;
}
@Override
public double calculateSalary()
throws InvalidSalaryException {
// TODO:
// Get full-time salary
double ft = super.calculateSalary();
return  ft + leadershipBonus;
this.leadershipBonus = leadershipBonus;
// Add leadershipBonus
return 0;
}
}
---------------- PayrollService.java ----------------
import java.util.*;
import java.util.stream.*;
public class PayrollService {
private List<Employee> employees = new ArrayList<>();
public void addEmployee(Employee emp) {
employees.add(emp);
}
public double totalPayrollCost() {
// TODO:
// Use Stream API
    return employees.stream()
                .mapToDouble(emp -> {
                    try{
                        return emp.calculateSalary();
                    }
                    catch(InvalidSalaryException e);
                    return 0;
                })  
                .sum();
}
public List<Employee> top3HighestPaid() {
// TODO:
// Sort and limit 3
return employees.stream()
        .sorted()
        .limit(3);
        .collect(collectors(toList()));
return null;
}
public Map<String, Double> averageSalaryByDepartment() {
// TODO:
// groupingBy department
return null;
}
public void payrollReport() {
// TODO:
// Print total payroll
// Print department averages
// Print top 3 employees
}
}
---------------- MainApplication.java ----------------
public class MainApplication {
public static void main(String[] args) throws Exception {
PayrollService service = new PayrollService();
Employee e1 = new FullTimeEmployee(
1, "Arjun", "IT",
60000, 10000, 5000);
Employee e2 = new ContractEmployee(
2, "Ravi", "Finance",
500, 120);

Employee e3 = new Manager(
3, "Meera", "IT",
90000, 15000, 10000,
20000);
service.addEmployee(e1);
service.addEmployee(e2);
service.addEmployee(e3);
service.payrollReport();
}
}