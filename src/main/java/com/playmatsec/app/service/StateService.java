package com.playmatsec.app.service;

import java.util.List;
import com.playmatsec.app.repository.model.State;
import com.playmatsec.app.controller.model.StateDTO;

public interface StateService {
    List<State> getStates(String nombre, Integer countryId);
    State getStateById(String id);
    State createState(StateDTO state);
    State updateState(String id, String updateRequest);
    State updateState(String id, StateDTO state);
    Boolean deleteState(String id);
}
