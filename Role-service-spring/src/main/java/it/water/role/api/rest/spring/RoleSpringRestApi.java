/*
 * Copyright 2024 Aristide Cittadino
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.water.role.api.rest.spring;

import com.fasterxml.jackson.annotation.JsonView;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.role.api.rest.RoleRestApi;
import it.water.role.model.WaterRole;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @Author Aristide Cittadino
 * Interface exposing same methods of its parent RoleRestApi but adding Spring annotations.
 * Swagger annotation should be found because they have been defined in the parent RoleRestApi.
 */
@RequestMapping("/roles")
@FrameworkRestApi
public interface RoleSpringRestApi extends RoleRestApi {
    @PostMapping
    @JsonView(WaterJsonView.Public.class)
    WaterRole save(@RequestBody WaterRole role);

    @PutMapping
    @JsonView(WaterJsonView.Public.class)
    WaterRole update(@RequestBody WaterRole role);

    @PostMapping("/assign")
    @JsonView(WaterJsonView.Public.class)
    void assignRole(@RequestParam("userId") long userId, @RequestParam("roleId") long roleId);

    @PostMapping("/unassign")
    @JsonView(WaterJsonView.Public.class)
    void unassignRole(@RequestParam("userId") long userId, @RequestParam("roleId") long roleId);


    @GetMapping("/{id}")
    @JsonView(WaterJsonView.Public.class)
    WaterRole find(@PathVariable("id") long id);

    @GetMapping
    @JsonView(WaterJsonView.Public.class)
    PaginableResult<WaterRole> findAll();

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @JsonView(WaterJsonView.Public.class)
    void remove(@PathVariable("id") long id);

}
