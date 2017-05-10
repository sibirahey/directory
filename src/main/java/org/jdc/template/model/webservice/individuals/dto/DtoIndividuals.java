package org.jdc.template.model.webservice.individuals.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DtoIndividuals
{
    private List<DtoIndividual> individuals;

    public List<DtoIndividual> getIndividuals() {
        return individuals;
    }

    public void setIndividuals(List<DtoIndividual> individuals) {
        this.individuals = individuals;
    }
}
