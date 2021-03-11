package com.epam.esm.entity;

import com.epam.esm.utils.OrderType;
import com.epam.esm.utils.SortParameter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class SearchGiftCertificateParameter {
    private String name;
    private String description;
    private List<String> tags;
    private SortParameter sortBy;
    private OrderType order;

    public boolean isEmpty() {
        return (name == null || name.isEmpty()) && (description == null || description.isEmpty()) && (tags == null || tags.isEmpty()) && sortBy == null && order == null;
    }
}