package com.duckstore.controller;

import com.duckstore.dto.DuckCreateRequest;
import com.duckstore.dto.DuckResponse;
import com.duckstore.dto.DuckUpdateRequest;
import com.duckstore.service.Direction;
import com.duckstore.service.DuckChange;
import com.duckstore.service.DuckService;
import com.duckstore.service.SaveResult;
import com.duckstore.service.SortField;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ducks")
public class DuckController {

    private final DuckService service;

    public DuckController(DuckService service) {
        this.service = service;
    }

    @GetMapping
    public List<DuckResponse> list(@RequestParam(defaultValue = "quantity") String sort,
                                   @RequestParam(defaultValue = "desc") String dir) {
        return service.list(SortField.parse(sort), Direction.parse(dir)).stream().map(DuckResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DuckResponse get(@PathVariable int id) {
        return DuckResponse.from(service.get(id));
    }

    /** 201 when a new record is created, 200 when the quantity was merged into an existing one. */
    @PostMapping
    public ResponseEntity<DuckResponse> add(@Valid @RequestBody DuckCreateRequest request) {
        SaveResult result = service.add(request.color(), request.size(), request.price(), request.quantity());
        DuckResponse body = DuckResponse.from(result.duck());
        if (result.created()) {
            return ResponseEntity.created(URI.create("/api/ducks/" + body.id())).body(body);
        }
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}")
    public DuckResponse update(@PathVariable int id, @Valid @RequestBody DuckUpdateRequest request) {
        DuckChange change = new DuckChange(request.color(), request.size(), request.price(), request.quantity());
        return DuckResponse.from(service.update(id, change));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
