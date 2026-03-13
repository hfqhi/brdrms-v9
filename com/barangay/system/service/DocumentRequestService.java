package com.barangay.system.service;

import com.barangay.system.model.DocumentRequest;
import com.barangay.system.repository.DocumentRequestRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * Business-logic layer for document request operations.
 * Validates all inputs before delegating to the repository.
 * Returns plain-text result messages for the UI to display.
 *
 * Expects purpose strings to already be Sentence-Cased
 * by the UI layer (ConsoleHelper.toSentenceCase).
 */
public class DocumentRequestService {

    private final DocumentRequestRepository requestRepository;

    public DocumentRequestService(DocumentRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    // ----------------------------------------------------------
    // SUBMIT - validate then insert a new PENDING request.
    // documentTypeIdx is 0-based (UI passes choice - 1).
    // ----------------------------------------------------------
    public String submitRequest(int userId, int documentTypeIdx, String purpose) {
        if (documentTypeIdx < 0 || documentTypeIdx >= DocumentRequest.DOCUMENT_TYPES.length) {
            return "ERROR: Invalid document type selection.";
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            return "ERROR: Purpose cannot be empty.";
        }

        String documentType = DocumentRequest.DOCUMENT_TYPES[documentTypeIdx];

        try {
            DocumentRequest request = new DocumentRequest(
                    userId, documentType, purpose.trim());
            boolean saved = requestRepository.save(request);
            return saved
                    ? "SUCCESS: Request for '" + documentType
                    + "' submitted. Request ID: " + request.getId()
                    : "ERROR: Failed to submit request.";
        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // GET BY USER - resident's own request list
    // ----------------------------------------------------------
    public List<DocumentRequest> getRequestsByUser(int userId) {
        try {
            return requestRepository.findByUserId(userId);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return new ArrayList<DocumentRequest>();
        }
    }

    // ----------------------------------------------------------
    // GET BY USER + STATUS - resident's segregated request view
    // and for showing only PENDING before edit/cancel prompts
    // ----------------------------------------------------------
    public List<DocumentRequest> getRequestsByUserAndStatus(int userId, String status) {
        try {
            return requestRepository.findByUserIdAndStatus(userId, status);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return new ArrayList<DocumentRequest>();
        }
    }

    // ----------------------------------------------------------
    // GET ALL - admin full list
    // ----------------------------------------------------------
    public List<DocumentRequest> getAllRequests() {
        try {
            return requestRepository.findAll();
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return new ArrayList<DocumentRequest>();
        }
    }

    // ----------------------------------------------------------
    // GET BY STATUS - admin filter
    // ----------------------------------------------------------
    public List<DocumentRequest> getRequestsByStatus(String status) {
        try {
            return requestRepository.findByStatus(status);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return new ArrayList<DocumentRequest>();
        }
    }

    // ----------------------------------------------------------
    // GET SINGLE BY ID
    // ----------------------------------------------------------
    public DocumentRequest getRequestById(int id) {
        try {
            return requestRepository.findById(id);
        } catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return null;
        }
    }

    // ----------------------------------------------------------
    // EDIT PURPOSE - resident can only edit their own PENDING requests
    // ----------------------------------------------------------
    public String updateRequestPurpose(int requestId, int userId, String newPurpose) {
        if (newPurpose == null || newPurpose.trim().isEmpty()) {
            return "ERROR: Purpose cannot be empty.";
        }

        try {
            DocumentRequest request = requestRepository.findById(requestId);

            if (request == null) {
                return "ERROR: Request not found.";
            }
            if (request.getUserId() != userId) {
                return "ERROR: You can only edit your own requests.";
            }
            if (!DocumentRequest.STATUS_PENDING.equals(request.getStatus())) {
                return "ERROR: Only PENDING requests can be edited.";
            }

            request.setPurpose(newPurpose.trim());
            boolean updated = requestRepository.updatePurpose(request);
            return updated
                    ? "SUCCESS: Request updated successfully."
                    : "ERROR: Update failed (request may no longer be PENDING).";

        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // PROCESS REQUEST - admin approves or rejects
    // ----------------------------------------------------------
    public String processRequest(int requestId, String newStatus, String adminRemarks) {
        if (!DocumentRequest.STATUS_APPROVED.equals(newStatus)
                && !DocumentRequest.STATUS_REJECTED.equals(newStatus)) {
            return "ERROR: Invalid status. Use APPROVED or REJECTED.";
        }

        try {
            DocumentRequest request = requestRepository.findById(requestId);
            if (request == null) {
                return "ERROR: Request ID " + requestId + " not found.";
            }

            boolean updated = requestRepository.updateStatus(
                    requestId, newStatus,
                    (adminRemarks == null ? "" : adminRemarks.trim())
            );
            return updated
                    ? "SUCCESS: Request #" + requestId + " has been " + newStatus + "."
                    : "ERROR: Could not update request status.";

        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // CANCEL - resident cancels their own PENDING request
    // ----------------------------------------------------------
    public String cancelRequest(int requestId, int userId) {
        try {
            DocumentRequest request = requestRepository.findById(requestId);
            if (request == null) {
                return "ERROR: Request not found.";
            }
            if (request.getUserId() != userId) {
                return "ERROR: You can only cancel your own requests.";
            }
            if (!DocumentRequest.STATUS_PENDING.equals(request.getStatus())) {
                return "ERROR: Only PENDING requests can be cancelled.";
            }

            boolean deleted = requestRepository.deleteByIdAndUserId(requestId, userId);
            return deleted
                    ? "SUCCESS: Request #" + requestId + " has been cancelled."
                    : "ERROR: Cancellation failed.";

        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }

    // ----------------------------------------------------------
    // DELETE - admin removes any request (data cleanup)
    // ----------------------------------------------------------
    public String deleteRequest(int requestId) {
        try {
            boolean deleted = requestRepository.deleteById(requestId);
            return deleted
                    ? "SUCCESS: Request #" + requestId + " deleted."
                    : "ERROR: Request not found.";
        } catch (SQLException e) {
            return "ERROR: Database error - " + e.getMessage();
        }
    }
}
