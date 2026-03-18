package com.barangay.system.ui.admin;

import com.barangay.system.model.History;
import com.barangay.system.service.HistoryService;
import com.barangay.system.ui.Console;

import java.util.List;

public class HistoryMenu {

    private final HistoryService historySvc;

    public HistoryMenu(HistoryService historySvc) {
        this.historySvc = historySvc;
    }

    public void show() {
        boolean running = true;
        while (running) {
            Console.header("HISTORY LOG");
            Console.menu(new String[]{
                "[ 1 ]  View All Entries",
                "[ 2 ]  Find Entries",
                "[ 3 ]  Filter by Type",
                "[ 0 ]  Back"
            });
            int choice = Console.readInt("Enter choice: ");
            switch (choice) {
                case 1: viewAll();      break;
                case 2: findEntries();  break;
                case 3: filterByType(); break;
                case 0: running = false; break;
                default: Console.centered("[!!]  Invalid choice.");
            }
        }
    }

    // All entries in chronological order (oldest first).
    private void viewAll() {
        Console.header("ALL HISTORY ENTRIES");
        List<History> list = historySvc.getAll();
        if (list.isEmpty()) { Console.noResults("history entries"); Console.pressEnter(); return; }
        System.out.println();
        Console.count(list.size(), "entry");
        printHistoryTable(list);
        Console.pressEnter();
    }

    private void findEntries() {
        Console.header("FIND ENTRIES");
        String kw = Console.readLine("Keyword (admin name / action / description): ");
        if (kw.isEmpty()) { Console.result("ERROR: Keyword cannot be empty."); Console.pressEnter(); return; }

        List<History> list = historySvc.search(kw);
        if (list.isEmpty()) {
            Console.noResults("entries matching '" + kw + "'");
        } else {
            System.out.println();
            Console.count(list.size(), "result");
            printHistoryTable(list);
        }
        Console.pressEnter();
    }

    private void filterByType() {
        Console.header("FILTER BY TYPE");
        Console.menu(new String[]{
            "[ 1 ]  REQUEST",
            "[ 2 ]  DOCTYPE",
            "[ 3 ]  REGISTRATION",
            "[ 4 ]  RESIDENT",
            "[ 0 ]  Back"
        });
        int choice = Console.readInt("Select type: ");
        String[] types = { null, "REQUEST", "DOCTYPE", "REGISTRATION", "RESIDENT" };
        if (choice < 0 || choice >= types.length || choice == 0) return;

        List<History> list = historySvc.getByType(types[choice]);
        if (list.isEmpty()) {
            Console.noResults("entries for type " + types[choice]);
        } else {
            System.out.println();
            Console.count(list.size(), "entry");
            printHistoryTable(list);
        }
        Console.pressEnter();
    }

    private void printHistoryTable(List<History> list) {
        Console.separator();
        Console.row(Console.FMT_HISTORY, "ID", "Admin", "Action", "Type", "Date");
        Console.separator();
        for (History h : list) {
            Console.row(Console.FMT_HISTORY,
                String.valueOf(h.getId()),
                Console.cut(h.getAdminName(), 16),
                Console.cut(h.getAction(), 20),
                Console.cut(h.getTargetType(), 8),
                Console.fmtDate(h.getActedAt()));
            if (h.getDescription() != null && !h.getDescription().isEmpty())
                System.out.printf("           %s%n", h.getDescription());
            Console.separator();
        }
    }
}