package com.barangay.system.ui;

import com.barangay.system.model.Admin;
import com.barangay.system.model.DocRequest;
import com.barangay.system.service.AdminService;
import com.barangay.system.service.DocRequestService;
import com.barangay.system.service.DocTypeService;
import com.barangay.system.service.HistoryService;
import com.barangay.system.service.RegistrationService;
import com.barangay.system.service.ResidentService;
import com.barangay.system.ui.admin.DocTypeMenu;
import com.barangay.system.ui.admin.HistoryMenu;
import com.barangay.system.ui.admin.RegistrationsMenu;
import com.barangay.system.ui.admin.RequestsMenu;
import com.barangay.system.ui.admin.ResidentsMenu;

// Orchestrator — delegates to sub-menu classes.
public class AdminMenu {

    private final AdminService        adminSvc;
    private final ResidentService     residentSvc;
    private final DocTypeService      docTypeSvc;
    private final DocRequestService   requestSvc;
    private final RegistrationService regSvc;
    private final HistoryService      historySvc;
    private Admin                     currentAdmin;

    public AdminMenu(AdminService adminSvc, ResidentService residentSvc,
                     DocTypeService docTypeSvc, DocRequestService requestSvc,
                     RegistrationService regSvc, HistoryService historySvc,
                     Admin currentAdmin) {
        this.adminSvc    = adminSvc;
        this.residentSvc = residentSvc;
        this.docTypeSvc  = docTypeSvc;
        this.requestSvc  = requestSvc;
        this.regSvc      = regSvc;
        this.historySvc  = historySvc;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        // Instantiate sub-menus once; each handles its own loop.
        RequestsMenu      requestsMenu      = new RequestsMenu(requestSvc, historySvc, currentAdmin);
        DocTypeMenu       docTypeMenu       = new DocTypeMenu(docTypeSvc, historySvc, currentAdmin);
        ResidentsMenu     residentsMenu     = new ResidentsMenu(residentSvc, historySvc, currentAdmin);
        RegistrationsMenu registrationsMenu = new RegistrationsMenu(regSvc, historySvc, currentAdmin);
        HistoryMenu       historyMenu       = new HistoryMenu(historySvc);

        boolean running = true;
        while (running) {
            try {
                printDashboard();
                int choice = Console.readInt("Enter choice: ");
                switch (choice) {
                    case 1: requestsMenu.show();      break;
                    case 2: docTypeMenu.show();       break;
                    case 3: residentsMenu.show();     break;
                    case 4: registrationsMenu.show(); break;
                    case 5: historyMenu.show();       break;
                    case 6: changePassword();         break;
                    case 0:
                        Console.centered("Goodbye, " + currentAdmin.getFullName() + "!");
                        running = false;
                        break;
                    default: Console.centered("[!!]  Invalid choice.");
                }
            } catch (Exception e) {
                Console.centered("[!!]  Error: " + e.getMessage());
            }
        }
    }

    private void printDashboard() {
        int pendingReq = requestSvc.getByStatus(DocRequest.PENDING).size();
        int pendingReg = regSvc.getPending().size();

        System.out.println();
        Console.divider();
        Console.centered(Console.TITLE);
        Console.centered(currentAdmin.getFullName() + "  |  " + currentAdmin.getPosition());
        if (pendingReq > 0) Console.centered("[!]   " + pendingReq + " request(s) awaiting action");
        if (pendingReg > 0) Console.centered("[!]   " + pendingReg + " registration(s) awaiting approval");
        if (pendingReq == 0 && pendingReg == 0) Console.centered("All clear.");
        Console.divider();
        Console.menu(new String[]{
            "[ 1 ]  Requests",
            "[ 2 ]  Document Types",
            "[ 3 ]  Residents",
            "[ 4 ]  Registrations",
            "[ 5 ]  History Log",
            "[ 6 ]  My Account",
            "[ 0 ]  Log Out"
        });
    }

    private void changePassword() {
        Console.header("CHANGE PASSWORD");
        System.out.println();

        String currentPw = Console.readPassword("Current Password : ");
        String newPw      = Console.readPassword("New Password     : ");
        String confirmPw  = Console.readPassword("Confirm Password : ");

        if (!newPw.equals(confirmPw)) {
            Console.result("ERROR: New passwords do not match.");
            Console.pressEnter();
            return;
        }
        Console.result(adminSvc.changePassword(currentAdmin.getId(), currentPw, newPw));
        Console.pressEnter();
    }
}