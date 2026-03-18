package com.barangay.system.ui.admin;

import com.barangay.system.model.Admin;
import com.barangay.system.model.DocType;
import com.barangay.system.service.DocTypeService;
import com.barangay.system.service.HistoryService;
import com.barangay.system.ui.Console;

import java.util.List;

public class DocTypeMenu {

    private final DocTypeService docTypeSvc;
    private final HistoryService historySvc;
    private final Admin          currentAdmin;

    public DocTypeMenu(DocTypeService docTypeSvc, HistoryService historySvc, Admin currentAdmin) {
        this.docTypeSvc   = docTypeSvc;
        this.historySvc   = historySvc;
        this.currentAdmin = currentAdmin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Console.header("DOCUMENT TYPES");
            printTypeTable(docTypeSvc.getAll());
            Console.menu(new String[]{
                "[ 1 ]  New Type",
                "[ 2 ]  Edit Type",
                "[ 3 ]  Archive Type",
                "[ 4 ]  Restore Type",
                "[ 5 ]  Remove Type",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 0: running = false; break;
                case 1: newType();       break;
                case 2: editType();      break;
                case 3: archiveType();   break;
                case 4: restoreType();   break;
                case 5: removeType();    break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    private void newType() {
        Console.header("NEW TYPE");
        System.out.println();
        String name = Console.readTitleCase("Type Name   : ");
        String desc = Console.readSentence("Description : ");
        double fee  = Console.readDouble("Fee (0 = FREE): ");
        String result = docTypeSvc.add(name, desc, fee);
        Console.result(result);
        if (result.startsWith("SUCCESS"))
            historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                "ADDED_DOCTYPE", 0, "DOCTYPE", "Added type: " + name);
        Console.pressEnter();
    }

    private void editType() {
        Console.header("EDIT TYPE");
        int id = Console.readInt("Type ID to edit  [ 0 = Back ]: ");
        if (id == 0) return;

        DocType dt = docTypeSvc.getById(id);
        if (dt == null) { Console.result("ERROR: Not found."); Console.pressEnter(); return; }

        String name = Console.readTitleCase("Name [ " + dt.getTypeName() + " ]: ");
        if (name.isEmpty()) name = dt.getTypeName();

        String desc = Console.readSentence("Desc [ " + dt.getDescription() + " ]: ");
        if (desc.isEmpty()) desc = dt.getDescription();

        String feeStr = Console.readLine("Fee  [ " + Console.fmtFee(dt.getFee()) + " ]: ");
        double fee = feeStr.isEmpty() ? dt.getFee() : Double.parseDouble(feeStr);

        String result = docTypeSvc.edit(id, name, desc, fee);
        Console.result(result);
        if (result.startsWith("SUCCESS"))
            historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                "EDITED_DOCTYPE", id, "DOCTYPE", "Edited type ID " + id);
        Console.pressEnter();
    }

    private void archiveType() {
        Console.header("ARCHIVE TYPE");
        int id = Console.readInt("Type ID to archive [ 0 = Back ]: ");
        if (id == 0) return;
        String result = docTypeSvc.archive(id);
        Console.result(result);
        if (result.startsWith("SUCCESS"))
            historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                "ARCHIVED_DOCTYPE", id, "DOCTYPE", "Archived type ID " + id);
        Console.pressEnter();
    }

    private void restoreType() {
        Console.header("RESTORE TYPE");
        int id = Console.readInt("Type ID to restore [ 0 = Back ]: ");
        if (id == 0) return;
        String result = docTypeSvc.restore(id);
        Console.result(result);
        if (result.startsWith("SUCCESS"))
            historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                "RESTORED_DOCTYPE", id, "DOCTYPE", "Restored type ID " + id);
        Console.pressEnter();
    }

    private void removeType() {
        Console.header("REMOVE TYPE");
        int id = Console.readInt("Type ID to remove  [ 0 = Back ]: ");
        if (id == 0) return;
        if (Console.confirm("Permanently remove type ID " + id + "?")) {
            String result = docTypeSvc.remove(id);
            Console.result(result);
            if (result.startsWith("SUCCESS"))
                historySvc.log(currentAdmin.getId(), currentAdmin.getFullName(),
                    "REMOVED_DOCTYPE", id, "DOCTYPE", "Removed type ID " + id);
        } else {
            Console.centered("Aborted.");
        }
        Console.pressEnter();
    }

    private void printTypeTable(List<DocType> list) {
        if (list.isEmpty()) { Console.noResults("document types"); return; }
        System.out.println();
        Console.separator();
        Console.row(Console.FMT_DOCTYPE, "ID", "Type Name", "Fee", "Status");
        Console.separator();
        for (DocType dt : list) {
            Console.row(Console.FMT_DOCTYPE,
                String.valueOf(dt.getId()),
                Console.cut(dt.getTypeName(), 32),
                Console.fmtFee(dt.getFee()),
                dt.isArchived() ? "Archived" : "Active");
        }
        Console.separator();
    }
}