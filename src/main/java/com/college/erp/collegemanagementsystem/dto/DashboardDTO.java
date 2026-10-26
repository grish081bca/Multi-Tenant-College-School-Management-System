package com.college.erp.collegemanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class DashboardDTO {
    private String greeting;
    private String description;
    private String roleLabel;
    private String pieGradient = "conic-gradient(#e2e8f0 0% 100%)";
    private long pieTotal;
    private List<DashboardCardDTO> cards = new ArrayList<>();
    private List<DashboardChartItemDTO> pieItems = new ArrayList<>();
    private List<DashboardChartItemDTO> barItems = new ArrayList<>();
    private List<DashboardActionDTO> actions = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardCardDTO {
        private String title;
        private long value;
        private String icon;
        private String tone;
        private String href;
        private String caption;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardChartItemDTO {
        private String label;
        private long value;
        private String color;
        private int percentage;
        private String href;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardActionDTO {
        private String label;
        private String description;
        private String icon;
        private String href;
    }
}
