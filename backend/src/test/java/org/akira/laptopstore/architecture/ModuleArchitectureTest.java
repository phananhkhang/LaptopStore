package org.akira.laptopstore.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleArchitectureTest {

    private static final String BASE_PACKAGE = "org.akira.laptopstore";

    private static final List<String> BUSINESS_MODULES = List.of(
            "auth",
            "user",
            "catalog",
            "cart",
            "order",
            "payment",
            "promotion",
            "inventory",
            "procurement",
            "review"
    );

    private static final Map<String, Set<String>> ALLOWED_BUSINESS_DEPENDENCIES = Map.of(
            "auth", Set.of("user"),
            "user", Set.of(),
            "catalog", Set.of(),
            "cart", Set.of("catalog"),
            "order", Set.of("user", "cart", "catalog", "inventory", "promotion"),
            "payment", Set.of("order"),
            "promotion", Set.of(),
            "inventory", Set.of("catalog"),
            "procurement", Set.of("catalog", "inventory"),
            "review", Set.of("order", "catalog")
    );

    private final JavaClasses classes = new ClassFileImporter().importPackages(BASE_PACKAGE);

    @Test
    void controllersMustNotDependDirectlyOnRepositories() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .allowEmptyShould(true)
                .because("controller must call service, not repository")
                .check(classes);
    }

    @Test
    void modulesMustNotImportPersistenceTypesFromOtherModules() {
        for (String source : BUSINESS_MODULES) {
            for (String target : BUSINESS_MODULES) {
                if (source.equals(target)) {
                    continue;
                }

                noClasses()
                        .that().resideInAPackage(".." + source + "..")
                        .should().dependOnClassesThat().resideInAnyPackage(
                                ".." + target + ".repository..",
                                ".." + target + ".entity.."
                        )
                        .allowEmptyShould(true)
                        .because(source + " must use service/DTO contracts instead of " + target + " persistence types")
                        .check(classes);
            }
        }
    }

    @Test
    void businessModulesMustFollowDependencyAllowlist() {
        for (String source : BUSINESS_MODULES) {
            Set<String> allowedTargets = ALLOWED_BUSINESS_DEPENDENCIES.get(source);

            for (String target : BUSINESS_MODULES) {
                if (source.equals(target) || allowedTargets.contains(target)) {
                    continue;
                }

                noClasses()
                        .that().resideInAPackage(".." + source + "..")
                        .should().dependOnClassesThat().resideInAPackage(".." + target + "..")
                        .allowEmptyShould(true)
                        .because(source + " is not allowed to depend on " + target)
                        .check(classes);
            }
        }
    }

    @Test
    void topLevelPackagesShouldBeFreeOfCycles() {
        slices()
                .matching(BASE_PACKAGE + ".(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true)
                .check(classes);
    }
}
