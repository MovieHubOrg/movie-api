package com.movie.api.dto.statistic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticGroupDto {
    private String label;
    private Long value;

    public StatisticGroupDto(Integer label, Long value) {
        this.label = label != null ? label.toString() : "Unknown";
        this.value = value;
    }
}
