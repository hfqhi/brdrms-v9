package com.barangay.system.ui.admin;

import com.barangay.system.model.Admin;
import com.barangay.system.model.DocRequest;
import com.barangay.system.service.DocRequestService;
import com.barangay.system.service.HistoryService;
import com.barangay.system.ui.Console;

import java.util.List;

public class RequestsMenu {

    private final DocRequestService requestSvc;
    private final HistoryService    historySvc;
    private final Admin             currentAdmin;

    public RequestsMenu(DocRequestService requestSvc, HistoryService historySvc, Admin currentAdmin) {
        this.requestSvc  = requestSvc;
        this.historySvc  = historySvc;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Console.header("REQUESTS");
            Console.menu(new String[]{
                "[ 1 ]  View Requests",
                "[ 2 ]  Find Requests",
                "[ 3 ]  Process Request",
                "[ 4 ]  Archive Request",
                "[ 5 ]  View Archive",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewRequests();   break;
                case 2: findRequests();   break;
                case 3: processRequest(); break;
                case 4: archiveRequest(); break;
                case 5: viewArchive();    break;
                case 0: running = false;  break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void viewRequests() {
        Console.header("ALL REQUESTS");

        List<DocRequest> pending  = requestSvc.getByStatus(DocRequest.PENDING);
        List<DocRequest> approved = requestSvc.getByStatus(DocRequest.APPROVED);
        List<DocRequest> rejected = requestSvc.getByStatus(DocRequest.REJECTED);
        int total = pending.size() + approved.size() + rejected.size();

        if (total == 0) { Console.noResults("active requests"); Console.pressEnter(); return; }

        System.out.println();
        Console.centered("Total: " + total + "  |  Pending: " + pending.size() +
            "  |  Approved: " + approved.size() + "  |  Rejected: " + rejected.size());
        printAdminSection("PENDING",  pending);
        printAdminSection("APPROVED", approved);
        printAdminSection("REJECTED", rejected);
        Console.pressEnter();
    }

    private void printAdminSection(String label, List<DocRequest> list) {
        if (list.isEmpty()) return;
        System.out.println();
        Console.subHeader(label + "  (" + list.size() + ")");
        Console.row(Console.FMT_REQUEST_ADMIN, "ID", "Resident", "Document Type", "Status");
        Console.separator();
        for (DocRequest dr : list) {
            Console.row(Console.FMT_REQUEST_ADMIN,
                DocRequestService.fmt(dr.getId()),
                Console.cut(dr.getResidentName(), 20),
                Console.cut(dr.getDocTypeName(), 22),
                dr.getStatus());
            System.out.printf("           Purpose  : %s%n", dr.getPurpose());
            Console.separator();
        }
    }

    private void findRequests() {
        Console.header("FIND REQUESTS");

        String kw = Console.readLine("Keyword (resident / document / purpose): ");
        if (kw.isEmpty()) { Console.result("ERROR: Keyword cannot be empty."); Console.pressEnter(); return; }

        List<DocRequest> list = requestSvc.search(kw);
        if (list.isEmpty()) {
            Console.noResults("requests matching '" + kw + "'");
        } else {
            System.out.println();
            Console.count(list.size(), "result");
            printRequestTable(list);
        }
        Console.pressEnter();
    }

    private void processRequest() {
        Console.header("PROCESS REQUEST");

        List<DocRequest> pending = requestSvc.getByStatus(DocRequest.PENDING);
        if (pending.isEmpty()) { Console.noResults("PENDING requests"); Console.pressEnter(); return; }

        System.out.println();
        Console.centered("PENDING requests:");
        printRequestTable(pending);
        System.out.println();

        int id = Console.readInt("Request ID to process [ 0 = Back ]: ");
        if (id == 0) return;

        DocRequest req = requestSvc.getById(id);
        if (req == null) { Console.result("ERROR: " + DocRequestService.fmt(id) + " not found."); Console.pressEnter(); return; }
        if (!req.isPending()) { Console.result("ERROR: " + DocRequestService.fmt(id) + " is already " + req.getStatus() + "."); Console.pressEnter(); return; }

        printRequestDetail(req);
        Console.menu(new String[]{
            "[ 1 ]  Approve",
            "[ 2 ]  Reject",
            "[ 0 ]  Back"
        });

        int action = Console.readInt("Action: ");
        if (action == 0) return;
        String status = action == 1 ? DocRequest.APPROVED : action == 2 ? DocRequest.REJECTED : null;
        if (status == null) { Console.centered("[!!]  Invalid action."); Console.pressEnter(); return; }

        String remarks = Console.readSentence("Remarks (Enter to skip): ");
        String result  = requestSvc.process(id, status, remarks, currentAdmin.getId());
        Console.result(result);
        if (result.startsWith("SUCCESS"))
            historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                status + "_REQUEST", id, "REQUEST",
                status + " " + DocRequestService.fmt(id) + " for " + req.getResidentName());
        Console.pressEnter();
    }

    private void archiveRequest() {
        Console.header("ARCHIVE REQUEST");

        List<DocRequest> all = requestSvc.getAll();
        if (all.isEmpty()) { Console.noResults("active requests"); Console.pressEnter(); return; }

        printRequestTable(all);
        System.out.println();

        int id = Console.readInt("Request ID to archive [ 0 = Back ]: ");
        if (id == 0) return;

        DocRequest req = requestSvc.getById(id);
        if (req != null) printRequestDetail(req);

        if (Console.confirm("Archive " + DocRequestService.fmt(id) + "?")) {
            String result = requestSvc.archive(id);
            Console.result(result);
            if (result.startsWith("SUCCESS") && req != null)
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "ARCHIVED_REQUEST", id, "REQUEST", "Archived " + DocRequestService.fmt(id));
        } else {
            Console.centered("Aborted.");
        }
        Console.pressEnter();
    }

    private void viewArchive() {
        Console.header("ARCHIVE");

        List<DocRequest> archived = requestSvc.getAllArchived();
        if (archived.isEmpty()) { Console.noResults("archived requests"); Console.pressEnter(); return; }

        System.out.println();
        Console.count(archived.size(), "archived request");
        printRequestTable(archived);
        System.out.println();
        Console.menu(new String[]{
            "[ 1 ]  Restore Request",
            "[ 2 ]  Remove Request",
            "[ 0 ]  Back"
        });

        int action = Console.readInt("Action: ");
        if (action == 0) return;

        if (action == 1) {
            int id = Console.readInt("Request ID to restore [ 0 = Back ]: ");
            if (id == 0) return;
            String result = requestSvc.restore(id);
            Console.result(result);
            if (result.startsWith("SUCCESS"))
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "RESTORED_REQUEST", id, "REQUEST", "Restored " + DocRequestService.fmt(id));

        } else if (action == 2) {
            int id = Console.readInt("Request ID to remove  [ 0 = Back ]: ");
            if (id == 0) return;
            DocRequest req = requestSvc.getByIdAny(id);
            if (req != null) printRequestDetail(req);
            if (Console.confirm("WARNING: Cannot be undone. Remove " + DocRequestService.fmt(id) + "?")) {
                String result = requestSvc.remove(id);
                Console.result(result);
                if (result.startsWith("SUCCESS"))
                    historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                        "REMOVED_REQUEST", id, "REQUEST", "Permanently removed " + DocRequestService.fmt(id));
            } else {
                Console.centered("Aborted.");
            }
        } else {
            Console.centered("[!!]  Invalid action.");
        }
        Console.pressEnter();
    }

    void printRequestTable(List<DocRequest> list) {
        Console.separator();
        Console.row(Console.FMT_REQUEST_ADMIN, "ID", "Resident", "Document Type", "Status");
        Console.separator();
        for (DocRequest dr : list) {
            Console.row(Console.FMT_REQUEST_ADMIN,
                DocRequestService.fmt(dr.getId()),
                Console.cut(dr.getResidentName(), 20),
                Console.cut(dr.getDocTypeName(), 22),
                dr.getStatus());
        }
        Console.separator();
    }

    void printRequestDetail(DocRequest dr) {
        System.out.println();
        Console.separator();
        System.out.printf("  %-18s %s%n", "Request ID",    DocRequestService.fmt(dr.getId()));
        System.out.printf("  %-18s %s%n", "Resident",      dr.getResidentName());
        System.out.printf("  %-18s %s%n", "Document Type", dr.getDocTypeName());
        System.out.printf("  %-18s %s%n", "Fee",           Console.fmtFee(dr.getDocFee()));
        System.out.printf("  %-18s %s%n", "Purpose",       dr.getPurpose());
        System.out.printf("  %-18s %s%n", "Status",        dr.getStatus());
        System.out.printf("  %-18s %s%n", "Requested On",  Console.fmtDate(dr.getRequestDate()));
        System.out.printf("  %-18s %s%n", "Processed On",
            dr.getProcessedDate() != null ? Console.fmtDate(dr.getProcessedDate()) : "Not yet processed");
        System.out.printf("  %-18s %s%n", "Processed By",
            dr.getAdminName() != null ? dr.getAdminName() : "-");
        System.out.printf("  %-18s %s%n", "Remarks",
            (dr.getAdminRemarks() != null && !dr.getAdminRemarks().isEmpty())
                ? dr.getAdminRemarks() : "(none)");
        Console.separator();
    }
}