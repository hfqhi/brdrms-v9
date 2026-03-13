package com.barangay.system.ui;

import com.barangay.system.model.DocumentRequest;
import com.barangay.system.model.User;
import com.barangay.system.service.DocumentRequestService;
import com.barangay.system.service.UserService;

import java.util.List;

/*
 * Console menu for logged-in residents (role = USER).
 *
 * Capitalization rules applied here:
 *   - Full Name and Address inputs use readTitleCase()
 *   - Purpose / Reason inputs use readSentenceCase()
 *
 * UI features:
 *   - viewMyRequests() groups requests into PENDING / APPROVED / REJECTED sections
 *   - editRequest() shows only the user's PENDING list before asking for an ID
 *   - cancelRequest() shows only the user's PENDING list before asking for an ID
 *   - All separators are 80 characters wide
 */
public class UserMenu {

    private final UserService userService;
    private final DocumentRequestService requestService;
    private User currentUser;

    public UserMenu(UserService userService,
            DocumentRequestService requestService,
            User currentUser) {
        this.userService = userService;
        this.requestService = requestService;
        this.currentUser = currentUser;
    }

    // ----------------------------------------------------------
    // Main resident loop
    // ----------------------------------------------------------
    public void show() {
        boolean running = true;

        while (running) {
            printDashboard();
            int choice = ConsoleHelper.readInt("  Enter choice: ");

            switch (choice) {
                case 1:
                    viewMyRequests();
                    break;
                case 2:
                    submitRequest();
                    break;
                case 3:
                    editRequest();
                    break;
                case 4:
                    cancelRequest();
                    break;
                case 5:
                    viewProfile();
                    break;
                case 6:
                    updateProfile();
                    break;
                case 7:
                    System.out.println(
                            "\n  Logged out. Goodbye, " + currentUser.getFullName() + "!");
                    running = false;
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please try again.");
            }
        }
    }

    // ----------------------------------------------------------
    // Resident dashboard menu header
    // ----------------------------------------------------------
    private void printDashboard() {
        System.out.println();
        ConsoleHelper.printDivider();
        System.out.println("  RESIDENT DASHBOARD");
        System.out.println("  Logged in as : " + currentUser.getFullName()
                + "  (ID: " + currentUser.getId() + ")");
        ConsoleHelper.printDivider();
        System.out.println("  [1] View My Requests");
        System.out.println("  [2] Submit New Request");
        System.out.println("  [3] Edit a PENDING Request");
        System.out.println("  [4] Cancel a PENDING Request");
        System.out.println("  [5] View My Profile");
        System.out.println("  [6] Update My Profile");
        System.out.println("  [7] Log Out");
        ConsoleHelper.printSeparator();
    }

