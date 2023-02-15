package com.igatn.extranet.rest.historyActivity.models;

import com.igatn.extranet.domainjpa.impl.domain.demand.TypeDemand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemandTypesResponseModel {
    Boolean success;
    List<TypeDemand> types;
}
