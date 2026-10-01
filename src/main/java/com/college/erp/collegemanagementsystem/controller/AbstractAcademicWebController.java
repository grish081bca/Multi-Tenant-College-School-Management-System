package com.college.erp.collegemanagementsystem.controller;

import com.college.erp.collegemanagementsystem.dto.AcademicRecordDTO;
import com.college.erp.collegemanagementsystem.enums.Status;
import com.college.erp.collegemanagementsystem.service.AcademicManagementService;
import com.college.erp.collegemanagementsystem.service.AcademicModuleService;
import com.college.erp.collegemanagementsystem.service.EntityChangeLogService;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

/** Common web workflow; module-specific controllers provide distinct routes, services and JSP entry points. */
public abstract class AbstractAcademicWebController {
    private final AcademicModuleService service;
    private final AcademicManagementService workflow;
    private final EntityChangeLogService changeLogs;

    protected AbstractAcademicWebController(AcademicModuleService service, AcademicManagementService workflow, EntityChangeLogService changeLogs) {
        this.service = service; this.workflow = workflow; this.changeLogs = changeLogs;
    }
    protected abstract String module();
    private String base() { return "/web/" + module(); }
    private String title() { return switch (module()) { case "departments" -> "Department"; case "faculties" -> "Faculty"; case "programs" -> "Program"; case "academic-years" -> "Academic Year"; case "semesters" -> "Semester"; case "sections" -> "Section"; case "subjects" -> "Subject"; case "program-subjects" -> "Program Subject"; case "students" -> "Student"; default -> "Student Enrollment"; }; }
    private String viewPrefix() { return module(); }

