package org.akira.laptopstore.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
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

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE_PACKAGE);

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
    void cartSecurityDependenciesMustStayInControllers() {
        noClasses()
                .that().resideInAPackage("..cart..")
                .and().resideOutsideOfPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..security..")
                .allowEmptyShould(true)
                .because("cart controllers resolve the principal and pass the user ID to services")
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
                        .should().dependOnClassesThat(JavaClass.Predicates.resideInAnyPackage(
                                ".." + target + ".repository..",
                                ".." + target + ".entity.."
                        ).and(DescribedPredicate.describe("non-enum persistence types", type -> !type.isEnum())))
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
                // Controllers may use the security principal; security loads credentials via user.service.
                .ignoreDependency(
                        DescribedPredicate.describe("controllers", type -> type.getPackageName().contains(".controller")),
                        JavaClass.Predicates.resideInAPackage("..security.."))
                .allowEmptyShould(true)
                .check(classes);
    }
}
