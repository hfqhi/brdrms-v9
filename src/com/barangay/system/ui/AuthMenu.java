package com.barangay.system.ui;

import com.barangay.system.model.Admin;
import com.barangay.system.model.Resident;
import com.barangay.system.service.AdminService;
import com.barangay.system.service.RegistrationService;
import com.barangay.system.service.ResidentService;

// Returns Admin or Resident on success, null on Exit.
public class AuthMenu {

    private final AdminService        adminSvc;
    private final ResidentService     residentSvc;
    private final RegistrationService regSvc;

    public AuthMenu(AdminService adminSvc, ResidentService residentSvc,
                    RegistrationService regSvc) {
        this.adminSvc    = adminSvc;
        this.residentSvc = residentSvc;
        this.regSvc      = regSvc;
    }

    public Object show() {
        while (true) {
            try {
                printWelcome();
                int choice = Console.readInt("Enter choice: ");
                switch (choice) {
                    case 1: { Object user = doLogin(); if (user != null) return user; break; }
                    case 2: doRegister(); break;
                    case 0:
                        System.out.println();
                        Console.centered("Thank you for using the Barangay System. Goodbye!");
                        System.out.println();
                        return null;
                    default: Console.centered("[!!]  Invalid choice.");
                }
            } catch (Exception e) {
                Console.centered("[!!]  Error: " + e.getMessage());
            }
        }
    }

    private void printWelcome() {
        System.out.println();
        Console.divider();
        Console.centered(Console.TITLE);
        Console.centered(Console.SUBTITLE);
        Console.divider();
        Console.menu(new String[]{
            "[ 1 ]  Log In",
            "[ 2 ]  Register",
            "[ 0 ]  Exit"
        });
    }

    // Tries admin login first, then resident.
    private Object doLogin() {
        Console.header("LOG IN");

        String username = Console.readLine("Username : ");
        String password = Console.readPassword("Password : ");

        Admin admin = adminSvc.login(username, password);
        if (admin != null) {
            System.out.println();
            Console.centered("Welcome, " + admin.getFullName() + "  [ " + admin.getPosition() + " ]");
            Console.pressEnter();
            return admin;
        }

        Resident resident = residentSvc.login(username, password);
        if (resident != null) {
            System.out.println();
            Console.centered("Welcome back, " + resident.getFullName() + "!");
            Console.pressEnter();
            return resident;
        }

        // Show the resident error message as primary feedback.
        Console.result(residentSvc.getLoginMsg());
        Console.pressEnter();
        return null;
    }

    private void doRegister() {
        Console.header("REGISTER  --  NEW RESIDENT ACCOUNT");
        System.out.println();
        Console.centered("Your account will be reviewed by a barangay admin before you can log in.");
        System.out.println();

        String username = Console.readLine("Username       : ");
        String password = Console.readPassword("Password       : ");
        String fullName = Console.readTitleCase("Full Name      : ");
        String contact  = Console.readLine("Contact Number : ");
        String address  = Console.readTitleCase("Home Address   : ");

        Console.result(regSvc.submit(username, password, fullName, contact, address));
        Console.pressEnter();
    }
}