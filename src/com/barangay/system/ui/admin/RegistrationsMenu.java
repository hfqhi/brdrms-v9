package com.barangay.system.ui.admin;

import com.barangay.system.model.Admin;
import com.barangay.system.model.Registration;
import com.barangay.system.service.HistoryService;
import com.barangay.system.service.RegistrationService;
import com.barangay.system.ui.Console;

import java.util.List;

public class RegistrationsMenu {

    private final RegistrationService regSvc;
    private final HistoryService      historySvc;
    private final Admin               currentAdmin;

    public RegistrationsMenu(RegistrationService regSvc, HistoryService historySvc, Admin currentAdmin) {
        this.regSvc      = regSvc;
        this.historySvc  = historySvc;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Console.header("REGISTRATIONS");
            Console.menu(new String[]{
                "[ 1 ]  View Pending",
                "[ 2 ]  Approve",
                "[ 3 ]  Reject",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewPending(); break;
                case 2: approve();     break;
                case 3: reject();      break;
                case 0: running = false; break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void viewPending() {
        Console.header("PENDING REGISTRATIONS");
        List<Registration> list = regSvc.getPending();
        if (list.isEmpty()) { Console.noResults("pending registrations"); Console.pressEnter(); return; }
        System.out.println();
        Console.count(list.size(), "pending registration");
        printRegTable(list);
        Console.pressEnter();
    }

    private void approve() {
        Console.header("APPROVE REGISTRATION");
        List<Registration> list = regSvc.getPending();
        if (list.isEmpty()) { Console.noResults("pending registrations"); Console.pressEnter(); return; }
        printRegTable(list);
        System.out.println();

        int id = Console.readInt("Registration ID to approve [ 0 = Back ]: ");
        if (id == 0) return;

        Registration reg = regSvc.getById(id);
        if (reg != null) printRegDetail(reg);

        if (Console.confirm("Approve registration #" + id + "?")) {
            String result = regSvc.approve(id, currentAdmin.getId());
            Console.result(result);
            if (result.startsWith("SUCCESS") && reg != null)
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "APPROVED_REG", id, "REGISTRATION", "Approved registration for " + reg.getFullName());
        } else {
            Console.centered("Aborted.");
        }
        Console.pressEnter();
    }

    private void reject() {
        Console.header("REJECT REGISTRATION");
        List<Registration> list = regSvc.getPending();
        if (list.isEmpty()) { Console.noResults("pending registrations"); Console.pressEnter(); return; }
        printRegTable(list);
        System.out.println();

        int id = Console.readInt("Registration ID to reject [ 0 = Back ]: ");
        if (id == 0) return;

        Registration reg = regSvc.getById(id);
        if (reg != null) printRegDetail(reg);

        if (Console.confirm("Reject registration #" + id + "?")) {
            String result = regSvc.reject(id, currentAdmin.getId());
            Console.result(result);
            if (result.startsWith("SUCCESS") && reg != null)
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "REJECTED_REG", id, "REGISTRATION", "Rejected registration for " + reg.getFullName());
        } else {
            Console.centered("Aborted.");
        }
        Console.pressEnter();
    }

    private void printRegTable(List<Registration> list) {
        Console.separator();
        Console.row(Console.FMT_REGISTRATION, "ID", "Username", "Full Name", "Contact", "Submitted");
        Console.separator();
        for (Registration r : list) {
            Console.row(Console.FMT_REGISTRATION,
                String.valueOf(r.getId()),
                Console.cut(r.getUsername(), 14),
                Console.cut(r.getFullName(), 20),
                Console.cut(r.getContactNumber(), 13),
                Console.fmtDate(r.getSubmittedAt()));
        }
        Console.separator();
    }

    private void printRegDetail(Registration r) {
        System.out.println();
        Console.separator();
        System.out.printf("  %-18s %s%n", "ID",             r.getId());
        System.out.printf("  %-18s %s%n", "Username",       r.getUsername());
        System.out.printf("  %-18s %s%n", "Full Name",      r.getFullName());
        System.out.printf("  %-18s %s%n", "Contact Number", r.getContactNumber());
        System.out.printf("  %-18s %s%n", "Address",        r.getAddress());
        System.out.printf("  %-18s %s%n", "Submitted",      Console.fmtDate(r.getSubmittedAt()));
        Console.separator();
    }
}