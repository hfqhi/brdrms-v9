package com.barangay.system.ui;

import com.barangay.system.model.DocumentRequest;
import com.barangay.system.model.User;
import com.barangay.system.service.DocumentRequestService;
import com.barangay.system.service.UserService;

import java.util.List;

/*
 * Console menu for logged-in administrators (role = ADMIN).
 *
 * UI features:
 *   - Control panel shows a live PENDING count as a reminder
 *   - viewAllRequests() groups requests into PENDING / APPROVED / REJECTED sections
 *     with per-section counts and summary totals
 *   - approveOrRejectRequest() lists all PENDING requests with purpose visible
 *     BEFORE asking for an ID, then shows the selected detail again to confirm
 *   - viewRequestDetails() shows a compact overview BEFORE asking for an ID
 *   - deleteRequest() shows compact overview + full detail BEFORE confirming
 *   - All separators are 80 characters wide
 *
 * Admin remarks use readSentenceCase() so they are stored consistently.
 */
public class AdminMenu {

    private final UserService userService;
    private final DocumentRequestService requestService;
    private final User adminUser;

    public AdminMenu(UserService userService,
            DocumentRequestService requestService,
            User adminUser) {
        this.userService = userService;
        this.requestService = requestService;
        this.adminUser = adminUser;
    }

