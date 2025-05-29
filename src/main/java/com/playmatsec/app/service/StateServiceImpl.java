package com.playmatsec.app.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.playmatsec.app.controller.model.StateDTO;
import com.playmatsec.app.repository.StateRepository;
import com.playmatsec.app.repository.model.State;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StateServiceImpl implements StateService {
    private final StateRepository stateRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<State> getStates(String nombre, Integer countryId) {
        if (StringUtils.hasLength(nombre) || countryId != null) {
            return stateRepository.search(nombre, countryId);
        }
        List<State> states = stateRepository.getStates();
        return states.isEmpty() ? null : states;
    }

    @Override
    public State getStateById(String id) {
        try {
            Integer stateId = Integer.parseInt(id);
            return stateRepository.getById(stateId);
        } catch (NumberFormatException e) {
            log.error("Invalid state ID format: {}", id, e);
            return null;
        }
    }

    @Override
    public State createState(StateDTO request) {
        if (request != null && StringUtils.hasLength(request.getName())) {
            State state = objectMapper.convertValue(request, State.class);
            return stateRepository.save(state);
        }
        return null;
    }

    @Override
    public State updateState(String id, String request) {
        State state = getStateById(id);
        if (state != null) {
            try {
                JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(objectMapper.readTree(request));
                JsonNode target = jsonMergePatch.apply(objectMapper.readTree(objectMapper.writeValueAsString(state)));
                State patched = objectMapper.treeToValue(target, State.class);
                stateRepository.save(patched);
                return patched;
            } catch (JsonProcessingException | JsonPatchException e) {
                log.error("Error updating state {}", id, e);
                return null;
            }
        }
        return null;
    }

    @Override
    public State updateState(String id, StateDTO request) {
        State state = getStateById(id);
        if (state != null) {
            // state.update(request); // Implementar si existe método update
            stateRepository.save(state);
            return state;
        }
        return null;
    }

    @Override
    public Boolean deleteState(String id) {
        try {
            Integer stateId = Integer.parseInt(id);
            State state = stateRepository.getById(stateId);
            if (state != null) {
                stateRepository.delete(state);
                return true;
            }
        } catch (NumberFormatException e) {
            log.error("Invalid state ID format: {}", id, e);
        }
        return false;
    }
}
