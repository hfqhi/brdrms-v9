package com.barangay.system.ui;

import com.barangay.system.model.DocRequest;
import com.barangay.system.model.DocType;
import com.barangay.system.model.Resident;
import com.barangay.system.service.DocRequestService;
import com.barangay.system.service.DocTypeService;
import com.barangay.system.service.ResidentService;

import java.util.List;

public class ResidentMenu {

    private final ResidentService   residentSvc;
    private final DocTypeService    docTypeSvc;
    private final DocRequestService requestSvc;
    private Resident                currentResident;

    public ResidentMenu(ResidentService residentSvc, DocTypeService docTypeSvc,
                        DocRequestService requestSvc, Resident currentResident) {
        this.residentSvc     = residentSvc;
        this.docTypeSvc      = docTypeSvc;
        this.requestSvc      = requestSvc;
        this.currentResident = currentResident;
    }

    public void show() {
        boolean running = true;
        while (running) {
            try {
                printDashboard();
                int choice = Console.readInt("Enter choice: ");
                switch (choice) {
                    case 1: showRequests(); break;
                    case 2: showProfile();  break;
                    case 0:
                        Console.centered("Goodbye, " + currentResident.getFullName() + "!");
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
        // Build a brief summary of pending request counts.
        int pending  = requestSvc.getByResidentAndStatus(currentResident.getId(), DocRequest.PENDING).size();
        int approved = requestSvc.getByResidentAndStatus(currentResident.getId(), DocRequest.APPROVED).size();

        System.out.println();
        Console.divider();
        Console.centered(Console.TITLE);
        Console.centered("Welcome, " + currentResident.getFullName());
        if (pending + approved > 0)
            Console.centered("Active Requests: " + (pending + approved) +
                "  ( " + pending + " Pending  |  " + approved + " Approved )");
        Console.divider();
        Console.menu(new String[]{
            "[ 1 ]  Requests",
            "[ 2 ]  Profile",
            "[ 0 ]  Log Out"
        });
    }

    // ── Requests sub-menu ──────────────────────────────────────────────────

    private void showRequests() {
        boolean running = true;
        while (running) {
            Console.header("REQUESTS");
            Console.menu(new String[]{
                "[ 1 ]  View Requests",
                "[ 2 ]  Find Requests",
                "[ 3 ]  New Request",
                "[ 4 ]  Edit Request",
                "[ 5 ]  Cancel Request",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewRequests();  break;
                case 2: findRequests();  break;
                case 3: newRequest();    break;
                case 4: editRequest();   break;
                case 5: cancelRequest(); break;
                case 0: running = false; break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void viewRequests() {
        Console.header("MY REQUESTS");

        List<DocRequest> pending  = requestSvc.getByResidentAndStatus(currentResident.getId(), DocRequest.PENDING);
        List<DocRequest> approved = requestSvc.getByResidentAndStatus(currentResident.getId(), DocRequest.APPROVED);
        List<DocRequest> rejected = requestSvc.getByResidentAndStatus(currentResident.getId(), DocRequest.REJECTED);

        if (pending.isEmpty() && approved.isEmpty() && rejected.isEmpty()) {
            Console.noResults("requests");
        } else {
            printSection("PENDING",  pending,  false);
            printSection("APPROVED", approved, true);
            printSection("REJECTED", rejected, true);
        }
        Console.pressEnter();
    }

    // showProcessed=true means the request was decided — show who processed it and when.
    private void printSection(String label, List<DocRequest> list, boolean showProcessed) {
        if (list.isEmpty()) return;
        System.out.println();
        Console.subHeader(label + "  (" + list.size() + ")");
        Console.row(Console.FMT_REQUEST_RESIDENT, "ID", "Document Type", "Fee", "Status", "Date");
        Console.separator();
        for (DocRequest dr : list) {
            Console.row(Console.FMT_REQUEST_RESIDENT,
                DocRequestService.fmt(dr.getId()),
                Console.cut(dr.getDocTypeName(), 26),
                Console.fmtFee(dr.getDocFee()),
                dr.getStatus(),
                Console.fmtDate(dr.getRequestDate()));
            System.out.printf("           Purpose  : %s%n", dr.getPurpose());
            if (showProcessed) {
                System.out.printf("           Processed: %s  by %s%n",
                    Console.fmtDate(dr.getProcessedDate()),
                    dr.getAdminName() != null ? dr.getAdminName() : "-");
            }
            if (dr.getAdminRemarks() != null && !dr.getAdminRemarks().isEmpty())
                System.out.printf("           Remarks  : %s%n", dr.getAdminRemarks());
            Console.separator();
        }
    }

    private void findRequests() {
        Console.header("FIND REQUESTS");

        String kw = Console.readLine("Keyword (document type or purpose): ");
        if (kw.isEmpty()) { Console.result("ERROR: Keyword cannot be empty."); Console.pressEnter(); return; }

        List<DocRequest> list = requestSvc.searchForResident(kw, currentResident.getId());
        if (list.isEmpty()) {
            Console.noResults("requests matching '" + kw + "'");
        } else {
            System.out.println();
            Console.count(list.size(), "result");
            printRequestTableResident(list);
        }
        Console.pressEnter();
    }

    private void newRequest() {
        Console.header("NEW REQUEST");

        List<DocType> types = docTypeSvc.getAllActive();
        if (types.isEmpty()) {
            Console.centered("[i]   No document types available. Please contact the barangay office.");
            Console.pressEnter();
            return;
        }

        System.out.println();
        Console.centered("Available Document Types:");
        System.out.println();
        Console.separator();
        Console.row(Console.FMT_DOCTYPE, "#", "Document Name", "Fee", "");
        Console.separator();
        for (int i = 0; i < types.size(); i++) {
            DocType dt = types.get(i);
            Console.row(Console.FMT_DOCTYPE,
                String.valueOf(i + 1),
                Console.cut(dt.getTypeName(), 32),
                Console.fmtFee(dt.getFee()), "");
        }
        Console.separator();
        System.out.println();

        int pick = Console.readInt("Select [1-" + types.size() + "]: ");
        if (pick < 1 || pick > types.size()) {
            Console.result("ERROR: Invalid selection.");
            Console.pressEnter();
            return;
        }

        DocType sel = types.get(pick - 1);
        System.out.println();
        Console.centered("Selected: " + sel.getTypeName() + "  |  Fee: " + Console.fmtFee(sel.getFee()));
        System.out.println();

        String purpose = Console.readSentence("Purpose / Reason: ");
        Console.result(requestSvc.submit(currentResident.getId(), sel.getId(), purpose));
        Console.pressEnter();
    }

    private void editRequest() {
        Console.header("EDIT REQUEST");

        List<DocRequest> pending = requestSvc.getByResidentAndStatus(
            currentResident.getId(), DocRequest.PENDING);
        if (pending.isEmpty()) { Console.noResults("PENDING requests to edit"); Console.pressEnter(); return; }

        System.out.println();
        Console.centered("Your PENDING requests:");
        printCompactTableResident(pending);
        System.out.println();

        int id = Console.readInt("Request ID to edit  [ 0 = Back ]: ");
        if (id == 0) return;

        String purpose = Console.readSentence("New purpose: ");
        Console.result(requestSvc.editPurpose(id, currentResident.getId(), purpose));
        Console.pressEnter();
    }

    private void cancelRequest() {
        Console.header("CANCEL REQUEST");

        List<DocRequest> pending = requestSvc.getByResidentAndStatus(
            currentResident.getId(), DocRequest.PENDING);
        if (pending.isEmpty()) { Console.noResults("PENDING requests to cancel"); Console.pressEnter(); return; }

        System.out.println();
        Console.centered("Your PENDING requests:");
        printCompactTableResident(pending);
        System.out.println();

        int id = Console.readInt("Request ID to cancel [ 0 = Back ]: ");
        if (id == 0) return;

        if (Console.confirm("Cancel " + DocRequestService.fmt(id) + "?")) {
            Console.result(requestSvc.cancel(id, currentResident.getId()));
        } else {
            Console.centered("Cancellation aborted.");
        }
        Console.pressEnter();
    }

    // ── Profile sub-menu ───────────────────────────────────────────────────

    private void showProfile() {
        boolean running = true;
        while (running) {
            Console.header("PROFILE");
            Console.menu(new String[]{
                "[ 1 ]  View Profile",
                "[ 2 ]  Edit Profile",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewProfile(); break;
                case 2: editProfile(); break;
                case 0: running = false; break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void viewProfile() {
        Console.header("MY PROFILE");
        System.out.println();
        Console.separator();
        System.out.printf("  %-18s %s%n", "ID",             currentResident.getId());
        System.out.printf("  %-18s %s%n", "Username",       currentResident.getUsername());
        System.out.printf("  %-18s %s%n", "Full Name",      currentResident.getFullName());
        System.out.printf("  %-18s %s%n", "Contact Number", currentResident.getContactNumber());
        System.out.printf("  %-18s %s%n", "Address",        currentResident.getAddress());
        System.out.printf("  %-18s %s%n", "Member Since",   Console.fmtDate(currentResident.getCreatedAt()));
        Console.separator();
        Console.pressEnter();
    }

    private void editProfile() {
        Console.header("EDIT PROFILE");
        System.out.println();
        Console.centered("Leave any field blank to keep the current value.");
        System.out.println();

        String fn  = Console.readTitleCase("Full Name      [ " + currentResident.getFullName()       + " ]: ");
        if (fn.isEmpty()) fn = currentResident.getFullName();

        String cn  = Console.readLine("Contact Number [ " + currentResident.getContactNumber()  + " ]: ");
        if (cn.isEmpty()) cn = currentResident.getContactNumber();

        String adr = Console.readTitleCase("Address        [ " + currentResident.getAddress()        + " ]: ");
        if (adr.isEmpty()) adr = currentResident.getAddress();

        String pw = Console.readPassword("New Password   [ blank = keep current ]: ");

        String result = residentSvc.updateProfile(currentResident.getId(), fn, cn, adr, pw);
        Console.result(result);
        if (result.startsWith("SUCCESS")) {
            currentResident.setFullName(fn);
            currentResident.setContactNumber(cn);
            currentResident.setAddress(adr);
        }
        Console.pressEnter();
    }

    // ── Table helpers ──────────────────────────────────────────────────────

    private void printRequestTableResident(List<DocRequest> list) {
        Console.separator();
        Console.row(Console.FMT_REQUEST_RESIDENT, "ID", "Document Type", "Fee", "Status", "Date");
        Console.separator();
        for (DocRequest dr : list) {
            Console.row(Console.FMT_REQUEST_RESIDENT,
                DocRequestService.fmt(dr.getId()),
                Console.cut(dr.getDocTypeName(), 26),
                Console.fmtFee(dr.getDocFee()),
                dr.getStatus(),
                Console.fmtDate(dr.getRequestDate()));
        }
        Console.separator();
    }

    private void printCompactTableResident(List<DocRequest> list) {
        Console.separator();
        Console.row("%-8s  %-28s  %-10s", "ID", "Document Type", "Fee");
        Console.separator();
        for (DocRequest dr : list) {
            Console.row("%-8s  %-28s  %-10s",
                DocRequestService.fmt(dr.getId()),
                Console.cut(dr.getDocTypeName(), 28),
                Console.fmtFee(dr.getDocFee()));
        }
        Console.separator();
    }
}