    // ----------------------------------------------------------
    // Main admin loop
    // ----------------------------------------------------------
    public void show() {
        boolean running = true;

        while (running) {
            printControlPanel();
            int choice = ConsoleHelper.readInt("  Enter choice: ");

            switch (choice) {
                case 1:
                    viewAllRequests();
                    break;
                case 2:
                    approveOrRejectRequest();
                    break;
                case 3:
                    viewRequestDetails();
                    break;
                case 4:
                    deleteRequest();
                    break;
                case 5:
                    viewAllUsers();
                    break;
                case 6:
                    System.out.println(
                            "\n  Logged out. Goodbye, " + adminUser.getFullName() + "!");
                    running = false;
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ----------------------------------------------------------
    // Admin control panel with live PENDING count
    // ----------------------------------------------------------
    private void printControlPanel() {
        List<DocumentRequest> pending
                = requestService.getRequestsByStatus(DocumentRequest.STATUS_PENDING);
        int pendingCount = pending.size();

        System.out.println();
        ConsoleHelper.printDivider();
        System.out.println("  ADMIN CONTROL PANEL");
        System.out.println("  Logged in as : " + adminUser.getFullName());
        if (pendingCount > 0) {
            System.out.println("  *** " + pendingCount
                    + " PENDING request(s) awaiting your action ***");
        } else {
            System.out.println("  No pending requests at the moment.");
        }
        ConsoleHelper.printDivider();
        System.out.println("  [1] View ALL Requests  (grouped by status)");
        System.out.println("  [2] Approve / Reject a Request");
        System.out.println("  [3] View Full Request Details");
        System.out.println("  [4] Delete a Request");
        System.out.println("  [5] View All Registered Residents");
        System.out.println("  [6] Log Out");
        ConsoleHelper.printSeparator();
    }

    // ----------------------------------------------------------
    // Show all requests grouped into PENDING / APPROVED / REJECTED
    // with per-section counts and a grand total summary
    // ----------------------------------------------------------
    private void viewAllRequests() {
        ConsoleHelper.printHeader("ALL DOCUMENT REQUESTS");

        List<DocumentRequest> pending
                = requestService.getRequestsByStatus(DocumentRequest.STATUS_PENDING);
        List<DocumentRequest> approved
                = requestService.getRequestsByStatus(DocumentRequest.STATUS_APPROVED);
        List<DocumentRequest> rejected
                = requestService.getRequestsByStatus(DocumentRequest.STATUS_REJECTED);

        int total = pending.size() + approved.size() + rejected.size();

        if (total == 0) {
            System.out.println("  No requests in the system yet.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        System.out.println("  Total: " + total
                + "  |  Pending: " + pending.size()
                + "  |  Approved: " + approved.size()
                + "  |  Rejected: " + rejected.size());

        printAdminStatusSection("PENDING", pending);
        printAdminStatusSection("APPROVED", approved);
        printAdminStatusSection("REJECTED", rejected);

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Show all PENDING requests with purpose visible, then prompt
    // for an ID. Show the selected detail again before confirming.
    // Admin remarks use readSentenceCase().
    // ----------------------------------------------------------
    private void approveOrRejectRequest() {
        ConsoleHelper.printHeader("APPROVE / REJECT A REQUEST");

        List<DocumentRequest> pending
                = requestService.getRequestsByStatus(DocumentRequest.STATUS_PENDING);

        if (pending.isEmpty()) {
            System.out.println("  There are no PENDING requests to process.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        // Show all PENDING requests with purpose so the admin can make an
        // informed decision before typing an ID
        System.out.println("  PENDING requests awaiting your action:");
        System.out.println();
        printDetailedTable(pending);

        int requestId = ConsoleHelper.readInt(
                "\n  Enter Request ID to process (0 to go back): ");
        if (requestId == 0) {
            return;
        }

        DocumentRequest dr = requestService.getRequestById(requestId);

        if (dr == null) {
            ConsoleHelper.printResult("ERROR: Request #" + requestId + " not found.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }
        if (!DocumentRequest.STATUS_PENDING.equals(dr.getStatus())) {
            ConsoleHelper.printResult(
                    "ERROR: Request #" + requestId + " is already " + dr.getStatus()
                    + ". Only PENDING requests can be processed here.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        // Show the selected request's full detail before confirming
        System.out.println("\n  You selected:");
        printRequestDetail(dr);

        System.out.println();
        System.out.println("  [1] APPROVE this request");
        System.out.println("  [2] REJECT  this request");
        System.out.println("  [0] Cancel");

        int action = ConsoleHelper.readInt("  Action: ");

        if (action == 0) {
            System.out.println("  Operation cancelled.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        String newStatus;
        if (action == 1) {
            newStatus = DocumentRequest.STATUS_APPROVED;
        } else if (action == 2) {
            newStatus = DocumentRequest.STATUS_REJECTED;
        } else {
            System.out.println("  [!] Invalid action.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        // Sentence case for admin remarks
        String remarks = ConsoleHelper.readSentenceCase(
                "  Admin Remarks (optional, press Enter to skip): ");

        String result = requestService.processRequest(requestId, newStatus, remarks);
        ConsoleHelper.printResult(result);
        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Shows a compact overview of all requests first, then prompts
    // for an ID and displays the full detail block
    // ----------------------------------------------------------
    private void viewRequestDetails() {
        ConsoleHelper.printHeader("VIEW FULL REQUEST DETAILS");

        List<DocumentRequest> all = requestService.getAllRequests();

        if (all.isEmpty()) {
            System.out.println("  No requests in the system.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        System.out.println("  All requests (select an ID to view full details):");
        printCompactAdminTable(all);
        System.out.println();

        int requestId = ConsoleHelper.readInt(
                "  Enter Request ID for full details (0 to go back): ");
        if (requestId == 0) {
            return;
        }

        DocumentRequest dr = requestService.getRequestById(requestId);

        if (dr == null) {
            ConsoleHelper.printResult("ERROR: Request #" + requestId + " not found.");
        } else {
            System.out.println();
            printRequestDetail(dr);
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Shows compact overview, then the selected detail, then
    // asks for confirmation before deleting
    // ----------------------------------------------------------
    private void deleteRequest() {
        ConsoleHelper.printHeader("DELETE A REQUEST");

        List<DocumentRequest> all = requestService.getAllRequests();

        if (all.isEmpty()) {
            System.out.println("  No requests in the system.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        System.out.println("  All requests (select an ID to delete):");
        printCompactAdminTable(all);
        System.out.println();

        int requestId = ConsoleHelper.readInt(
                "  Enter Request ID to delete (0 to cancel): ");
        if (requestId == 0) {
            return;
        }

        DocumentRequest dr = requestService.getRequestById(requestId);
        if (dr == null) {
            ConsoleHelper.printResult("ERROR: Request #" + requestId + " not found.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        // Show the full detail before asking for confirmation
        System.out.println("\n  You are about to permanently delete:");
        printRequestDetail(dr);

        String confirm = ConsoleHelper.readLine(
                "\n  WARNING: This cannot be undone. "
                + "Delete Request #" + requestId + "? (yes/no): ");

        if ("yes".equalsIgnoreCase(confirm)) {
            String result = requestService.deleteRequest(requestId);
            ConsoleHelper.printResult(result);
        } else {
            System.out.println("  Deletion aborted.");
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // List all registered residents
    // ----------------------------------------------------------
    private void viewAllUsers() {
        ConsoleHelper.printHeader("ALL REGISTERED RESIDENTS");

        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("  No users found.");
        } else {
            ConsoleHelper.printSeparator();
            System.out.printf("  %-5s  %-18s  %-24s  %-7s  %-19s%n",
                    "ID", "Username", "Full Name", "Role", "Joined");
            ConsoleHelper.printSeparator();

            for (User u : users) {
                String joined = u.getCreatedAt() != null
                        ? u.getCreatedAt().toString().substring(0, 19) : "-";
                System.out.printf("  %-5d  %-18s  %-24s  %-7s  %-19s%n",
                        u.getId(), u.getUsername(), u.getFullName(), u.getRole(), joined);
            }
            ConsoleHelper.printSeparator();
            System.out.println("  Total registered residents: " + users.size());
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Prints a labeled status section with a framed header and count.
    // Skips the section entirely when the list is empty.
    // ----------------------------------------------------------
    private void printAdminStatusSection(String label, List<DocumentRequest> requests) {
        if (requests.isEmpty()) {
            return;
        }

        System.out.println();

        if (label.equals("PENDING")) {
            System.out.println("  +--------------------------------------------------------------+");
            System.out.println("  |  PENDING  (" + requests.size() + ")  -  Awaiting admin decision          |");
            System.out.println("  +--------------------------------------------------------------+");
        } else if (label.equals("APPROVED")) {
            System.out.println("  +--------------------------------------------------------------+");
            System.out.println("  |  APPROVED (" + requests.size() + ")  -  Released to resident              |");
            System.out.println("  +--------------------------------------------------------------+");
        } else {
            System.out.println("  +--------------------------------------------------------------+");
            System.out.println("  |  REJECTED (" + requests.size() + ")  -  See admin remarks                |");
            System.out.println("  +--------------------------------------------------------------+");
        }

        ConsoleHelper.printSeparator();
        System.out.printf("  %-5s  %-20s  %-24s  %-19s%n",
                "ID", "Resident", "Document Type", "Date");
        ConsoleHelper.printSeparator();

        for (DocumentRequest dr : requests) {
            String date = dr.getRequestDate() != null
                    ? dr.getRequestDate().toString().substring(0, 19) : "-";
            System.out.printf("  %-5d  %-20s  %-24s  %-19s%n",
                    dr.getId(),
                    dr.getFullName() != null ? dr.getFullName() : "-",
                    dr.getDocumentType(),
                    date);
            System.out.printf("        Purpose  : %s%n", dr.getPurpose());
            if (dr.getAdminRemarks() != null && !dr.getAdminRemarks().isEmpty()) {
                System.out.printf("        Remarks  : %s%n", dr.getAdminRemarks());
            }
            ConsoleHelper.printSeparator();
        }
    }

    // ----------------------------------------------------------
    // Compact single-line table used before ID-selection prompts
    // ----------------------------------------------------------
    private void printCompactAdminTable(List<DocumentRequest> requests) {
        ConsoleHelper.printSeparator();
        System.out.printf("  %-5s  %-20s  %-24s  %-10s%n",
                "ID", "Resident", "Document Type", "Status");
        ConsoleHelper.printSeparator();
        for (DocumentRequest dr : requests) {
            System.out.printf("  %-5d  %-20s  %-24s  %-10s%n",
                    dr.getId(),
                    dr.getFullName() != null ? dr.getFullName() : "-",
                    dr.getDocumentType(),
                    dr.getStatus());
        }
        ConsoleHelper.printSeparator();
    }

    // ----------------------------------------------------------
    // Detailed table with purpose visible - used in
    // approveOrRejectRequest() so admin can read before deciding
    // ----------------------------------------------------------
    private void printDetailedTable(List<DocumentRequest> requests) {
        ConsoleHelper.printSeparator();
        System.out.printf("  %-5s  %-20s  %-24s  %-19s%n",
                "ID", "Resident", "Document Type", "Date Requested");
        ConsoleHelper.printSeparator();

        for (DocumentRequest dr : requests) {
            String date = dr.getRequestDate() != null
                    ? dr.getRequestDate().toString().substring(0, 19) : "-";
            System.out.printf("  %-5d  %-20s  %-24s  %-19s%n",
                    dr.getId(),
                    dr.getFullName() != null ? dr.getFullName() : "-",
                    dr.getDocumentType(),
                    date);
            System.out.printf("        Purpose  : %s%n", dr.getPurpose());
            ConsoleHelper.printSeparator();
        }
    }

    // ----------------------------------------------------------
    // Full detail block for a single request
    // ----------------------------------------------------------
    private void printRequestDetail(DocumentRequest dr) {
        ConsoleHelper.printSeparator();
        System.out.println("  Request ID    : " + dr.getId());
        System.out.println("  Resident      : " + dr.getFullName()
                + "  (@" + dr.getUsername() + ")");
        System.out.println("  Document Type : " + dr.getDocumentType());
        System.out.println("  Purpose       : " + dr.getPurpose());
        System.out.println("  Status        : " + dr.getStatus());
        System.out.println("  Requested On  : " + dr.getRequestDate());
        System.out.println("  Processed On  : "
                + (dr.getProcessedDate() != null
                ? dr.getProcessedDate().toString()
                : "Not yet processed"));
        System.out.println("  Admin Remarks : "
                + (dr.getAdminRemarks() != null && !dr.getAdminRemarks().isEmpty()
                ? dr.getAdminRemarks() : "(none)"));
        ConsoleHelper.printSeparator();
    }
}
