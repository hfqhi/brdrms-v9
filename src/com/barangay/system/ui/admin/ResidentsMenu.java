package com.barangay.system.ui.admin;

import com.barangay.system.model.Admin;
import com.barangay.system.model.Resident;
import com.barangay.system.service.HistoryService;
import com.barangay.system.service.ResidentService;
import com.barangay.system.ui.Console;

import java.util.List;

public class ResidentsMenu {

    private final ResidentService residentSvc;
    private final HistoryService  historySvc;
    private final Admin           currentAdmin;

    public ResidentsMenu(ResidentService residentSvc, HistoryService historySvc, Admin currentAdmin) {
        this.residentSvc  = residentSvc;
        this.historySvc   = historySvc;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Console.header("RESIDENTS");
            Console.menu(new String[]{
                "[ 1 ]  View Residents",
                "[ 2 ]  Find Residents",
                "[ 3 ]  Archive Resident",
                "[ 4 ]  Archived Residents",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewResidents();    break;
                case 2: findResidents();    break;
                case 3: archiveResident();  break;
                case 4: viewArchived();     break;
                case 0: running = false;    break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void viewResidents() {
        Console.header("ALL RESIDENTS");
        List<Resident> list = residentSvc.getAll();
        if (list.isEmpty()) { Console.noResults("residents"); Console.pressEnter(); return; }
        System.out.println();
        Console.count(list.size(), "resident");
        printResidentTable(list);
        Console.pressEnter();
    }

    private void findResidents() {
        Console.header("FIND RESIDENTS");
        String kw = Console.readLine("Keyword (name / username / contact): ");
        if (kw.isEmpty()) { Console.result("ERROR: Keyword cannot be empty."); Console.pressEnter(); return; }
        List<Resident> list = residentSvc.search(kw);
        if (list.isEmpty()) {
            Console.noResults("residents matching '" + kw + "'");
        } else {
            System.out.println();
            Console.count(list.size(), "result");
            printResidentTable(list);
        }
        Console.pressEnter();
    }

    private void archiveResident() {
        Console.header("ARCHIVE RESIDENT");

        List<Resident> list = residentSvc.getAll();
        if (list.isEmpty()) { Console.noResults("active residents"); Console.pressEnter(); return; }

        printResidentTable(list);
        System.out.println();

        int id = Console.readInt("Resident ID to archive [ 0 = Back ]: ");
        if (id == 0) return;

        Resident r = residentSvc.getById(id);
        if (r != null) printResidentDetail(r);

        if (Console.confirm("Archive resident ID " + id + "?")) {
            String result = residentSvc.archive(id);
            Console.result(result);
            if (result.startsWith("SUCCESS") && r != null)
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "ARCHIVED_RESIDENT", id, "RESIDENT", "Archived " + r.getFullName());
        } else {
            Console.centered("Aborted.");
        }
        Console.pressEnter();
    }

    // Archived residents sub-section: restore or permanently delete.
    private void viewArchived() {
        Console.header("ARCHIVED RESIDENTS");

        List<Resident> archived = residentSvc.getAllArchived();
        if (archived.isEmpty()) { Console.noResults("archived residents"); Console.pressEnter(); return; }

        System.out.println();
        Console.count(archived.size(), "archived resident");
        printResidentTable(archived);
        System.out.println();
        Console.menu(new String[]{
            "[ 1 ]  Restore Resident",
            "[ 2 ]  Remove Resident",
            "[ 0 ]  Back"
        });

        int action = Console.readInt("Action: ");
        if (action == 0) return;

        if (action == 1) {
            int id = Console.readInt("Resident ID to restore [ 0 = Back ]: ");
            if (id == 0) return;
            Resident r = residentSvc.getById(id);
            String result = residentSvc.restore(id);
            Console.result(result);
            if (result.startsWith("SUCCESS") && r != null)
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "RESTORED_RESIDENT", id, "RESIDENT", "Restored " + r.getFullName());

        } else if (action == 2) {
            int id = Console.readInt("Resident ID to remove  [ 0 = Back ]: ");
            if (id == 0) return;
            Resident r = residentSvc.getById(id);
            if (r != null) printResidentDetail(r);
            if (Console.confirm("WARNING: Permanently remove resident ID " + id +
                    " and all their requests?")) {
                String result = residentSvc.delete(id);
                Console.result(result);
                if (result.startsWith("SUCCESS") && r != null)
                    historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                        "REMOVED_RESIDENT", id, "RESIDENT", "Permanently removed " + r.getFullName());
            } else {
                Console.centered("Aborted.");
            }
        } else {
            Console.centered("[!!]  Invalid action.");
        }
        Console.pressEnter();
    }

    private void printResidentTable(List<Resident> list) {
        Console.separator();
        Console.row(Console.FMT_RESIDENT, "ID", "Username", "Full Name", "Contact");
        Console.separator();
        for (Resident r : list) {
            Console.row(Console.FMT_RESIDENT,
                String.valueOf(r.getId()),
                Console.cut(r.getUsername(), 16),
                Console.cut(r.getFullName(), 22),
                Console.cut(r.getContactNumber(), 15));
        }
        Console.separator();
    }

    private void printResidentDetail(Resident r) {
        System.out.println();
        Console.separator();
        System.out.printf("  %-18s %s%n", "ID",             r.getId());
        System.out.printf("  %-18s %s%n", "Username",       r.getUsername());
        System.out.printf("  %-18s %s%n", "Full Name",      r.getFullName());
        System.out.printf("  %-18s %s%n", "Contact Number", r.getContactNumber());
        System.out.printf("  %-18s %s%n", "Address",        r.getAddress());
        System.out.printf("  %-18s %s%n", "Member Since",   Console.fmtDate(r.getCreatedAt()));
        Console.separator();
    }
}