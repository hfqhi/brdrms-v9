package com.barangay.system.util;

import com.barangay.system.model.Admin;
import com.barangay.system.model.DocType;
import com.barangay.system.repo.AdminRepo;
import com.barangay.system.repo.DocTypeRepo;
import org.mindrot.jbcrypt.BCrypt;

/*
 * Run ONCE after creating the database schema.
 * Right-click Setup.java -> Run File in NetBeans.
 * Creates the BCrypt-hashed admin account and default document types.
 */
public class Setup {

    public static void main(String[] args) {
        System.out.println("====================================================");
        System.out.println("  Barangay System V9 -- Initial Setup");
        System.out.println("====================================================");
        createAdmin();
        createDocumentTypes();
        System.out.println();
        System.out.println("  Setup complete.");
        System.out.println("  Admin login: username = admin | password = admin123");
        System.out.println("====================================================");
    }

    private static void createAdmin() {
        try {
            AdminRepo repo = new AdminRepo();
            if (repo.usernameExists("admin")) {
                System.out.println("  [SKIP] Admin account already exists."); return;
            }
            String hash = BCrypt.hashpw("admin123", BCrypt.gensalt(12));
            Admin admin = new Admin("admin", hash, "System Administrator", "Barangay Captain");
            repo.save(admin);
            System.out.println("  [OK]   Admin account created.");
        } catch (Exception e) {
            System.out.println("  [!!]   Admin setup failed: " + e.getMessage());
        }
    }

    private static void createDocumentTypes() {
        String[][] types = {
            { "Barangay Clearance",                 "General-purpose clearance certificate.", "50.00"  },
            { "Certificate of Residency",           "Proof of residence in the barangay.",    "50.00"  },
            { "Certificate of Indigency",           "Proof of indigent status.",              "0.00"   },
            { "Business Permit Endorsement",        "Endorsement for business permit.",       "100.00" },
            { "Certificate of Good Moral Character","Certificate of good moral standing.",    "50.00"  }
        };
        try {
            DocTypeRepo repo = new DocTypeRepo();
            for (String[] t : types) {
                repo.save(new DocType(t[0], t[1], Double.parseDouble(t[2])));
                System.out.println("  [OK]   Added: " + t[0]);
            }
        } catch (Exception e) {
            System.out.println("  [!!]   Document type setup failed: " + e.getMessage());
        }
    }
}