package com.barangay.system.main;

import com.barangay.system.database.DbConnection;
import com.barangay.system.model.Admin;
import com.barangay.system.model.Resident;
import com.barangay.system.repo.AdminRepo;
import com.barangay.system.repo.DocRequestRepo;
import com.barangay.system.repo.DocTypeRepo;
import com.barangay.system.repo.HistoryRepo;
import com.barangay.system.repo.RegistrationRepo;
import com.barangay.system.repo.ResidentRepo;
import com.barangay.system.service.AdminService;
import com.barangay.system.service.DocRequestService;
import com.barangay.system.service.DocTypeService;
import com.barangay.system.service.HistoryService;
import com.barangay.system.service.RegistrationService;
import com.barangay.system.service.ResidentService;
import com.barangay.system.ui.AdminMenu;
import com.barangay.system.ui.AuthMenu;
import com.barangay.system.ui.Console;
import com.barangay.system.ui.ResidentMenu;

import java.sql.SQLException;

public class App {

    public static void main(String[] args) {

        // 1. Verify database connection.
        System.out.println();
        Console.divider();
        Console.centered("Connecting to database...");

        try {
            DbConnection.getInstance();
            Console.centered("Connection established successfully.");
            Console.divider();
        } catch (SQLException e) {
            Console.centered("[!!]  FATAL: Cannot connect to the database.");
            Console.centered("Reason: " + e.getMessage());
            Console.centered("Ensure XAMPP MySQL is running and db_brdrms_v1 exists.");
            Console.centered("Run the SQL schema, then run Setup.java.");
            System.out.println();
            return;
        }

        // 2. Wire repos -> services -> menus.
        AdminRepo        adminRepo   = new AdminRepo();
        ResidentRepo     resRepo     = new ResidentRepo();
        DocTypeRepo      typeRepo    = new DocTypeRepo();
        DocRequestRepo   requestRepo = new DocRequestRepo();
        RegistrationRepo regRepo     = new RegistrationRepo();
        HistoryRepo      histRepo    = new HistoryRepo();

        AdminService       adminSvc    = new AdminService(adminRepo);
        ResidentService    resSvc      = new ResidentService(resRepo);
        DocTypeService     typeSvc     = new DocTypeService(typeRepo);
        DocRequestService  requestSvc  = new DocRequestService(requestRepo);
        RegistrationService regSvc     = new RegistrationService(regRepo, resRepo);
        HistoryService     histSvc     = new HistoryService(histRepo);

        AuthMenu authMenu = new AuthMenu(adminSvc, resSvc, regSvc);

        // 3. Application loop — returns to auth screen after each logout.
        boolean running = true;
        while (running) {
            try {
                Object user = authMenu.show();
                if (user == null) {
                    running = false;
                } else if (user instanceof Admin) {
                    new AdminMenu(adminSvc, resSvc, typeSvc, requestSvc, regSvc, histSvc,
                                  (Admin) user).show();
                } else if (user instanceof Resident) {
                    new ResidentMenu(resSvc, typeSvc, requestSvc, (Resident) user).show();
                }
            } catch (Exception e) {
                Console.centered("[!!]  Critical error: " + e.getMessage());
                Console.centered("Restarting...");
            }
        }

        Console.divider();
        Console.centered("System shutdown. Goodbye!");
        Console.divider();
    }
}