    // ----------------------------------------------------------
    // Show all requests grouped into PENDING / APPROVED / REJECTED
    // ----------------------------------------------------------
    private void viewMyRequests() {
        ConsoleHelper.printHeader("MY DOCUMENT REQUESTS");

        List<DocumentRequest> pending
                = requestService.getRequestsByUserAndStatus(
                        currentUser.getId(), DocumentRequest.STATUS_PENDING);
        List<DocumentRequest> approved
                = requestService.getRequestsByUserAndStatus(
                        currentUser.getId(), DocumentRequest.STATUS_APPROVED);
        List<DocumentRequest> rejected
                = requestService.getRequestsByUserAndStatus(
                        currentUser.getId(), DocumentRequest.STATUS_REJECTED);

        if (pending.isEmpty() && approved.isEmpty() && rejected.isEmpty()) {
            System.out.println("  You have no document requests yet.");
        } else {
            printStatusSection("PENDING", pending);
            printStatusSection("APPROVED", approved);
            printStatusSection("REJECTED", rejected);
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Show document type list, collect purpose, and submit.
    // Purpose uses readSentenceCase() -> only the first letter
    // is capitalized, e.g. "For employment purposes"
    // ----------------------------------------------------------
    private void submitRequest() {
        ConsoleHelper.printHeader("SUBMIT NEW DOCUMENT REQUEST");

        System.out.println("  Select the document you need:");
        System.out.println();
        for (int i = 0; i < DocumentRequest.DOCUMENT_TYPES.length; i++) {
            System.out.println("    [" + (i + 1) + "] " + DocumentRequest.DOCUMENT_TYPES[i]);
        }
        System.out.println();

        int typeChoice = ConsoleHelper.readInt(
                "  Document type [1-" + DocumentRequest.DOCUMENT_TYPES.length + "]: ");
        int typeIndex = typeChoice - 1;

        // Sentence case: first letter only is capitalized
        String purpose = ConsoleHelper.readSentenceCase("  Purpose / Reason   : ");

        String result = requestService.submitRequest(
                currentUser.getId(), typeIndex, purpose);
        ConsoleHelper.printResult(result);
        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Shows the user's PENDING list first, then asks for the ID.
    // Purpose update also uses readSentenceCase().
    // ----------------------------------------------------------
    private void editRequest() {
        ConsoleHelper.printHeader("EDIT A PENDING REQUEST");

        List<DocumentRequest> pending
                = requestService.getRequestsByUserAndStatus(
                        currentUser.getId(), DocumentRequest.STATUS_PENDING);

        if (pending.isEmpty()) {
            System.out.println("  You have no PENDING requests to edit.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        System.out.println("  Your PENDING requests:");
        printCompactTable(pending);
        System.out.println();

        int requestId = ConsoleHelper.readInt("  Enter Request ID to edit (0 to go back): ");
        if (requestId == 0) {
            return;
        }

        // Sentence case for the updated purpose
        String newPurpose = ConsoleHelper.readSentenceCase("  New purpose: ");

        String result = requestService.updateRequestPurpose(
                requestId, currentUser.getId(), newPurpose);
        ConsoleHelper.printResult(result);
        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Shows the user's PENDING list first, then asks for the ID.
    // ----------------------------------------------------------
    private void cancelRequest() {
        ConsoleHelper.printHeader("CANCEL A PENDING REQUEST");

        List<DocumentRequest> pending
                = requestService.getRequestsByUserAndStatus(
                        currentUser.getId(), DocumentRequest.STATUS_PENDING);

        if (pending.isEmpty()) {
            System.out.println("  You have no PENDING requests to cancel.");
            ConsoleHelper.pressEnterToContinue();
            return;
        }

        System.out.println("  Your PENDING requests:");
        printCompactTable(pending);
        System.out.println();

        int requestId = ConsoleHelper.readInt(
                "  Enter Request ID to cancel (0 to go back): ");
        if (requestId == 0) {
            return;
        }

        String confirm = ConsoleHelper.readLine(
                "  Are you sure you want to cancel Request #" + requestId + "? (yes/no): ");

        if ("yes".equalsIgnoreCase(confirm)) {
            String result = requestService.cancelRequest(requestId, currentUser.getId());
            ConsoleHelper.printResult(result);
        } else {
            System.out.println("  Cancellation aborted.");
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Show the current user's account details
    // ----------------------------------------------------------
    private void viewProfile() {
        ConsoleHelper.printHeader("MY PROFILE");
        ConsoleHelper.printSeparator();
        System.out.println("  ID        : " + currentUser.getId());
        System.out.println("  Username  : " + currentUser.getUsername());
        System.out.println("  Full Name : " + currentUser.getFullName());
        System.out.println("  Address   : " + currentUser.getAddress());
        System.out.println("  Role      : " + currentUser.getRole());
        System.out.println("  Joined    : " + currentUser.getCreatedAt());
        ConsoleHelper.printSeparator();
        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Prompt for updated profile fields.
    // Full Name and Address use readTitleCase().
    // Blank input keeps the current value.
    // ----------------------------------------------------------
    private void updateProfile() {
        ConsoleHelper.printHeader("UPDATE MY PROFILE");
        System.out.println("  Leave any field blank to keep the current value.\n");

        // Title Case for name and address
        String newName = ConsoleHelper.readTitleCase(
                "  Full Name [" + currentUser.getFullName() + "]: ");
        if (newName.isEmpty()) {
            newName = currentUser.getFullName();
        }

        String newAddress = ConsoleHelper.readTitleCase(
                "  Address   [" + currentUser.getAddress() + "]: ");
        if (newAddress.isEmpty()) {
            newAddress = currentUser.getAddress();
        }

        String newPassword = ConsoleHelper.readLine(
                "  New Password (min 6 chars, blank to keep): ");
        if (newPassword.isEmpty()) {
            newPassword = currentUser.getPassword();
        }

        String result = userService.updateProfile(
                currentUser.getId(), newName, newAddress, newPassword);
        ConsoleHelper.printResult(result);

        if (result.startsWith("SUCCESS")) {
            currentUser.setFullName(newName);
            currentUser.setAddress(newAddress);
            currentUser.setPassword(newPassword);
        }

        ConsoleHelper.pressEnterToContinue();
    }

    // ----------------------------------------------------------
    // Prints a labeled status section.
    // Skips the section entirely when the list is empty.
    // ----------------------------------------------------------
    private void printStatusSection(String label, List<DocumentRequest> requests) {
        if (requests.isEmpty()) {
            return;
        }

        String banner;
        if (label.equals("PENDING")) {
            banner = "  [ PENDING  - Awaiting admin action ]";
        } else if (label.equals("APPROVED")) {
            banner = "  [ APPROVED - Ready for release      ]";
        } else {
            banner = "  [ REJECTED - See remarks below      ]";
        }

        System.out.println();
        System.out.println(banner);
        ConsoleHelper.printSeparator();
        System.out.printf("  %-5s  %-32s  %-20s%n", "ID", "Document Type", "Date Requested");
        ConsoleHelper.printSeparator();

        for (DocumentRequest dr : requests) {
            System.out.printf("  %-5d  %-32s  %-20s%n",
                    dr.getId(),
                    dr.getDocumentType(),
                    dr.getRequestDate() != null
                    ? dr.getRequestDate().toString().substring(0, 19) : "-");
            System.out.printf("        Purpose  : %s%n", dr.getPurpose());
            if (dr.getAdminRemarks() != null && !dr.getAdminRemarks().isEmpty()) {
                System.out.printf("        Remarks  : %s%n", dr.getAdminRemarks());
            }
            ConsoleHelper.printSeparator();
        }
    }

    // ----------------------------------------------------------
    // Compact single-line table shown before an ID-selection prompt
    // ----------------------------------------------------------
    private void printCompactTable(List<DocumentRequest> requests) {
        ConsoleHelper.printSeparator();
        System.out.printf("  %-5s  %-32s  %-30s%n", "ID", "Document Type", "Purpose");
        ConsoleHelper.printSeparator();
        for (DocumentRequest dr : requests) {
            String purpose = dr.getPurpose();
            if (purpose.length() > 28) {
                purpose = purpose.substring(0, 25) + "...";
            }
            System.out.printf("  %-5d  %-32s  %-30s%n",
                    dr.getId(), dr.getDocumentType(), purpose);
        }
        ConsoleHelper.printSeparator();
    }
}
