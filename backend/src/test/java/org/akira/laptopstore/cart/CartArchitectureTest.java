package org.akira.laptopstore.cart;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CartArchitectureTest {
    @Test
    void cartMustUseCatalogContractsInsteadOfWebOrPersistenceLayers() {
        noClasses()
                .that().resideInAPackage("..cart..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..catalog.controller..", "..catalog.entity..", "..catalog.repository..")
                .check(new ClassFileImporter()
                        .withImportOption(com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages("org.akira.laptopstore.cart"));
    }
}
