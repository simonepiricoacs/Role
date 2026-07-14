package it.water.role;

import it.water.core.api.bundle.Runtime;
import it.water.core.api.model.PaginableResult;
import it.water.core.api.registry.ComponentRegistry;
import it.water.core.api.repository.query.Query;
import it.water.core.api.service.Service;
import it.water.core.api.user.UserManager;
import it.water.core.interceptors.annotations.Inject;
import it.water.core.testing.utils.junit.WaterTestExtension;
import it.water.core.testing.utils.runtime.TestRuntimeUtils;
import it.water.core.testing.utils.security.TestSecurityContext;
import it.water.role.api.RoleApi;
import it.water.role.api.RoleRepository;
import it.water.role.api.RoleSystemApi;
import it.water.role.model.WaterRole;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Multitenancy "Tassello 4" — behavioral enforcement tests for the tenant filter on
 * {@code WaterRole}, a {@code TenantResource} (single company, nullable {@code companyId} = global
 * role visible to every tenant). See {@code multitenancy-analysis-proposal.md} &sect;1/&sect;6.
 * <p>
 * Covers:
 * <ul>
 *     <li>findAll scoped to an active company returns the company's own roles PLUS the global
 *     (companyId == null) roles, but not another company's roles;</li>
 *     <li>with no active company, behaviour is unfiltered (backward compatible).</li>
 * </ul>
 * {@code WaterRole} is not an {@code OwnedResource}, so (like {@code WaterUser}) the tenant filter
 * is the only enforcement in play here. There is no by-id scenario in scope for this entity (not
 * requested for Role in the Tassello 4 task).
 */
@ExtendWith(WaterTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WaterRoleTenantFilterTest implements Service {

    //declared as boxed Long (not primitive long) to avoid a JUnit5 assertEquals(long,long) vs
    //assertEquals(Object,Object) overload ambiguity when compared against getCompanyId() (Long)
    private static final Long COMPANY_A = 5100L;
    private static final Long COMPANY_B = 5200L;

    @Inject
    @Setter
    private ComponentRegistry componentRegistry;

    @Inject
    @Setter
    private RoleApi roleApi;

    @Inject
    @Setter
    private RoleSystemApi roleSystemApi;

    @Inject
    @Setter
    private RoleRepository roleRepository;

    @Inject
    @Setter
    private Runtime runtime;

    @Inject
    @Setter
    private UserManager userManager;

    private long adminId;

    private long roleCompanyAId;
    private long roleCompanyBId;
    private long roleGlobalId;

    @BeforeAll
    void beforeAll() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        adminId = userManager.findUser("admin").getId();
    }

    /**
     * C1: seeds three roles directly via the SystemApi (bypasses auto-assign and permissions), one
     * per company plus a global (null companyId) one.
     */
    @Test
    @Order(1)
    void seedTenantFixtureRoles_viaSystemApi() {
        WaterRole roleA = new WaterRole("tftRoleCompanyA", "description-A");
        roleA.setCompanyId(COMPANY_A);
        roleCompanyAId = roleSystemApi.save(roleA).getId();
        Assertions.assertEquals(COMPANY_A, roleSystemApi.find(roleCompanyAId).getCompanyId());

        WaterRole roleB = new WaterRole("tftRoleCompanyB", "description-B");
        roleB.setCompanyId(COMPANY_B);
        roleCompanyBId = roleSystemApi.save(roleB).getId();
        Assertions.assertEquals(COMPANY_B, roleSystemApi.find(roleCompanyBId).getCompanyId());

        WaterRole roleGlobal = new WaterRole("tftRoleGlobal", "description-global");
        //companyId left null => global role, visible to every tenant
        roleGlobalId = roleSystemApi.save(roleGlobal).getId();
        Assertions.assertNull(roleSystemApi.find(roleGlobalId).getCompanyId());
    }

    /**
     * C2: scoped to company A, findAll must return the company-A role and the global role, but NOT
     * the company-B role.
     */
    @Test
    @Order(2)
    void findAll_scopedToCompanyA_returnsOwnAndGlobalRoles() {
        runtime.fillSecurityContext(TestSecurityContext.createContext(adminId, "admin", true, COMPANY_A));
        try {
            Query filter = filterOnIds(roleCompanyAId, roleCompanyBId, roleGlobalId);
            PaginableResult<WaterRole> result = roleApi.findAll(filter, -1, -1, null);
            Set<Long> ids = idsOf(result);
            Assertions.assertTrue(ids.contains(roleCompanyAId), "own-company role must be visible");
            Assertions.assertTrue(ids.contains(roleGlobalId), "global role must be visible");
            Assertions.assertFalse(ids.contains(roleCompanyBId), "other-company role must NOT be visible");
        } finally {
            TestRuntimeUtils.impersonateAdmin(componentRegistry);
        }
    }

    /**
     * D: backward compatibility — with no active company, all roles (including both company-scoped
     * ones) are returned unfiltered.
     */
    @Test
    @Order(3)
    void findAll_noActiveCompany_backwardCompatibleReturnsAll() {
        TestRuntimeUtils.impersonateAdmin(componentRegistry);
        Query filter = filterOnIds(roleCompanyAId, roleCompanyBId, roleGlobalId);
        PaginableResult<WaterRole> result = roleApi.findAll(filter, -1, -1, null);
        Set<Long> ids = idsOf(result);
        Assertions.assertTrue(ids.containsAll(List.of(roleCompanyAId, roleCompanyBId, roleGlobalId)),
                "with no active company, the tenant filter must not apply (backward compatible)");
    }

    private Query filterOnIds(Long... ids) {
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        return roleRepository.getQueryBuilderInstance().createQueryFilter("id IN (" + sb + ")");
    }

    private Set<Long> idsOf(PaginableResult<WaterRole> result) {
        return result.getResults().stream().map(WaterRole::getId).collect(Collectors.toSet());
    }
}