    @GetMapping
    public String list(@RequestParam Map<String, String> parameters, Model model) {
        Map<String, String> filters = new LinkedHashMap<>();
        for (Map<String, String> field : filterFields()) {
            String value = parameters.get(field.get("key"));
            if (value != null && !value.isBlank()) filters.put(field.get("key"), value);
        }
        String q = parameters.get("q");
        Status status = parseStatus(parameters.get("status"));
        Integer page = parseInt(parameters.get("page"), 1), size = parseInt(parameters.get("size"), 10);
        model.addAttribute("page", service.search(q, filters, status, page, size));
        model.addAttribute("module", module()); model.addAttribute("moduleTitle", title());
        model.addAttribute("q", q); model.addAttribute("selectedStatus", status); model.addAttribute("statuses", Status.values());
        model.addAttribute("academicFilters", filterFields()); model.addAttribute("selectedFilters", filters);
        Map<String, Object> retained = new LinkedHashMap<>(); retained.put("q", q); retained.putAll(filters); retained.put("status", status);
        WebPagination.add(model, base(), size, retained);
        return viewPrefix() + "-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) { prepareForm(new AcademicRecordDTO(), model); model.addAttribute("isEdit", false); return viewPrefix() + "-form"; }

    @PostMapping
    public String create(@ModelAttribute("record") AcademicRecordDTO form, BindingResult binding, RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", bindingMessage(binding)); flash.addFlashAttribute("academicForm", form); return "redirect:" + base() + "/add"; }
        try { service.save(null, form, null); flash.addFlashAttribute("success", title() + " created successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("academicForm", form); return "redirect:" + base() + "/add"; }
        return "redirect:" + base();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try { prepareForm(service.get(id), model); model.addAttribute("isEdit", true); return viewPrefix() + "-form"; }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:" + base(); }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("record") AcademicRecordDTO form, BindingResult binding,
                         @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        if (binding.hasErrors()) { flash.addFlashAttribute("error", bindingMessage(binding)); flash.addFlashAttribute("academicForm", form); return "redirect:" + base() + "/" + id + "/edit"; }
        try { service.save(id, form, remarks); flash.addFlashAttribute("success", title() + " updated successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); flash.addFlashAttribute("academicForm", form); return "redirect:" + base() + "/" + id + "/edit"; }
        return "redirect:" + base();
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            AcademicRecordDTO record = service.get(id); model.addAttribute("record", record); model.addAttribute("module", module()); model.addAttribute("moduleTitle", title());
            model.addAttribute("changeLogs", changeLogs.getRecentChanges(entityName(), id)); return viewPrefix() + "-detail";
        } catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); return "redirect:" + base(); }
    }

    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id, @RequestParam Status status, @RequestParam(required = false) String remarks, RedirectAttributes flash) {
        try { service.changeStatus(id, status, remarks); flash.addFlashAttribute("success", title() + " status updated successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:" + base();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try { service.delete(id); flash.addFlashAttribute("success", title() + " deleted successfully."); }
        catch (Exception ex) { flash.addFlashAttribute("error", message(ex)); }
        return "redirect:" + base();
    }

    private void prepareForm(AcademicRecordDTO record, Model model) {
        if (model.containsAttribute("academicForm")) record = (AcademicRecordDTO) model.asMap().get("academicForm");
        model.addAttribute("record", record); model.addAttribute("module", module()); model.addAttribute("moduleTitle", title());
        model.addAttribute("statuses", Status.values()); model.addAttribute("today", java.time.LocalDate.now());
        switch (module()) {
            case "departments" -> model.addAttribute("faculties", workflow.options("faculties"));
            case "programs" -> { model.addAttribute("faculties", workflow.options("faculties")); model.addAttribute("departments", workflow.options("departments")); }
            case "sections" -> { model.addAttribute("programs", workflow.options("programs")); model.addAttribute("academicYears", workflow.options("academic-years")); model.addAttribute("semesters", workflow.options("semesters")); }
            case "program-subjects" -> { model.addAttribute("programs", workflow.options("programs")); model.addAttribute("semesters", workflow.options("semesters")); model.addAttribute("subjects", workflow.options("subjects")); }
            case "student-enrollments" -> { model.addAttribute("students", workflow.options("students")); model.addAttribute("programs", workflow.options("programs")); model.addAttribute("academicYears", workflow.options("academic-years")); model.addAttribute("semesters", workflow.options("semesters")); model.addAttribute("sections", workflow.options("sections")); }
            default -> { }
        }
    }

    private List<Map<String, String>> filterFields() {
        String fields = switch (module()) {
            case "departments" -> "code:Code,name:Name,faculty:Faculty,description:Description";
            case "faculties" -> "code:Code,name:Name,description:Description";
            case "programs" -> "code:Code,name:Name,faculty:Faculty,department:Department,description:Description";
            case "academic-years" -> "code:Code,name:Name,description:Description";
            case "semesters" -> "code:Code,name:Name,sequenceNumber:Sequence number,description:Description";
            case "sections" -> "code:Code,name:Name,program:Program,academicYear:Academic year,semester:Semester,description:Description";
            case "subjects" -> "code:Code,name:Name,creditHours:Credit hours,description:Description";
            case "program-subjects" -> "program:Program,subjectCode:Subject code,subject:Subject,semester:Semester";
            case "students" -> "registrationNumber:Registration number,firstName:First name,middleName:Middle name,lastName:Last name,email:Email,phone:Phone,dateOfBirth:Date of birth:date,address:Address";
            default -> "student:Student registration,program:Program,academicYear:Academic year,semester:Semester,section:Section";
        };
        List<Map<String, String>> result = new ArrayList<>();
        for (String item : fields.split(",")) {
            String[] pair = item.split(":", 3);
            Map<String, String> field = new LinkedHashMap<>(); field.put("key", pair[0]); field.put("label", pair[1]);
            if (pair.length == 3) field.put("type", pair[2]);
            result.add(field);
        }
        return result;
    }
    private Status parseStatus(String value) { try { return value == null || value.isBlank() ? null : Status.valueOf(value); } catch (IllegalArgumentException ex) { return null; } }
    private Integer parseInt(String value, int fallback) { try { return value == null ? fallback : Integer.valueOf(value); } catch (NumberFormatException ex) { return fallback; } }
    private String message(Exception ex) { return ex.getMessage() == null ? "The request could not be completed." : ex.getMessage(); }
    private String bindingMessage(BindingResult binding) { return binding.getFieldErrors().stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; ")); }
    private String entityName() { return switch (module()) { case "academic-years" -> "AcademicYear"; case "program-subjects" -> "ProgramSubject"; case "student-enrollments" -> "StudentEnrollment"; default -> title().replace(" ", ""); }; }
}
