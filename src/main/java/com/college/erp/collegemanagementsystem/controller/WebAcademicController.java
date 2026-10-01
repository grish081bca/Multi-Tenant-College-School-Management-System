package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
public class WebAcademicController {
    private static final String MODULES = "departments|faculties|programs|academic-years|semesters|sections|subjects|program-subjects|students|student-enrollments";
    private final AcademicManagementService service;
    private final EntityChangeLogService changeLogs;

    public WebAcademicController(AcademicManagementService service, EntityChangeLogService changeLogs) {
        this.service = service; this.changeLogs = changeLogs;
    }

    @GetMapping("/web/{module:" + MODULES + "}")
    public String list(@PathVariable String module, @RequestParam(required = false) String q,
                       @RequestParam(required = false) Status status,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size, Model model) {
        model.addAttribute("page", service.search(module, q, status, page, size));
        model.addAttribute("module", module); model.addAttribute("moduleTitle", title(module));
        model.addAttribute("q", q); model.addAttribute("selectedStatus", status); model.addAttribute("statuses", Status.values());
        var filters = WebPagination.filters(); filters.put("q", q); filters.put("status", status);
        WebPagination.add(model, "/web/" + module, size, filters);
        return "academic-list";
    }

    @GetMapping("/web/{module:" + MODULES + "}/add")
    public String addForm(@PathVariable String module, Model model) {
        prepareForm(module, new AcademicRecordDTO(), model); model.addAttribute("isEdit", false); return "academic-form";
    }

    @PostMapping("/web/{module:" + MODULES + "}")
    public String create(@PathVariable String module, @ModelAttribute AcademicRecordDTO form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", bindingMessage(binding)); flash.addFlashAttribute("academicForm", form);
            return "redirect:/web/" + module + "/add";
        }
        try { service.save(module, null, form, null); flash.addFlashAttribute("success", title(module) + " created successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("academicForm", form); return "redirect:/web/" + module + "/add"; }
        return "redirect:/web/" + module;
    }

    @GetMapping("/web/{module:" + MODULES + "}/{id}/edit")
    public String editForm(@PathVariable String module, @PathVariable Long id, Model model, RedirectAttributes flash) {
        try { prepareForm(module, service.get(module, id), model); model.addAttribute("isEdit", true); return "academic-form"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/" + module; }
    }

    @PostMapping("/web/{module:" + MODULES + "}/{id}")
    public String update(@PathVariable String module, @PathVariable Long id, @ModelAttribute AcademicRecordDTO form, BindingResult binding,
                         @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) {
            flash.addFlashAttribute("error", bindingMessage(binding)); flash.addFlashAttribute("academicForm", form);
            return "redirect:/web/" + module + "/" + id + "/edit";
        }
        try { service.save(module, id, form, remarks); flash.addFlashAttribute("success", title(module) + " updated successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("academicForm", form); return "redirect:/web/" + module + "/" + id + "/edit"; }
        return "redirect:/web/" + module;
    }

    @GetMapping("/web/{module:" + MODULES + "}/{id}")
    public String view(@PathVariable String module, @PathVariable Long id, Model model, RedirectAttributes flash) {
        try { AcademicRecordDTO record = service.get(module, id); model.addAttribute("record", record); model.addAttribute("module", module); model.addAttribute("moduleTitle", title(module)); model.addAttribute("changeLogs", changeLogs.getRecentChanges(entityName(module), id)); return "academic-detail"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:/web/" + module; }
    }

    @PostMapping("/web/{module:" + MODULES + "}/{id}/status")
    public String status(@PathVariable String module, @PathVariable Long id, @RequestParam Status status,
                         @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        try { service.changeStatus(module, id, status, remarks); flash.addFlashAttribute("success", title(module) + " status updated successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/" + module;
    }

    @PostMapping("/web/{module:" + MODULES + "}/{id}/delete")
    public String delete(@PathVariable String module, @PathVariable Long id, RedirectAttributes flash) {
        try { service.delete(module, id); flash.addFlashAttribute("success", title(module) + " deleted successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:/web/" + module;
    }

    private void prepareForm(String module, AcademicRecordDTO record, Model model) {
        if (model.containsAttribute("academicForm")) record = (AcademicRecordDTO) model.asMap().get("academicForm");
        model.addAttribute("record", record); model.addAttribute("module", module); model.addAttribute("moduleTitle", title(module));
        model.addAttribute("statuses", Status.values());
        model.addAttribute("today", java.time.LocalDate.now());
        switch (module) {
            case "departments" -> model.addAttribute("faculties", service.options("faculties"));
            case "programs" -> { model.addAttribute("faculties", service.options("faculties")); model.addAttribute("departments", service.options("departments")); }
            case "sections" -> { model.addAttribute("programs", service.options("programs")); model.addAttribute("academicYears", service.options("academic-years")); model.addAttribute("semesters", service.options("semesters")); }
            case "program-subjects" -> { model.addAttribute("programs", service.options("programs")); model.addAttribute("semesters", service.options("semesters")); model.addAttribute("subjects", service.options("subjects")); }
            case "student-enrollments" -> { model.addAttribute("students", service.options("students")); model.addAttribute("programs", service.options("programs")); model.addAttribute("academicYears", service.options("academic-years")); model.addAttribute("semesters", service.options("semesters")); model.addAttribute("sections", service.options("sections")); }
            default -> { }
        }
    }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
    private String bindingMessage(BindingResult binding) { return binding.getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(java.util.stream.Collectors.joining("; ")); }
    private String title(String value) { return switch(value) { case "departments" -> "Department"; case "faculties" -> "Faculty"; case "programs" -> "Program"; case "academic-years" -> "Academic Year"; case "semesters" -> "Semester"; case "sections" -> "Section"; case "subjects" -> "Subject"; case "program-subjects" -> "Program Subject"; case "students" -> "Student"; case "student-enrollments" -> "Student Enrollment"; default -> "Academic record"; }; }
    private String entityName(String module) { return switch (module) { case "academic-years" -> "AcademicYear"; case "program-subjects" -> "ProgramSubject"; case "student-enrollments" -> "StudentEnrollment"; default -> title(module).replace(" ", ""); }; }
}
